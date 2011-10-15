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

import java.util.List;

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
	public void testLoadFields1() throws Exception {
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
	public void testLoadFields2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("cModels", "CModel.class", "opposite=\"bModel\"");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "opposite=\"cModels\"");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO c_models(name,b_model) VALUES(?,?)", "nameC1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA2", 1);

		Model b = bm.newInstance();
		b.setId(1);
		
		assertTrue(b.isEmpty());
		
		b.load("cModels");
		
		assertFalse(b.isSet("name"));
		assertTrue(b.isSet("cModels"));
		assertTrue(b.peek("cModels") instanceof List<?>);
		assertFalse(((List<?>) b.peek("cModels")).isEmpty());
		assertEquals(cm.getModelClass(), ((List<?>) b.peek("cModels")).get(0).getClass());
		assertFalse(((Model) ((List<?>) b.peek("cModels")).get(0)).isEmpty());
	}
	
	@Test
	public void testLoadFieldsWithInclude1() throws Exception {
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
	public void testLoadFieldsWithInclude2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("cModels", "CModel.class", "opposite=\"bModel\"");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "opposite=\"cModels\"");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO c_models(name,b_model) VALUES(?,?)", "nameC1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA2", 1);

		Model a = am.newInstance();
		a.setId(2);
		
		assertTrue(a.isEmpty());
		
		a.load("bModel:cModels");
		
		assertFalse(a.isSet("name"));
		assertTrue(a.isSet("bModel"));
		assertEquals(bm.getModelClass(), a.peek("bModel").getClass());
		assertTrue(((Model) a.peek("bModel")).isSet("cModels"));
	}
	
	@Test
	public void testLoadFieldsWithInclude3() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("cModels", "CModel.class", "opposite=\"bModel\"");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class")
													.addHasOne("bModel", "BModel.class", "opposite=\"cModels\"")
													.addHasOne("dModel", "DModel.class", "opposite=\"cModels\"");
		DynModel dm = DynClasses.getModel("DModel").addAttr("name", "String.class").addHasMany("cModels", "CModel.class", "opposite=\"dModel\"");
		
		migrate(am, bm, cm, dm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO d_models(name) VALUES(?)", "nameD1");
		persistService.executeUpdate("INSERT INTO c_models(name,b_model,d_model) VALUES(?,?,?)", "nameC1", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA2", 1);

		Model a = am.newInstance();
		a.setId(2);
		
		assertTrue(a.isEmpty());
		
		a.load("bModel:cModels:dModel");
		
		assertFalse(a.isSet("name"));
		assertTrue(a.isSet("bModel"));
		assertEquals(bm.getModelClass(), a.peek("bModel").getClass());

		assertTrue(((Model) a.peek("bModel")).isSet("cModels"));
		assertTrue(((Model) a.peek("bModel")).peek("cModels") instanceof List<?>);
		assertFalse(((List<?>) ((Model) a.peek("bModel")).peek("cModels")).isEmpty());
		assertEquals(cm.getModelClass(), ((List<?>) ((Model) a.peek("bModel")).peek("cModels")).get(0).getClass());
		assertFalse(((Model) ((List<?>) ((Model) a.peek("bModel")).peek("cModels")).get(0)).isEmpty());
		
		assertTrue(((Model) ((List<?>) ((Model) a.peek("bModel")).peek("cModels")).get(0)).isSet("dModel"));
		assertEquals(dm.getModelClass(), ((Model) ((List<?>) ((Model) a.peek("bModel")).peek("cModels")).get(0)).peek("dModel").getClass());
		assertTrue(((Model) ((Model) ((List<?>) ((Model) a.peek("bModel")).peek("cModels")).get(0)).peek("dModel")).isSet("name"));
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
