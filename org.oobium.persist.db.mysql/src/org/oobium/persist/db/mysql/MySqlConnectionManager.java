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
package org.oobium.persist.db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.oobium.persist.db.ConnectionManager;

public class MySqlConnectionManager extends ConnectionManager {

	public MySqlConnectionManager(String database) {
		super(database, false);
	}
	
	@Override
	protected Connection createConnection() throws SQLException {
        try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch(ClassNotFoundException e) {
			throw new SQLException("could not create connection: ClassNotFoundException", e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:mysql://").append(host).append(':').append(port);
		sb.append('/').append(database);

		String dbURL = sb.toString();
		if(logger.isLoggingDebug()) {
			logger.debug("create connection: " + dbURL);
		}
		
    	return DriverManager.getConnection(dbURL, username, password);
    }

	@Override
	public void createDatabase() throws SQLException {
		exec("CREATE DATABASE " + database);
	}
	
	private void exec(String cmd) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			StringBuilder sb = new StringBuilder();
			sb.append("jdbc:mysql://").append(host).append(':').append(port);

			String dbURL = sb.toString();
			if(logger.isLoggingDebug()) {
				logger.debug(cmd + ": " + dbURL);
			}
			
	    	connection = DriverManager.getConnection(dbURL, username, password);
			statement = connection.createStatement();
			statement.execute(cmd);
		} catch(SQLException e) {
			throw e;
		} catch(Exception e) {
			throw new SQLException("could not create database", e);
		} finally {
			if(statement != null) {
				try {
					statement.close();
				} catch(SQLException e) {
					// discard
				}
			}
			if(connection != null) {
				try {
					connection.close();
				} catch(SQLException e) {
					// discard
				}
			}
		}
	}
	
	@Override
	public void dropDatabase() throws SQLException {
		exec("DROP DATABASE " + database);
	}

	@Override
	protected String getDefaultHost() {
		return "127.0.0.1";
	}

	@Override
	protected int getDefaultPort() {
		return 3306;
	}

}
