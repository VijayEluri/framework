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
package org.oobium.persist.db.derby;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.apache.derby.jdbc.ClientConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.oobium.logging.LogProvider;
import org.oobium.persist.db.ConnectionPool;

public class DerbyConnectionPool extends ConnectionPool {

	public DerbyConnectionPool(String client, Map<String, Object> properties) {
		super(client, properties, LogProvider.getLogger(DerbyPersistService.class));
	}

	@Override
	protected ConnectionPoolDataSource createDataSource(Map<String, Object> properties) {
		String host = (String) properties.get("host");
		if("embedded".equals(host)) {
			EmbeddedConnectionPoolDataSource ds = new EmbeddedConnectionPoolDataSource();
			ds.setCreateDatabase("create");
			ds.setDatabaseName((String) properties.get("database"));
			ds.setUser((String) properties.get("username"));
			ds.setPassword((String) properties.get("password"));
			return ds;
		} else {
			ClientConnectionPoolDataSource ds = new ClientConnectionPoolDataSource();
			ds.setCreateDatabase("create");
			ds.setDatabaseName((String) properties.get("database"));
			ds.setPortNumber(coerce(properties.get("port"), int.class));
			ds.setUser((String) properties.get("username"));
			ds.setPassword((String) properties.get("password"));
			return ds;
		}
	}

	@Override
	public String getDatabaseIdentifier() {
		Object ds = getDataSource();
		if(ds instanceof ClientConnectionPoolDataSource) {
			return ((ClientConnectionPoolDataSource) getDataSource()).getDatabaseName();
		} else if(ds instanceof EmbeddedConnectionPoolDataSource) {
			return ((EmbeddedConnectionPoolDataSource) getDataSource()).getDatabaseName();
		} else {
			return "unknown datasource type: " + ds;
		}
	}

}
