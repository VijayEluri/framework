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

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.oobium.logging.LogProvider;
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
		String path = database.getCanonicalPath();
		return path;
	}
	
	public DerbyEmbeddedConnectionPool(String client, Map<String, Object> properties) {
		super(client, properties, LogProvider.getLogger(DerbyEmbeddedPersistService.class));
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
		ds.setUser(coerce(properties.get("username"), "root"));
		ds.setPassword(coerce(properties.get("password"), ""));
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
