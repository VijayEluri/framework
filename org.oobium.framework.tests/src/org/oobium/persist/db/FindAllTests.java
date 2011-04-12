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

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.persist.Model;

public class FindAllTests extends BaseDbTestCase {

	@Test(expected=SQLException.class)
	public void testFindAllInvalidSQL() throws Exception {
		DynModel am = DynClasses.getModel("AModel");
		persistService.findAll(am.getModelClass(), "blah");
	}
	
	@Test
	public void testFindAllWithLimit() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA3");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "limit 2");

		assertEquals(2, models.size());
		assertEquals("nameA1", models.get(0).get("name"));
		assertEquals("nameA2", models.get(1).get("name"));
	}
	
	@Test
	public void testFindAllWithOrder() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "order by name desc");
		
		assertEquals(2, models.size());
		assertEquals("nameA2", models.get(0).get("name"));
		assertEquals("nameA1", models.get(1).get("name"));
	}
	
	@Test
	public void testFindAllWithValue() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA3");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "where name=?", "nameA2");
		
		assertEquals(1, models.size());
		assertEquals("nameA2", models.get(0).get("name"));
	}
	
	@Test
	public void testFindAllSingleArg() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass());
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));
		
		// BModel should exist but not be resolved
		assertNotNull(model.get("bModel"));
		assertFalse(((Model) model.get("bModel")).isSet("bName"));
		assertFalse(((Model) model.get("bModel")).isSet("cName"));
	}
	
	@Test
	public void testFindAllEmpty() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "");
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));
		
		// BModel should exist but not be resolved
		assertNotNull(model.get("name"));
		assertFalse(((Model) model.get("bModel")).isSet("bName"));
		assertFalse(((Model) model.get("bModel")).isSet("cName"));
	}
	
	@Test
	public void testFindAllIncludeQO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:bModel");
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));
		assertNotNull(model.get("bModel"));
		assertEquals("nameB1", ((Model) model.get("bModel")).get("name"));
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(((Model) model.get("bModel")).get("cModel"));
		assertFalse(((Model) ((Model) model.get("bModel")).get("cModel")).isSet("cName"));
	}
	
	@Test
	public void testFindAllIncludeQO1Null() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:bModel");
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));
		assertNull(model.get("bModel"));
	}
	
	@Test
	public void testFindAllIncludeQO2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:{bModel:cModel}");
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));
		assertNotNull(model.get("bModel"));
		assertEquals("nameB1", ((Model) model.get("bModel")).get("name"));
		assertNotNull(((Model) model.get("bModel")).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) model.get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindAllIncludeQO1QO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:[bModel,cModel]");
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));

		assertNotNull(model.get("bModel"));
		assertEquals("nameB1", ((Model) model.get("bModel")).get("name"));
		
		assertNotNull(model.get("cModel"));
		assertEquals("nameC1", ((Model) model.get("cModel")).get("name"));
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(((Model) model.get("bModel")).get("cModel"));
		assertFalse(((Model) ((Model) model.get("bModel")).get("cModel")).isSet("cName"));
	}

	@Test
	public void testFindAllIncludeQO2QO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:[{bModel:cModel},cModel]");
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));

		assertNotNull(model.get("bModel"));
		assertEquals("nameB1", ((Model) model.get("bModel")).get("name"));
		
		assertNotNull(model.get("cModel"));
		assertEquals("nameC1", ((Model) model.get("cModel")).get("name"));
		
		assertNotNull(((Model) model.get("bModel")).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) model.get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindAllIncludeQO1QO2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasOne("cModel", "CModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:[cModel,{bModel:cModel}]");
		
		assertEquals(1, models.size());
		
		Model model = models.get(0);
		
		assertEquals("nameA1", model.get("name"));

		assertNotNull(model.get("bModel"));
		assertEquals("nameB1", ((Model) model.get("bModel")).get("name"));
		
		assertNotNull(model.get("cModel"));
		assertEquals("nameC1", ((Model) model.get("cModel")).get("name"));
		
		assertNotNull(((Model) model.get("bModel")).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) model.get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindAllIncludeQM1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class");
		
		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB2");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB3");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB4");
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 2, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 3, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 4, 2);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:bModels");
		
		assertEquals(2, models.size());
		
		assertEquals("nameA1", models.get(0).get("name"));
		assertEquals("nameA2", models.get(1).get("name"));

		assertEquals(1, ((Collection<?>) models.get(0).get("bModels")).size());
		assertEquals("nameB1", ((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("name"));
		
		assertEquals(3, ((Collection<?>) models.get(1).get("bModels")).size());
		for(Object m : ((Collection<?>) models.get(1).get("bModels"))) {
			Model model = (Model) m;
			assertTrue("nameB2".equals(model.get("name")) || "nameB3".equals(model.get("name")) || "nameB4".equals(model.get("name")));
		}
	}

	@Test
	public void testFindAllIncludeQM1QO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class");
		
		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB2");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB3");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameB4");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 2);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 2, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 3, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 4, 2);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:[bModels,bModel]");
		
		assertEquals(2, models.size());
		
		assertEquals("nameA1", models.get(0).get("name"));
		assertEquals("nameA2", models.get(1).get("name"));

		assertNotNull(models.get(0).get("bModel"));
		assertEquals("nameB2", ((Model) models.get(0).get("bModel")).get("name"));

		assertEquals(1, ((Collection<?>) models.get(0).get("bModels")).size());
		assertEquals("nameB1", ((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("name"));
		
		assertEquals(3, ((Collection<?>) models.get(1).get("bModels")).size());
		for(Object m : ((Collection<?>) models.get(1).get("bModels"))) {
			Model model = (Model) m;
			assertTrue("nameB2".equals(model.get("name")) || "nameB3".equals(model.get("name")) || "nameB4".equals(model.get("name")));
		}
	}

	@Test
	public void testFindAllIncludeQM2() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:{bModels:cModel}");
		
		assertEquals(1, models.size());
		
		assertEquals("nameA1", models.get(0).get("name"));

		assertEquals(1, ((Collection<?>) models.get(0).get("bModels")).size());
		assertEquals("nameB1", ((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("name"));
		
		assertNotNull(((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("cModel")).get("name"));
	}

	@Test
	public void testFindAllIncludeQM2QO2() throws Exception {
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

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:[{bModels:cModel},{bModel:cModel}]");
		
		assertEquals(1, models.size());
		
		assertEquals("nameA1", models.get(0).get("name"));

		assertEquals(1, ((Collection<?>) models.get(0).get("bModels")).size());
		assertEquals("nameB1", ((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("name"));
		
		assertNotNull(((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("cModel"));
		assertEquals("nameC1", ((Model) ((Model) ((Collection<?>) models.get(0).get("bModels")).iterator().next()).get("cModel")).get("name"));

		assertNotNull(models.get(0).get("bModel"));
		assertEquals("nameB2", ((Model) models.get(0).get("bModel")).get("name"));

		assertNotNull(((Model) models.get(0).get("bModel")).get("cModel"));
		assertEquals("nameC2", ((Model) ((Model) models.get(0).get("bModel")).get("cModel")).get("name"));
	}

	@Test
	public void testFindAllIncludeQMM1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasMany("bModels", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");
		
		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameD1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameD2");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameE1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameE2");
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 2);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:bModels");
		
		assertEquals(2, models.size());
		assertEquals("nameD1", models.get(0).get("name"));
		assertEquals("nameD2", models.get(1).get("name"));

		assertEquals(2, ((Collection<?>) models.get(0).get("bModels")).size());
		assertEquals(2, ((Collection<?>) models.get(1).get("bModels")).size());
		
		for(Object m : ((Collection<?>) models.get(0).get("bModels"))) {
			Model model = (Model) m;
			assertTrue("nameE1".equals(model.get("name")) || "nameE2".equals(model.get("name")));
		}

		for(Object m : ((Collection<?>) models.get(1).get("bModels"))) {
			Model model = (Model) m;
			assertTrue("nameE1".equals(model.get("name")) || "nameE2".equals(model.get("name")));
		}
	}

	@Test
	public void testFindAllIncludeQMO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");
		
		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameF1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameF2");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameD1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameD2", 2);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameD3", 2);

		List<? extends Model> models = persistService.findAll(bm.getModelClass(), "include:aModels");
		
		assertEquals(2, models.size());
		assertEquals("nameF1", models.get(0).get("name"));
		assertEquals("nameF2", models.get(1).get("name"));

		assertEquals(1, ((Collection<?>) models.get(0).get("aModels")).size());
		assertEquals("nameD1", ((Model) ((Collection<?>) models.get(0).get("aModels")).iterator().next()).get("name"));
		
		assertEquals(2, ((Collection<?>) models.get(1).get("aModels")).size());
		for(Object m : ((Collection<?>) models.get(1).get("aModels"))) {
			Model model = (Model) m;
			assertTrue("nameD2".equals(model.get("name")) || "nameD3".equals(model.get("name")));
		}
	}

	@Test
	public void testFindAllIncludeQOM1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");
		
		migrate(am, bm);
		
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameF1");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "nameF2");
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameD1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameD2", 2);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameD3", 2);

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "include:bModel");
		
		assertEquals(3, models.size());
		assertEquals("nameD1", models.get(0).get("name"));
		assertEquals("nameD2", models.get(1).get("name"));
		assertEquals("nameD3", models.get(2).get("name"));
		
		assertNotNull(models.get(0).get("bModel"));
		assertEquals("nameF1", ((Model) models.get(0).get("bModel")).get("name"));
		
		assertNotNull(models.get(1).get("bModel"));
		assertEquals("nameF2", ((Model) models.get(1).get("bModel")).get("name"));
		
		assertNotNull(models.get(1).get("bModel"));
		assertEquals("nameF2", ((Model) models.get(1).get("bModel")).get("name"));
	}

}
