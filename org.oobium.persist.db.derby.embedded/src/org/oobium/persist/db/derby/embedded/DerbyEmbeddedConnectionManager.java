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
package org.oobium.persist.db.derby.embedded;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.oobium.persist.db.ConnectionManager;

public class DerbyEmbeddedConnectionManager extends ConnectionManager {

	public DerbyEmbeddedConnectionManager(String database, boolean inMemory) {
		super(database, inMemory);
	}
	
	@Override
	protected Connection createConnection() throws SQLException {
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
		sb.append(File.separatorChar).append(database);
		sb.append(";create=true;");
		sb.append("user=\"").append(username).append("\";password=\"").append(password).append('"');

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

	@Override
	public void createDatabase() throws SQLException {
		createConnection();
	}
	
	@Override
	public void dropDatabase() throws SQLException {
		if(inMemory) {
			StringBuilder sb = new StringBuilder();
			sb.append("jdbc:derby:memory:");
			sb.append(File.separatorChar).append(database);
			sb.append(";drop=true;");
			sb.append("user=\"").append(username).append("\";password=\"").append(password).append('"');
	
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

	@Override
	protected String getDefaultHost() {
		return null;
	}

	@Override
	protected int getDefaultPort() {
		return 0;
	}

}
