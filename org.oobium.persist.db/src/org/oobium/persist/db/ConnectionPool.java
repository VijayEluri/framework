/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.persist.db;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.oobium.logging.Logger;

public class ConnectionPool {

	protected String client;
	protected Logger logger;
	private ConnectionPoolDataSource ds;
	private Set<PooledConnection> connections;
	private Stack<PooledConnection> pool;
	private Semaphore semaphore;
	private int timeout; // in seconds
	private ConnectionEventListener connectionListener;
	
	public ConnectionPool(String client, Map<String, Object> properties, ConnectionPoolDataSource dataSource, Logger logger) {
		this.client = client;
		this.logger = logger;
		this.ds = dataSource;

		int maxConnections = coerce(properties.get("maxConnections"), int.class);
		if(maxConnections < 1) {
			maxConnections = 10;
		}

		this.connections = new HashSet<PooledConnection>();
		
		int timeout = coerce(properties.get("timeout"), int.class);
		this.timeout = (timeout < 1) ? 30 : timeout;
		
		pool = new Stack<PooledConnection>();
		semaphore = new Semaphore(maxConnections, true);
		
		connectionListener = new ConnectionEventListener() {
			@Override
			public void connectionClosed(ConnectionEvent event) {
				PooledConnection pc = (PooledConnection) event.getSource();
				pc.removeConnectionEventListener(this);
				addConnection(pc);
			}
			@Override
			public void connectionErrorOccurred(ConnectionEvent event) {
				PooledConnection pc = (PooledConnection) event.getSource();
				pc.removeConnectionEventListener(this);
				disposeConnection(pc);
			}
		};
	}
	
	private synchronized void addConnection(PooledConnection connection) {
		semaphore.release();
		pool.push(connection);
	}

	private void log(String message) {
		message = getClass().getSimpleName() + ": " + message;
		try {
			PrintWriter log = ds.getLogWriter();
			log.println(message);
		} catch(SQLException e) {
			logger.error(e);
		}
	}
	
	private synchronized void disposeConnection(PooledConnection connection) {
		connections.remove(connection);
		semaphore.release();
		try {
			connection.close();
		} catch(SQLException e) {
			log("could not dispose connection");
		}
	}
	
	/**
	 * Closes all connections and clears the pool.
	 */
	public synchronized void close() {
		if(client != null) { // in case is has been disposed
			if(!connections.isEmpty()) {
				for(PooledConnection connection : connections) {
					try {
						connection.close();
					} catch(SQLException e) {
						log("could not close connection during dispose");
					}
				}
			}
			semaphore.release(pool.size());
			connections.clear();
			pool.clear();
		}
	}

	public boolean isDisposed() {
		return client == null;
	}
	
	/**
	 * Closes all connections, clears the pool, and nulls out all fields.
	 */
	public synchronized void dispose() {
		if(client != null) { // allow multiple calls
			if(!connections.isEmpty()) {
				for(PooledConnection connection : connections) {
					try {
						connection.close();
					} catch(SQLException e) {
						log("could not close connection during dispose");
					}
				}
			}
			connections.clear();
			pool.clear();
			
			client = null;
			logger = null;
			ds = null;
			connections = null;
			pool = null;
			semaphore = null;
			connectionListener = null;
		}
	}

	private synchronized Connection doGetConnection() throws SQLException {
		if(isDisposed()) {
			throw new IllegalStateException("ConnectionPool is disposed");
		}

		PooledConnection pc;
		if(pool.isEmpty()) {
			pc = ds.getPooledConnection();
			connections.add(pc);
		} else {
			pc = pool.pop();
		}
		pc.addConnectionEventListener(connectionListener);
		Connection connection = pc.getConnection();
		return connection;
	}
	
	public Connection getConnection() throws SQLException {
		if(pool == null) {
			throw new IllegalStateException("ConnectionPool is disposed");
		}

		try {
			if(!semaphore.tryAcquire(timeout, TimeUnit.SECONDS)) {
				throw new SQLException("timed out trying to obtain a database connection");
			}
		} catch(InterruptedException e) {
			throw new SQLException("Interupted while waiting for a database connection");
		}

		boolean failed = true;
		try {
			Connection connection = doGetConnection();
			failed = false;
			return connection;
		} finally {
			if(failed) {
				semaphore.release();
			}
		}
	}

	protected ConnectionPoolDataSource getDataSource() {
		return ds;
	}
	
}
