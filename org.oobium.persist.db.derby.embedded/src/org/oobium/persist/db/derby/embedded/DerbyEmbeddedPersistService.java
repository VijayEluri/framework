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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	protected ConnectionPool createConnectionPool(String client, Map<String, Object> properties) {
		return new DerbyEmbeddedConnectionPool(client, properties);
	}
	
	public void dropDatabase() {
		if(inMemory()) {
			super.dropDatabase();
		} else {
			logger.info("dropping all tables...");
			
			String sql = "select t.tablename, c.constraintname" + " from sys.sysconstraints c, sys.systables t"
					+ " where c.type = 'F' and t.tableid = c.tableid";

			List<Map<String, Object>> constraints = null;
			try {
				constraints = executeQuery(sql);
			} catch(SQLException e) {
				logger.info("database has not yet been created");
				return;
			}

			for(Map<String, Object> map : constraints) {
				sql = "alter table " + map.get("tablename") + " drop constraint " + map.get("constraintname");
				logger.debug(sql);
				try {
					executeUpdate(sql);
				} catch(Exception e) {
					logger.error("could not alter table: " + sql, e);
				}
			}

			try {
				Connection connection = getConnection();
				ResultSet rs = null;
				try {
					String appSchema = "ROOT";
					rs = connection.getMetaData().getTables(null, appSchema, "%", new String[] { "TABLE" });
					while(rs.next()) {
						sql = "drop table " + appSchema + "." + rs.getString(3);
						logger.debug(sql);
						Statement stmt = connection.createStatement();
						try {
							stmt.executeUpdate(sql);
						} finally {
							stmt.close();
						}
					}
				} finally {
					if(rs != null) {
						rs.close();
					}
					// connection.close(); no need - connection will be closed when
					// the session is closed
				}
				logger.info("all tables dropped.");
			} catch(SQLException e) {
				// well, something went wrong...
				logger.error("ERROR dropping database", e);
			}
		}
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
			public String getDescription() {
				return "Persist service for embedded derby databases";
			}
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
				return "0.6.0";
			}
		};
	}

	@Override
	public String getPersistServiceName() {
		return getClass().getPackage().getName();
	}

}
