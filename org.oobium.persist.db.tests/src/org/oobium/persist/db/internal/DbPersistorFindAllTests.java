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
package org.oobium.persist.db.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.oobium.persist.db.internal.QueryBuilder;
import org.oobium.persist.db.tests.DbPersistorTestCase;
import org.oobium.persist.db.tests.models.AModel;
import org.oobium.persist.db.tests.models.BModel;
import org.oobium.persist.db.tests.models.DModel;
import org.oobium.persist.db.tests.models.EModel;
import org.oobium.persist.db.tests.models.FModel;

public class DbPersistorFindAllTests extends DbPersistorTestCase {

	@Test
	public void testBuilder() throws Exception {
		QueryBuilder.build(AModel.class, "");
		QueryBuilder.build(AModel.class, "include:bModel");
		QueryBuilder.build(AModel.class, "include:bModels");
		QueryBuilder.build(AModel.class, "include:{bModel:cModel}");
		QueryBuilder.build(AModel.class, "include:{bModels:cModel}");
		QueryBuilder.build(AModel.class, "include:[bModel,bModels]");
		QueryBuilder.build(AModel.class, "include:[bModel,{bModels:cModel}]");
		QueryBuilder.build(AModel.class, "include:[{bModel:cModel},{bModels:cModel}]");
	}
	
	@Test(expected=SQLException.class)
	public void testFindAllInvalid() throws Exception {
		service.findAll(AModel.class, "blah");
	}
	
	@Test
	public void testFindAllWithLimit() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA2");
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA3");

		List<AModel> models = service.findAll(AModel.class, "limit 2");

		assertEquals(2, models.size());
		assertEquals("nameA1", models.get(0).getAName());
		assertEquals("nameA2", models.get(1).getAName());
	}
	
	@Test
	public void testFindAllWithOrder() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA2");

		List<AModel> models = service.findAll(AModel.class, "order by a_name desc");
		
		assertEquals(2, models.size());
		assertEquals("nameA2", models.get(0).getAName());
		assertEquals("nameA1", models.get(1).getAName());
	}
	
	@Test
	public void testFindAllWithValue() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");

		List<AModel> models = service.findAll(AModel.class, "where a_name=?", "nameA1");
		
		assertEquals(1, models.size());
		assertEquals("nameA1", models.get(0).getAName());
	}
	
	@Test
	public void testFindAllSingleArg() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class);
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());
		
		// BModel should exist but not be resolved
		assertNotNull(model.getBModel());
		assertFalse(model.getBModel().isSet("bName"));
		assertFalse(model.getBModel().isSet("cName"));
	}
	
	@Test
	public void testFindAllEmpty() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "");
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());
		
		// BModel should exist but not be resolved
		assertNotNull(model.getBModel());
		assertFalse(model.getBModel().isSet("bName"));
		assertFalse(model.getBModel().isSet("cName"));
	}
	
	@Test
	public void testFindAllIncludeQO1() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "include:bModel");
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());
		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(model.getBModel().getCModel());
		assertFalse(model.getBModel().getCModel().isSet("cName"));
	}
	
	@Test
	public void testFindAllIncludeQO1Null() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "include:bModel");
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());
		assertNull(model.getBModel());
	}
	
	@Test
	public void testFindAllIncludeQO2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "include:{bModel:cModel}");
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());
		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		assertNotNull(model.getBModel().getCModel());
		assertEquals("nameC1", model.getBModel().getCModel().getCName());
	}

	@Test
	public void testFindAllIncludeQO1QO1() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "include:[bModel,cModel]");
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());

		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		assertNotNull(model.getCModel());
		assertEquals("nameC1", model.getCModel().getCName());
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(model.getBModel().getCModel());
		assertFalse(model.getBModel().getCModel().isSet("cName"));
	}

	@Test
	public void testFindAllIncludeQO2QO1() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "include:[{bModel:cModel},cModel]");
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());

		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		assertNotNull(model.getCModel());
		assertEquals("nameC1", model.getCModel().getCName());
		
		assertNotNull(model.getBModel().getCModel());
		assertEquals("nameC1", model.getBModel().getCModel().getCName());
	}

	@Test
	public void testFindAllIncludeQO1QO2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "include:[cModel,{bModel:cModel}]");
		
		assertEquals(1, models.size());
		
		AModel model = models.get(0);
		
		assertEquals("nameA1", model.getAName());

		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		assertNotNull(model.getCModel());
		assertEquals("nameC1", model.getCModel().getCName());
		
		assertNotNull(model.getBModel().getCModel());
		assertEquals("nameC1", model.getBModel().getCModel().getCName());
	}

	@Test
	public void testFindAllIncludeQM1() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB3");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB4");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 2);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 3, 2);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 4, 2);

		List<AModel> models = service.findAll(AModel.class, "include:bModels");
		
		assertEquals(2, models.size());
		
		assertEquals("nameA1", models.get(0).getAName());
		assertEquals("nameA2", models.get(1).getAName());

		assertEquals(1, models.get(0).bModels().size());
		assertEquals("nameB1", models.get(0).bModels().iterator().next().getBName());
		
		assertEquals(3, models.get(1).bModels().size());
		for(BModel model : models.get(1).bModels()) {
			logger.debug(model.getBName());
			assertTrue("nameB2".equals(model.getBName()) || "nameB3".equals(model.getBName()) || "nameB4".equals(model.getBName()));
		}
	}

	@Test
	public void testFindAllIncludeQM1QO1() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 2);
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB3");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB4");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 2);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 3, 2);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 4, 2);

		List<AModel> models = service.findAll(AModel.class, "include:[bModels,bModel]");
		
		assertEquals(2, models.size());
		
		assertEquals("nameA1", models.get(0).getAName());
		assertEquals("nameA2", models.get(1).getAName());

		assertNotNull(models.get(0).getBModel());
		assertEquals("nameB2", models.get(0).getBModel().getBName());

		assertEquals(1, models.get(0).bModels().size());
		assertEquals("nameB1", models.get(0).bModels().iterator().next().getBName());
		
		assertEquals(3, models.get(1).bModels().size());
		for(BModel model : models.get(1).bModels()) {
			logger.debug(model.getBName());
			assertTrue("nameB2".equals(model.getBName()) || "nameB3".equals(model.getBName()) || "nameB4".equals(model.getBName()));
		}
	}

	@Test
	public void testFindAllIncludeQM2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		List<AModel> models = service.findAll(AModel.class, "include:{bModels:cModel}");
		
		assertEquals(1, models.size());
		
		assertEquals("nameA1", models.get(0).getAName());

		assertEquals(1, models.get(0).bModels().size());
		assertEquals("nameB1", models.get(0).bModels().iterator().next().getBName());
		
		assertNotNull(models.get(0).bModels().iterator().next().getCModel());
		assertEquals("nameC1", models.get(0).bModels().iterator().next().getCModel().getCName());
	}

	@Test
	public void testFindAllIncludeQM2QO2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 2);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB2", 2);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC2");

		List<AModel> models = service.findAll(AModel.class, "include:[{bModels:cModel},{bModel:cModel}]");
		
		assertEquals(1, models.size());
		
		assertEquals("nameA1", models.get(0).getAName());

		assertEquals(1, models.get(0).bModels().size());
		assertEquals("nameB1", models.get(0).bModels().iterator().next().getBName());
		
		assertNotNull(models.get(0).bModels().iterator().next().getCModel());
		assertEquals("nameC1", models.get(0).bModels().iterator().next().getCModel().getCName());

		assertNotNull(models.get(0).getBModel());
		assertEquals("nameB2", models.get(0).getBModel().getBName());

		assertNotNull(models.get(0).getBModel().getCModel());
		assertEquals("nameC2", models.get(0).getBModel().getCModel().getCName());
	}

	@Test
	public void testFindAllIncludeQMM1() throws Exception {
		createDatabase("de");
		
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD1");
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD2");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE2");
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 2);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 2);

		List<DModel> models = service.findAll(DModel.class, "include:eModels");
		
		assertEquals(2, models.size());
		assertEquals("nameD1", models.get(0).getDName());
		assertEquals("nameD2", models.get(1).getDName());

		assertEquals(2, models.get(0).eModels().size());
		assertEquals(2, models.get(1).eModels().size());
		
		for(EModel model : models.get(0).eModels()) {
			String name = model.getEName();
			logger.debug(name);
			assertTrue("nameE1".equals(name) || "nameE2".equals(name));
		}

		for(EModel model : models.get(1).eModels()) {
			String name = model.getEName();
			logger.debug(name);
			assertTrue("nameE1".equals(name) || "nameE2".equals(name));
		}
	}

	@Test
	public void testFindAllIncludeQMO1() throws Exception {
		createDatabase("fd");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF2");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 2);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 2);

		List<FModel> models = service.findAll(FModel.class, "include:dModels");
		
		assertEquals(2, models.size());
		assertEquals("nameF1", models.get(0).getFName());
		assertEquals("nameF2", models.get(1).getFName());

		assertEquals(1, models.get(0).dModels().size());
		assertEquals("nameD1", models.get(0).dModels().iterator().next().getDName());
		
		assertEquals(2, models.get(1).dModels().size());
		for(DModel model : models.get(1).dModels()) {
			String name = model.getDName();
			logger.debug(name);
			assertTrue("nameD2".equals(name) || "nameD3".equals(name));
		}
	}

	@Test
	public void testFindAllIncludeQOM1() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 2);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 2);
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF2");

		List<DModel> models = service.findAll(DModel.class, "include:fModel");
		
		assertEquals(3, models.size());
		assertEquals("nameD1", models.get(0).getDName());
		assertEquals("nameD2", models.get(1).getDName());
		assertEquals("nameD3", models.get(2).getDName());
		
		assertNotNull(models.get(0).getFModel());
		assertEquals("nameF1", models.get(0).getFModel().getFName());
		
		assertNotNull(models.get(1).getFModel());
		assertEquals("nameF2", models.get(1).getFModel().getFName());
		
		assertNotNull(models.get(1).getFModel());
		assertEquals("nameF2", models.get(1).getFModel().getFName());
	}

}
