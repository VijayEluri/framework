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

import static org.junit.Assert.*;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class LoadTests extends BaseDbTestCase {

	@Test
	public void testLoad() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = am.newInstance();
		a.setId(1);
		
		assertTrue(a.isEmpty());
		
		a.load();
		
		assertTrue(a.isSet("name"));
		assertEquals("nameA1", a.peek("name"));
		assertTrue(a.isSet("bModel"));
		assertEquals(bm.getModelClass(), a.peek("bModel").getClass());
		assertTrue(((Model) a.peek("bModel")).isEmpty());
	}
	
	@Test
	public void testLoadFields() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = am.newInstance();
		a.setId(1);
		
		assertTrue(a.isEmpty());
		
		a.load("bModel");
		
		assertFalse(a.isSet("name"));
		assertTrue(a.isSet("bModel"));
		assertEquals(bm.getModelClass(), a.peek("bModel").getClass());
		assertTrue(((Model) a.peek("bModel")).isSet("name"));
		assertTrue(((Model) a.peek("bModel")).isSet("cModel"));
		assertFalse(a.isSet("cModel"));

		// make sure that it does overwrite loaded fields, but not existing ones
		Object b = a.get("bModel");
		a.set("bTest", b);
		
		a.load("bModel, cModel");
		
		assertNotSame(b, a.peek("bModel"));
		assertSame(b, a.peek("bTest"));
		assertTrue(a.isSet("cModel"));
	}
	
	@Test
	public void testLoadFieldsWithInclude() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = am.newInstance();
		a.setId(1);
		
		assertTrue(a.isEmpty());
		
		a.load("bModel:cModel");
		
		assertFalse(a.isSet("name"));
		assertTrue(a.isSet("bModel"));
		assertEquals(bm.getModelClass(), a.peek("bModel").getClass());
		assertTrue(((Model) a.peek("bModel")).isSet("cModel"));
		assertFalse(a.isSet("cModel"));

		a = am.newInstance();
		a.setId(1);
		
		assertTrue(a.isEmpty());
		
		a.load("bModel:cModel,cModel");
		
		assertFalse(a.isSet("name"));
		assertTrue(a.isSet("bModel"));
		assertEquals(bm.getModelClass(), a.peek("bModel").getClass());
		assertTrue(((Model) a.peek("bModel")).isSet("cModel"));
		assertTrue(a.isSet("cModel"));
	}
	
	@Test
	public void testLoadWithInclude() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = am.newInstance();
		a.setId(1);
		a.load("include : bModel");
		
		assertEquals("nameA1", a.peek("name"));
		assertNotNull(a.peek("bModel"));
		assertEquals("nameB1", ((Model) a.peek("bModel")).peek("name"));
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(((Model) a.peek("bModel")).peek("cModel"));
		assertFalse(((Model) ((Model) a.peek("bModel")).peek("cModel")).isSet("name"));
	}
	
}
