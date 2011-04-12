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

import java.sql.Connection;
import java.sql.SQLException;

import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;

public abstract class ConnectionManager {

	protected final Logger logger;
	protected final String host;
	protected final int port;
	protected final String database;
	protected final String username;
	protected final String password;
	protected final boolean inMemory;
	
	protected Connection connection;

	public ConnectionManager(String database, boolean inMemory) {
		logger = LogProvider.getLogger(DbPersistService.class);
		
		int ix = database.indexOf('@');
		if(ix == -1) {
			this.username = "root";
			this.password = "";
		} else {
			String credentials = database.substring(0, ix);
			database = database.substring(ix+1);
			ix = credentials.indexOf(':');
			if(ix == -1) {
				this.username = credentials;
				this.password = "";
			} else {
				this.username = credentials.substring(0, ix);
				this.password = credentials.substring(ix+1);
			}
		}

		ix = database.indexOf('/');
		if(ix == -1) {
			this.host = getDefaultHost();
			this.port = getDefaultPort();
			this.database = database;
		} else if(ix == 0) {
			this.host = getDefaultHost();
			this.port = getDefaultPort();
			this.database = database.substring(1);
		} else {
			String s = database.substring(0, ix);
			this.database = database.substring(ix+1);
			ix = s.indexOf(':');
			if(ix == -1) {
				this.host = s;
				this.port = getDefaultPort();
			} else {
				this.host = s.substring(0, ix);
				this.port = Integer.parseInt(s.substring(ix+1));
			}
		}

		this.inMemory = inMemory;
	}
	
	protected abstract Connection createConnection() throws SQLException;
	
	public abstract void createDatabase() throws SQLException;

	public abstract void dropDatabase() throws SQLException;
	
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

	public String getDatabase() {
		return database;
	}
	
	protected abstract String getDefaultHost();
	
	protected abstract int getDefaultPort();
	
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
