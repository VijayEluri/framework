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
package org.oobium.persist.db.internal;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.oobium.logging.Logger;
import org.oobium.logging.LogProvider;
import org.oobium.persist.db.DbPersistService;

public class SingleConnectionManager {

	private Logger logger;
	
	private Connection connection;
	
	private String database;
	private boolean inMemory;

	public SingleConnectionManager(String database, boolean inMemory) {
		logger = LogProvider.getLogger(DbPersistService.class);
		this.database = database;
		this.inMemory = inMemory;
	}
	
	private Connection createConnection() throws SQLException {
        try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch(ClassNotFoundException e) {
			logger.error(e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:derby:");
		if(inMemory) {
			sb.append("memory:");
		}
		if(database.charAt(0) != File.separatorChar) {
			sb.append(System.getProperty("user.dir")).append(File.separatorChar);
		}
		sb.append(database);
		sb.append(";create=true;user=\"session\";password=\"session\"");

		String dbURL = sb.toString();
		if(logger.isLoggingDebug()) {
			logger.debug("create connection: " + dbURL);
		}
		
    	Connection connection = DriverManager.getConnection(dbURL);
        Statement s = connection.createStatement();
        try {
	        s.executeUpdate("SET SCHEMA APP");
        } finally {
        	try {
        		s.close();
        	} catch(SQLException e) {
        		// discard
        	}
        }
        
        return connection;
    }
	
	public void dropDatabase() throws SQLException {
		if(inMemory) {
			StringBuilder sb = new StringBuilder();
			sb.append("jdbc:derby:memory:");
			if(database.charAt(0) != File.separatorChar) {
				sb.append(System.getProperty("user.dir")).append(File.separatorChar);
			}
			sb.append(database);
			sb.append(";drop=true;user=\"session\";password=\"session\"");
	
			String dbURL = sb.toString();
			if(logger.isLoggingDebug()) {
				logger.debug("drop database connection: " + dbURL);
			}

			try {
				DriverManager.getConnection(dbURL);
			} catch(SQLException e) {
				if(!e.getSQLState().equals("08006")) { // 08006 indicates success
					throw e;
				}
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public Connection getConnection() throws SQLException {
		if(!hasConnection()) {
			try {
				connection = createConnection();
			} catch(SQLException e) {
				connection = null;
				throw e;
			}
		}
    	return connection;
    }
	
	public boolean hasConnection() {
    	try {
			return (connection != null && !connection.isClosed());
		} catch(SQLException e) {
			logger.warn(e);
			return false;
		}
    }
	
	public boolean inMemory() {
		return inMemory;
	}
	
}
