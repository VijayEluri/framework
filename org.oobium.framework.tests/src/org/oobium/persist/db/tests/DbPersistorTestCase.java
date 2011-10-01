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

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.SimplePersistServiceProvider;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.db.derby.embedded.DerbyEmbeddedPersistService;

public class DbPersistorTestCase {

	protected static final Logger logger = LogProvider.getLogger(DbPersistService.class);
	
	protected static DbPersistService service;

	@BeforeClass
	public static void setUpClass() {
		logger.setConsoleLevel(Logger.TRACE);
	}

	@Before
	public void setUp() throws SQLException {
		service = new DerbyEmbeddedPersistService("testClient", "testDatabase", true);
		service.createDatabase("testClient");
		Model.setLogger(logger);
		Model.setPersistServiceProvider(new SimplePersistServiceProvider(service));
	}

	@After
	public void tearDown() throws SQLException {
		Model.setLogger(null);
		Model.setPersistServiceProvider(null);
		service.dropDatabase("testClient");
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
			throw new IllegalStateException("error during setup", e);
		}
	}
	
}
