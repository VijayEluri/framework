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
package org.oobium.persist.db.tests;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.PersistServices;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.db.derby.embedded.DerbyEmbeddedPersistService;

public class DbPersistorTestCase {

	protected static final Logger logger = Logger.getLogger(DbPersistService.class);
	
	protected static final String schema = "dbtest";
	protected static DbPersistService service;

	@BeforeClass
	public static void setUpClass() {
		logger.setConsoleLevel(Logger.TRACE);
	}

	@Before
	public void setUp() {
		service = new DerbyEmbeddedPersistService(schema, true);
		Model.setLogger(logger);
		Model.setPersistServices(new PersistServices(service));
	}

	@After
	public void tearDown() {
		Model.setLogger(null);
		Model.setPersistServices(null);
		dropDatabase();
		service.closeSession();
		service = null;
	}

	public void createDatabase(String tables) {
		try {
			for(char table : tables.toCharArray()) {
				switch(table) {
				case 'a':
					service.executeUpdate("CREATE TABLE a_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,a_name VARCHAR(32672), b_model INTEGER, c_model INTEGER, included_b_model INTEGER)");
					break;
				case 'b':
					service.executeUpdate("CREATE TABLE b_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,b_name VARCHAR(32672), c_model INTEGER)");
					break;
				case 'c':
					service.executeUpdate("CREATE TABLE c_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,c_name VARCHAR(32672))");
					break;
				case 'd':
					service.executeUpdate("CREATE TABLE d_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,d_name VARCHAR(32672), f_model INTEGER)");
					break;
				case 'e':
					service.executeUpdate("CREATE TABLE e_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,e_name VARCHAR(32672))");
					break;
				case 'f':
					service.executeUpdate("CREATE TABLE f_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,f_name VARCHAR(32672))");
					break;
				case 'g':
					service.executeUpdate("CREATE TABLE g_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,g_name VARCHAR(32672), h_model INTEGER NOT NULL)");
					break;
				case 'h':
					service.executeUpdate("CREATE TABLE h_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,h_name VARCHAR(32672))");
					break;
				case 'i':
					service.executeUpdate("CREATE TABLE i_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,i_name VARCHAR(32672), j_model INTEGER, included_j_model INTEGER)");
					break;
				case 'j':
					service.executeUpdate("CREATE TABLE j_models(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,j_name VARCHAR(32672), b_model INTEGER, included_b_model INTEGER)");
					break;
				}
			}
			if(tables.indexOf('a') != -1 && tables.indexOf('b') != -1) {
				service.executeUpdate("CREATE TABLE a_models__b_models___b_models__null(a_models__b_models INTEGER, b_models__null INTEGER)");
			}
			if(tables.indexOf('d') != -1 && tables.indexOf('e') != -1) {
				service.executeUpdate("CREATE TABLE d_models__e_models___e_models__d_models(d_models__e_models INTEGER, e_models__d_models INTEGER)");
			}
			logger.info("database setup successfully\n");
		} catch(SQLException e) {
			throw new IllegalStateException("error during setup");
		}
	}
	
	/**
	 * Drops all tables in the database, if it exists
	 */
	public final void dropDatabase() {
		logger.info("Dropping database...");

//		not currently using constraints
//		
//		String sql = "select t.tablename, c.constraintname" + " from sys.sysconstraints c, sys.systables t"
//				+ " where c.type = 'F' and t.tableid = c.tableid";
//
//		List<Map<String, Object>> constraints = null;
//		try {
//			constraints = service.executeQuery(sql);
//		} catch(SQLException e) {
//			logger.info("database has not yet been created");
//			return;
//		}
//		
//		for(Map<String, Object> map : constraints) {
//			sql = "alter table " + map.get("tablename") + " drop constraint " + map.get("constraintname");
//			logger.debug(sql);
//			try {
//				service.executeUpdate(sql);
//			} catch(Exception e) {
//				logger.error("could not alter table: " + sql, e);
//			}
//		}

		try {
			Connection connection = service.getConnection();
			ResultSet rs = null;
			try {
				rs = connection.getMetaData().getTables(null, "APP", "%", new String[] { "TABLE" });
				while(rs.next()) {
					String sql = "drop table APP." + rs.getString(3);
					logger.debug(sql);
					Statement stmt = connection.createStatement();
					try {
						stmt.executeUpdate(sql);
					} finally{
						stmt.close();
					}
				}
			} finally {
				if(rs != null) {
					rs.close();
				}
				// connection.close(); no need - connection will be closed when the session is closed
			}
			logger.info("Database dropped.\n");
		} catch(SQLException e) {
			// well, something went wrong...
			logger.error("ERROR dropping database", e);
		}
	}

}
