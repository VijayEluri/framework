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
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.oobium.logging.Logger;

public abstract class ConnectionPool {

	protected final String client;
	protected final Logger logger;
	private final ConnectionPoolDataSource ds;
	private final Stack<PooledConnection> connections;
	private final Semaphore semaphore;
	private final int timeout; // in seconds
	private final ConnectionEventListener connectionListener;
	
	public ConnectionPool(String client, Map<String, Object> properties, Logger logger) {
		this.client = client;
		this.logger = logger;
		ds = createDataSource(properties);

		int maxConnections = coerce(properties.get("maxConnections"), int.class);
		if(maxConnections < 1) {
			maxConnections = 10;
		}

		int timeout = coerce(properties.get("timeout"), int.class);
		this.timeout = (timeout < 1) ? 30 : timeout;
		
		connections = new Stack<PooledConnection>();
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
	
	protected abstract ConnectionPoolDataSource createDataSource(Map<String, Object> properties);

	private synchronized void addConnection(PooledConnection connection) {
		semaphore.release();
		connections.push(connection);
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
		semaphore.release();
		try {
			connection.close();
		} catch(SQLException e) {
			log("could not dispose connection");
		}
	}
	
	public synchronized void dispose() {
		if(connections != null) { // allow multiple calls
			if(!connections.isEmpty()) {
				for(PooledConnection connection : connections) {
					try {
						connection.close();
					} catch(SQLException e) {
						log("could not close connection during dispose");
					}
				}
				connections.clear();
			}
		}
	}

	private synchronized Connection doGetConnection() throws SQLException {
		if(connections == null) {
			throw new IllegalStateException("ConnectionPool is disposed");
		}

		PooledConnection pc;
		if(connections.isEmpty()) {
			pc = ds.getPooledConnection();
		} else {
			pc = connections.pop();
		}
		pc.addConnectionEventListener(connectionListener);
		Connection connection = pc.getConnection();
		return connection;
	}
	
	public Connection getConnection() throws SQLException {
		if(connections == null) {
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

	public abstract String getDatabaseIdentifier();

	protected ConnectionPoolDataSource getDataSource() {
		return ds;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.getClass() == getClass()) {
			return ((ConnectionPool) obj).getDatabaseIdentifier().equals(getDatabaseIdentifier());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getDatabaseIdentifier().hashCode();
	}

}
