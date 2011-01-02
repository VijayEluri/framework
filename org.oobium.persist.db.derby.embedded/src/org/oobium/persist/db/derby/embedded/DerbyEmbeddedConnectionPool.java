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
import java.io.IOException;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.oobium.logging.Logger;
import org.oobium.persist.db.ConnectionPool;

public class DerbyEmbeddedConnectionPool extends ConnectionPool {

	private static String getDatabase(String client, Map<String, Object> properties) throws IOException {
		Object o = properties.get("database");
		if(!(o instanceof String)) {
			o = ".." + File.separator + "data" + File.separator + client;
		}
		File database = new File((String) o);
		if(!database.isAbsolute()) {
			database = new File(System.getProperty("user.dir"), (String) o);
		}
		return database.getCanonicalPath();
	}
	
	private static String getPassword(Map<String, Object> properties) {
		Object password = properties.get("password");
		if(password instanceof String) {
			return (String) password;
		}
		return "";
	}
	
	private static String getUsername(Map<String, Object> properties) {
		Object username = properties.get("username");
		if(username instanceof String) {
			return (String) username;
		}
		return "root";
	}
	
	public DerbyEmbeddedConnectionPool(String client, Map<String, Object> properties) {
		super(client, properties, Logger.getLogger(DerbyEmbeddedPersistService.class));
	}

	@Override
	protected ConnectionPoolDataSource createDataSource(Map<String, Object> properties) {
		String database;
		try {
			database = getDatabase(client, properties);
		} catch(IOException e) {
			logger.error(e);
			return null;
		}
		
		EmbeddedConnectionPoolDataSource ds = new EmbeddedConnectionPoolDataSource();
		ds.setCreateDatabase("create");
		ds.setDatabaseName(database);
		ds.setUser(getUsername(properties));
		ds.setPassword(getPassword(properties));
		return ds;
	}

	@Override
	public String getDatabaseIdentifier() {
		EmbeddedConnectionPoolDataSource ds = (EmbeddedConnectionPoolDataSource) getDataSource();
		if(ds != null) {
			return ds.getDatabaseName();
		} else {
			return "";
		}
	}

}
