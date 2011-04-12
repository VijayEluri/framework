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

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.oobium.logging.LogProvider;
import org.oobium.persist.db.ConnectionPool;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class MySqlConnectionPool extends ConnectionPool {

	public MySqlConnectionPool(String client, Map<String, Object> properties) {
		super(client, properties, LogProvider.getLogger(MySqlPersistService.class));
	}

	@Override
	protected ConnectionPoolDataSource createDataSource(Map<String, Object> properties) {
		MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
//		ds.setCreateDatabase("create");
		ds.setDatabaseName((String) properties.get("database"));
		ds.setPortNumber(coerce(properties.get("port"), 3306));
		ds.setUser(coerce(properties.get("username"), "root"));
		ds.setPassword(coerce(properties.get("password"), ""));
		return ds;
	}

	@Override
	public String getDatabaseIdentifier() {
		return ((MysqlConnectionPoolDataSource) getDataSource()).getDatabaseName();
	}

}
