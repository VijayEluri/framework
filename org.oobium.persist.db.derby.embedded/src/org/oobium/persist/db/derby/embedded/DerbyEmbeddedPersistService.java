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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.oobium.persist.ServiceInfo;
import org.oobium.persist.db.ConnectionPool;
import org.oobium.persist.db.DbPersistService;

public class DerbyEmbeddedPersistService extends DbPersistService {

	public DerbyEmbeddedPersistService() {
		super();
	}
	
	public DerbyEmbeddedPersistService(String schema, boolean inMemory) {
		super(schema, inMemory);
	}
	
	@Override
	protected ConnectionPool createConnectionPool(String client, Map<String, Object> properties) {
		return new DerbyEmbeddedConnectionPool(client, properties);
	}

	private String adjustSql(String sql) {
		if(sql.equals("show schemas")) {
			return	"select schemaname schema_name from sys.sysschemas";
		} else if(sql.equals("show tables")) {
			return	"select schemaname schema_name, tablename table_name from sys.sysschemas s, sys.systables t where t.tabletype='T' and t.schemaid=s.schemaid";
		} else if(sql.startsWith("show columns from ")) {
			return	"select columnname column_name, columndatatype column_type, columndefault column_default, autoincrementinc column_inc " +
				 	"from sys.syscolumns c, sys.systables t where t.tabletype='T' and t.tablename='" + sql.substring(18).toUpperCase() +
				 	"' and t.tableid=c.referenceid";
		}
		return sql;
	}
	
	@Override
	public List<Map<String, Object>> executeQuery(String sql, Object... values) throws SQLException {
		return super.executeQuery(adjustSql(sql), values);
	}
	
	@Override
	public List<List<Object>> executeQueryLists(String sql, Object... values) throws SQLException {
		return super.executeQueryLists(adjustSql(sql), values);
	}
	
	@Override
	public Object executeQueryValue(String sql, Object... values) throws SQLException {
		return super.executeQueryValue(adjustSql(sql), values);
	}
	
	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public String getName() {
				return "Derby Embedded Database Persist Service";
			}
			@Override
			public String getProvider() {
				return "oobium.org";
			}
			@Override
			public String getVersion() {
				return "0.5.0";
			}
			@Override
			public String getDescription() {
				return "Persist service for embedded derby databases";
			}
		};
	}

	@Override
	public String getPersistServiceName() {
		return getClass().getPackage().getName();
	}

}
