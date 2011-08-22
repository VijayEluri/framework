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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class FindTests extends BaseDbTestCase {

	@Test
	public void testFindById() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");

		Model a = persistService.findById(am.getModelClass(), 1);
		assertNotNull(a);
		assertEquals("nameA1", a.get("name"));
	}
	
	@Test(expected=Exception.class)
	public void testFindInvalid() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		persistService.find(am.getModelClass(), "blah");
	}
	
	@Test
	public void testFindWithNestedAutoIncludeHasOne() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "include=true");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class", "include=true");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model model = persistService.findById(am.getModelClass(), 1);

		assertNotNull(model);
		assertTrue(model.isSet("bModel"));
		assertTrue(((Model) model.get("bModel")).isSet("name"));
		assertEquals("nameB1", ((Model) model.get("bModel")).get("name"));
		assertTrue(((Model) model.get("bModel")).isSet("cModel"));
		assertTrue(((Model) ((Model) model.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) model.get("bModel")).get("cModel")).get("name"));
	}
	
	@Test
	public void testFindIncludeQO1AndNestedAutoIncludeHasOne() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("includedBModel", "BModel.class", "include=true");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class", "include=true");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB2", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model, included_b_model) VALUES(?,?,?)", "nameA1", 1, 2);

		Model model = persistService.find(am.getModelClass(), "include:bModel");

		assertNotNull(model);

		assertTrue(model.isSet("bModel"));
		assertTrue(((Model) model.get("bModel")).isSet("name"));
		assertEquals("nameB1", ((Model) model.get("bModel")).get("name"));
		assertTrue(model.isSet("includedBModel"));
		assertTrue(((Model) model.get("includedBModel")).isSet("name"));
		assertEquals("nameB2", ((Model) model.get("includedBModel")).get("name"));

		assertTrue(((Model) model.get("bModel")).isSet("cModel"));
		assertTrue(((Model) ((Model) model.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) model.get("bModel")).get("cModel")).get("name"));
		assertTrue(((Model) model.get("includedBModel")).isSet("cModel"));
		assertTrue(((Model) ((Model) model.get("includedBModel")).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) model.get("includedBModel")).get("cModel")).get("name"));
	}
	
	@Test
	public void testFindIncludeQO2AndNestedAutoIncludeHasOne() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class").addHasOne("includedCModel", "CModel.class", "include=true");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC2");
		persistService.executeUpdate("INSERT INTO b_models(name,included_c_model,c_model) VALUES(?,?,?)", "nameB1", 1, 2);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.find(am.getModelClass(), "include:{bModel:cModel}");

		assertNotNull(a);
		assertTrue(a.isSet("bModel"));
		assertNotNull(a.get("bModel"));
		assertTrue(((Model) a.get("bModel")).isSet("name"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		assertTrue(((Model) a.get("bModel")).isSet("includedCModel"));
		assertNotNull(((Model) a.get("bModel")).get("includedCModel"));
		assertTrue(((Model) ((Model) a.get("bModel")).get("includedCModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("includedCModel")).get("name"));
		assertTrue(((Model) a.get("bModel")).isSet("cModel"));
		assertTrue(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC2", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
	}
	
	@Test
	public void testFindIncludeDuplicateQO1NestedAutoIncludeHasOne() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "include=true");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class", "include=true");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.find(am.getModelClass(), "include:bModel");

		assertNotNull(a);
		assertTrue(a.isSet("bModel"));
		assertNotNull(a.get("bModel"));
		assertTrue(((Model) a.get("bModel")).isSet("name"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		assertTrue(((Model) a.get("bModel")).isSet("cModel"));
		assertTrue(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
	}
	
	@Test
	public void testFindIncludeDuplicateQO2NestedAutoIncludeHasOne() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "include=true");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class", "include=true");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.find(am.getModelClass(), "include:{bModel:cModel}");

		assertNotNull(a);
		assertTrue(a.isSet("bModel"));
		assertNotNull(a.get("bModel"));
		assertTrue(((Model) a.get("bModel")).isSet("name"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		assertTrue(((Model) a.get("bModel")).isSet("cModel"));
		assertTrue(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
	}
	
	@Test
	public void testFindWithLimit() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA3");

		Model a = persistService.find(am.getModelClass(), "limit 2");
		assertNotNull(a);
		assertEquals("nameA1", a.get("name"));
	}
	
	@Test
	public void testFindWithOrder() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");

		Model a = persistService.find(am.getModelClass(), "order by name desc");
		assertNotNull(a);
		assertEquals("nameA2", a.get("name"));
	}
	
	@Test
	public void testFindWithValue() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");

		Model a = persistService.find(am.getModelClass(), "where name=?", "nameA1");
		assertNotNull(a);
		assertEquals("nameA1", a.get("name"));
	}
	
	@Test
	public void testFindEmpty() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.find(am.getModelClass(), "");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));
		
		// BModel should exist but not be resolved
		assertNotNull(a.get("bModel"));
		assertFalse(((Model) a.get("bModel")).isSet("name"));

		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertFalse(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
		assertTrue(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name")); // set by the previous get
	}
	
	@Test
	public void testFindIncludeQO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.find(am.getModelClass(), "include:bModel");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));
		assertNotNull(a.get("bModel"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertFalse(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
	}
	
	@Test
	public void testFindIncludeQO1ByValue() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.find(am.getModelClass(), "include:?", "bModel");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));
		assertNotNull(a.get("bModel"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertFalse(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
		assertTrue(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name")); // set by the previous get
	}
	
	@Test
	public void testFindIncludeQO1Null() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		
		Model a = persistService.find(am.getModelClass(), "include:bModel");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));
		assertNull(a.get("bModel"));
	}
	
	@Test
	public void testFindIncludeQO2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.find(am.getModelClass(), "include:{bModel:cModel}");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));
		assertNotNull(a.get("bModel"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindIncludeQO1QO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		Model a = persistService.find(am.getModelClass(), "include:[bModel,cModel]");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertNotNull(a.get("bModel"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		
		assertNotNull(a.get("cModel"));
		assertEquals("nameC1", ((Model) a.get("cModel")).get("name"));
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertFalse(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
	}

	@Test
	public void testFindIncludeQO2QO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		Model a = persistService.find(am.getModelClass(), "include:[{bModel:cModel},cModel]");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertNotNull(a.get("bModel"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		
		assertNotNull(a.get("cModel"));
		assertEquals("nameC1", ((Model) a.get("cModel")).get("name"));
		
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindIncludeQO1QO2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		Model a = persistService.find(am.getModelClass(), "include:[cModel,{bModel:cModel}]");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertNotNull(a.get("bModel"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		
		assertNotNull(a.get("cModel"));
		assertEquals("nameC1", ((Model) a.get("cModel")).get("name"));
		
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindIncludeQM1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB2");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB3");
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 2, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 3, 1);

		Model a = persistService.find(am.getModelClass(), "include:bModels");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertEquals(3, ((Collection<?>) a.get("bModels")).size());
		for(Object o : (Collection<?>) a.get("bModels")) {
			Model b = (Model) o;
			assertTrue("nameB1".equals(b.get("name")) || "nameB2".equals(b.get("name")) || "nameB3".equals(b.get("name")));
		}
	}

	@Test
	public void testFindIncludeQM1QO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class");
		
		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB2");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB3");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 2, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 3, 1);

		Model a = persistService.find(am.getModelClass(), "include:[bModels,bModel]");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertTrue(a.isSet("bModel"));
		assertNotNull(a.get("bModel"));
		assertTrue(((Model) a.get("bModel")).isSet("name"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		
		assertTrue(a.isSet("bModels"));
		assertEquals(3, ((Collection<?>) a.get("bModels")).size());
		for(Object o : (Collection<?>) a.get("bModels")) {
			Model b = (Model) o;
			assertTrue(b.isSet("name"));
			assertTrue("nameB1".equals(b.get("name")) || "nameB2".equals(b.get("name")) || "nameB3".equals(b.get("name")));
		}
	}

	@Test
	public void testFindIncludeQM1QO1_EmptyResults() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class");
		
		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");

		Model a = persistService.find(am.getModelClass(), "include:[bModels,bModel]");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertTrue(a.isSet("bModel"));
		assertNull(a.get("bModel"));
		
		assertTrue(a.isSet("bModels"));
		assertTrue(((Collection<?>) a.get("bModels")).isEmpty());
	}

	@Test
	public void testFindIncludeQM2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);

		Model model = persistService.find(am.getModelClass(), "include:{bModels:cModel}");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.get("name"));

		assertTrue(model.isSet("bModels"));
		assertEquals(1, ((Collection<?>) model.get("bModels")).size());
		assertTrue(((Model) ((Collection<?>) model.get("bModels")).iterator().next()).isSet("name"));
		assertEquals("nameB1", ((Model) ((Collection<?>) model.get("bModels")).iterator().next()).get("name"));
		
		assertTrue(((Model) ((Collection<?>) model.get("bModels")).iterator().next()).isSet("cModel"));
		assertNotNull(((Model) ((Collection<?>) model.get("bModels")).iterator().next()).get("cModel"));
		assertTrue(((Model) ((Model) ((Collection<?>) model.get("bModels")).iterator().next()).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) ((Collection<?>) model.get("bModels")).iterator().next()).get("cModel")).get("name"));
	}

	@Test
	public void testFindIncludeQM2QO2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC2");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB2", 2);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);

		Model a = persistService.find(am.getModelClass(), "include:[{bModels:cModel},{bModel:cModel}]");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertTrue(a.isSet("bModels"));
		assertEquals(1, ((Collection<?>) a.get("bModels")).size());
		assertTrue(((Model) ((Collection<?>) a.get("bModels")).iterator().next()).isSet("name"));
		assertEquals("nameB1", ((Model) ((Collection<?>) a.get("bModels")).iterator().next()).get("name"));

		assertTrue(((Model) ((Collection<?>) a.get("bModels")).iterator().next()).isSet("cModel"));
		assertNotNull(((Model) ((Collection<?>) a.get("bModels")).iterator().next()).get("cModel"));
		assertTrue(((Model) ((Model) ((Collection<?>) a.get("bModels")).iterator().next()).get("cModel")).isSet("name"));
		assertEquals("nameC1", ((Model) ((Model) ((Collection<?>) a.get("bModels")).iterator().next()).get("cModel")).get("name"));

		assertTrue(a.isSet("bModel"));
		assertNotNull(a.get("bModel"));
		assertTrue(((Model) a.get("bModel")).isSet("name"));
		assertEquals("nameB2", ((Model) a.get("bModel")).get("name"));

		assertTrue(((Model) a.get("bModel")).isSet("cModel"));
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertTrue(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
		assertEquals("nameC2", ((Model) ((Model) a.get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindIncludeQMM1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasMany("bModels", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");

		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB2");
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 2);

		Model a = persistService.find(am.getModelClass(), "include:bModels");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));

		assertEquals(2, ((Collection<?>) a.get("bModels")).size());
		
		for(Object o : (Collection<?>) a.get("bModels")) {
			String name = (String) ((Model) o).get("name");
			assertTrue("nameB1".equals(name) || "nameB2".equals(name));
		}
	}

	@Test
	public void testFindIncludeQMO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB2");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA2", 2);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA3", 2);

		Model b = persistService.find(bm.getModelClass(), "include:aModels");
		
		assertNotNull(b);
		
		assertEquals("nameB1", b.get("name"));

		assertTrue(b.isSet("aModels"));
		assertEquals(1, ((Collection<?>) b.get("aModels")).size());
		assertTrue(((Model) ((Collection<?>) b.get("aModels")).iterator().next()).isSet("name"));
		assertEquals("nameA1", ((Model) ((Collection<?>) b.get("aModels")).iterator().next()).get("name"));
	}

	@Test
	public void testFindIncludeQOM1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB2");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA2", 2);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA3", 2);

		Model a = persistService.find(am.getModelClass(), "include:bModel");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));
		
		assertNotNull(a.get("bModel"));
		assertTrue(((Model) a.get("bModel")).isSet("name"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
	}

}
