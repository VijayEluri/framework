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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.persist.ServiceInfo;
import org.oobium.persist.db.Database;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.db.DbServiceInfo;

public class DerbyEmbeddedPersistService extends DbPersistService {

	
	
	public DerbyEmbeddedPersistService() {
		super();
	}
	
	public DerbyEmbeddedPersistService(String client, final String database, final boolean inMemory) {
		super(client, new HashMap<String, Object>() {
							private static final long serialVersionUID = 1L;
							{
								put("database", database);
								put("memory", inMemory);
							}
						});
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
	protected Database createDatabase(String client, Map<String, Object> properties) {
		return new DerbyEmbeddedDatabase(client, properties);
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
		return new DbServiceInfo(this);
	}

}
