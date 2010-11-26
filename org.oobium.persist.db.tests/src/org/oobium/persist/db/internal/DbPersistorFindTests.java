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

import org.junit.Test;
import org.oobium.persist.db.tests.DbPersistorTestCase;
import org.oobium.persist.db.tests.models.AModel;
import org.oobium.persist.db.tests.models.BModel;
import org.oobium.persist.db.tests.models.DModel;
import org.oobium.persist.db.tests.models.EModel;
import org.oobium.persist.db.tests.models.FModel;
import org.oobium.persist.db.tests.models.IModel;

public class DbPersistorFindTests extends DbPersistorTestCase {

	@Test
	public void testFindById() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");

		AModel amodel = service.find(AModel.class, 1);
		assertNotNull(amodel);
		assertEquals("nameA1", amodel.getAName());
	}
	
	@Test(expected=SQLException.class)
	public void testFindInvalid() throws Exception {
		service.find(AModel.class, "blah");
	}
	
	@Test
	public void testFindWithNestedAutoIncludeHasOne() throws Exception {
		createDatabase("bij");
		
		service.executeUpdate("INSERT INTO i_models(i_name,included_j_model) VALUES(?,?)", "nameI1", 1);
		service.executeUpdate("INSERT INTO j_models(j_name,included_b_model) VALUES(?,?)", "nameJ1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");

		IModel model = service.find(IModel.class, 1);

		assertNotNull(model);
		assertTrue(model.isSet("includedJModel"));
		assertTrue(model.getIncludedJModel().isSet("jName"));
		assertEquals("nameJ1", model.getIncludedJModel().getJName());
		assertTrue(model.getIncludedJModel().isSet("includedBModel"));
		assertTrue(model.getIncludedJModel().getIncludedBModel().isSet("bName"));
		assertEquals("nameB1", model.getIncludedJModel().getIncludedBModel().getBName());
	}
	
	@Test
	public void testFindIncludeQO1AndNestedAutoIncludeHasOne() throws Exception {
		createDatabase("bij");
		
		service.executeUpdate("INSERT INTO i_models(i_name,included_j_model, j_model) VALUES(?,?,?)", "nameI1", 1, 2);
		service.executeUpdate("INSERT INTO j_models(j_name,included_b_model) VALUES(?,?)", "nameJ1", 1);
		service.executeUpdate("INSERT INTO j_models(j_name,included_b_model) VALUES(?,?)", "nameJ2", 1);
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");

		IModel model = service.find(IModel.class, "include:jModel");

		assertNotNull(model);
		assertTrue(model.isSet("includedJModel"));
		assertTrue(model.getIncludedJModel().isSet("jName"));
		assertEquals("nameJ1", model.getIncludedJModel().getJName());
		assertTrue(model.isSet("jModel"));
		assertTrue(model.getJModel().isSet("jName"));
		assertEquals("nameJ2", model.getJModel().getJName());
		assertTrue(model.getIncludedJModel().isSet("includedBModel"));
		assertTrue(model.getIncludedJModel().getIncludedBModel().isSet("bName"));
		assertEquals("nameB1", model.getIncludedJModel().getIncludedBModel().getBName());
	}
	
	@Test
	public void testFindIncludeQO2AndNestedAutoIncludeHasOne() throws Exception {
		createDatabase("bij");
		
		service.executeUpdate("INSERT INTO i_models(i_name,included_j_model) VALUES(?,?)", "nameI1", 1);
		service.executeUpdate("INSERT INTO j_models(j_name,included_b_model, b_model) VALUES(?,?,?)", "nameJ1", 1, 2);
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");

		IModel model = service.find(IModel.class, "include:{includedJModel:bModel}");

		assertNotNull(model);
		assertTrue(model.isSet("includedJModel"));
		assertTrue(model.getIncludedJModel().isSet("jName"));
		assertEquals("nameJ1", model.getIncludedJModel().getJName());
		assertTrue(model.getIncludedJModel().isSet("includedBModel"));
		assertTrue(model.getIncludedJModel().getIncludedBModel().isSet("bName"));
		assertEquals("nameB1", model.getIncludedJModel().getIncludedBModel().getBName());
		assertTrue(model.getIncludedJModel().isSet("bModel"));
		assertTrue(model.getIncludedJModel().getBModel().isSet("bName"));
		assertEquals("nameB2", model.getIncludedJModel().getBModel().getBName());
	}
	
	@Test
	public void testFindIncludeDuplicateQO1NestedAutoIncludeHasOne() throws Exception {
		createDatabase("bij");
		
		service.executeUpdate("INSERT INTO i_models(i_name,included_j_model) VALUES(?,?)", "nameI1", 1);
		service.executeUpdate("INSERT INTO j_models(j_name,included_b_model) VALUES(?,?)", "nameJ1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");

		IModel model = service.find(IModel.class, "include:includedJModel");

		assertNotNull(model);
		assertTrue(model.isSet("includedJModel"));
		assertTrue(model.getIncludedJModel().isSet("jName"));
		assertEquals("nameJ1", model.getIncludedJModel().getJName());
		assertTrue(model.getIncludedJModel().isSet("includedBModel"));
		assertTrue(model.getIncludedJModel().getIncludedBModel().isSet("bName"));
		assertEquals("nameB1", model.getIncludedJModel().getIncludedBModel().getBName());
	}
	
	@Test
	public void testFindIncludeDuplicateQO2NestedAutoIncludeHasOne() throws Exception {
		createDatabase("bij");
		
		service.executeUpdate("INSERT INTO i_models(i_name,included_j_model) VALUES(?,?)", "nameI1", 1);
		service.executeUpdate("INSERT INTO j_models(j_name,included_b_model) VALUES(?,?)", "nameJ1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");

		IModel model = service.find(IModel.class, "include:{includedJModel:includedBModel}");

		assertNotNull(model);
		assertTrue(model.isSet("includedJModel"));
		assertTrue(model.getIncludedJModel().isSet("jName"));
		assertEquals("nameJ1", model.getIncludedJModel().getJName());
		assertTrue(model.getIncludedJModel().isSet("includedBModel"));
		assertTrue(model.getIncludedJModel().getIncludedBModel().isSet("bName"));
		assertEquals("nameB1", model.getIncludedJModel().getIncludedBModel().getBName());
	}
	
	@Test
	public void testFindWithLimit() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA2");
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA3");

		AModel amodel = service.find(AModel.class, "limit 2");
		assertNotNull(amodel);
		assertEquals("nameA1", amodel.getAName());
	}
	
	@Test
	public void testFindWithOrder() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA2");

		AModel amodel = service.find(AModel.class, "order by a_name desc");
		assertNotNull(amodel);
		assertEquals("nameA2", amodel.getAName());
	}
	
	@Test
	public void testFindWithValue() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");

		AModel amodel = service.find(AModel.class, "where a_name=?", "nameA1");
		assertNotNull(amodel);
		assertEquals("nameA1", amodel.getAName());
	}
	
	@Test
	public void testFindEmpty() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());
		
		// BModel should exist but not be resolved
		assertNotNull(model.getBModel());
		assertFalse(model.getBModel().isSet("bName"));

		assertEquals("nameB1", model.getBModel().getBName());
		assertNotNull(model.getBModel().getCModel());
		assertFalse(model.getBModel().getCModel().isSet("cName"));
		assertEquals("nameC1", model.getBModel().getCModel().getCName());
		assertTrue(model.getBModel().getCModel().isSet("cName"));
	}
	
	@Test
	public void testFindIncludeQO1() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:bModel");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());
		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(model.getBModel().getCModel());
		assertFalse(model.getBModel().getCModel().isSet("cName"));
	}
	
	@Test
	public void testFindIncludeQO1ByValue() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:?", "bModel");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());
		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(model.getBModel().getCModel());
		assertFalse(model.getBModel().getCModel().isSet("cName"));

		assertEquals("nameC1", model.getBModel().getCModel().getCName());
		assertTrue(model.getBModel().getCModel().isSet("cName"));
	}
	
	@Test
	public void testFindIncludeQO1Null() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:bModel");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());
		assertNull(model.getBModel());
	}
	
	@Test
	public void testFindIncludeQO2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:{bModel:cModel}");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());
		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		assertNotNull(model.getBModel().getCModel());
		assertEquals("nameC1", model.getBModel().getCModel().getCName());
	}

	@Test
	public void testFindIncludeQO1QO1() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:[bModel,cModel]");
		
		assertNotNull(model);
		
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
	public void testFindIncludeQO2QO1() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:[{bModel:cModel},cModel]");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());

		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		assertNotNull(model.getCModel());
		assertEquals("nameC1", model.getCModel().getCName());
		
		assertNotNull(model.getBModel().getCModel());
		assertEquals("nameC1", model.getBModel().getCModel().getCName());
	}

	@Test
	public void testFindIncludeQO1QO2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model,c_model) VALUES(?,?,?)", "nameA1", 1, 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:[cModel,{bModel:cModel}]");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());

		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		assertNotNull(model.getCModel());
		assertEquals("nameC1", model.getCModel().getCName());
		
		assertNotNull(model.getBModel().getCModel());
		assertEquals("nameC1", model.getBModel().getCModel().getCName());
	}

	@Test
	public void testFindIncludeQM1() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB3");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 3, 1);

		AModel model = service.find(AModel.class, "include:bModels");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());

		assertEquals(3, model.bModels().size());
		for(BModel bModel : model.bModels()) {
			logger.debug(bModel.getBName());
			assertTrue("nameB1".equals(bModel.getBName()) || "nameB2".equals(bModel.getBName()) || "nameB3".equals(bModel.getBName()));
		}
	}

	@Test
	public void testFindIncludeQM1QO1() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB3");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 3, 1);

		AModel model = service.find(AModel.class, "include:[bModels,bModel]");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());

		assertNotNull(model.getBModel());
		assertEquals("nameB1", model.getBModel().getBName());
		
		assertEquals(3, model.bModels().size());
		for(BModel bModel : model.bModels()) {
			logger.debug(bModel.getBName());
			assertTrue("nameB1".equals(bModel.getBName()) || "nameB2".equals(bModel.getBName()) || "nameB3".equals(bModel.getBName()));
		}
	}

	@Test
	public void testFindIncludeQM2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		AModel model = service.find(AModel.class, "include:{bModels:cModel}");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());

		assertEquals(1, model.bModels().size());
		assertEquals("nameB1", model.bModels().iterator().next().getBName());
		
		assertNotNull(model.bModels().iterator().next().getCModel());
		assertEquals("nameC1", model.bModels().iterator().next().getCModel().getCName());
	}

	@Test
	public void testFindIncludeQM2QO2() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name,b_model) VALUES(?,?)", "nameA1", 2);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name,c_model) VALUES(?,?)", "nameB2", 2);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC2");

		AModel model = service.find(AModel.class, "include:[{bModels:cModel},{bModel:cModel}]");
		
		assertNotNull(model);
		
		assertEquals("nameA1", model.getAName());

		assertEquals(1, model.bModels().size());
		assertEquals("nameB1", model.bModels().iterator().next().getBName());
		
		assertNotNull(model.bModels().iterator().next().getCModel());
		assertEquals("nameC1", model.bModels().iterator().next().getCModel().getCName());

		assertNotNull(model.getBModel());
		assertEquals("nameB2", model.getBModel().getBName());

		assertNotNull(model.getBModel().getCModel());
		assertEquals("nameC2", model.getBModel().getCModel().getCName());
	}

	@Test
	public void testFindIncludeQMM1() throws Exception {
		createDatabase("de");
		
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD1");
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD2");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE2");
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 2);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 2);

		DModel model = service.find(DModel.class, "include:eModels");
		
		assertNotNull(model);
		
		assertEquals("nameD1", model.getDName());

		assertEquals(2, model.eModels().size());
		
		for(EModel emodel : model.eModels()) {
			String name = emodel.getEName();
			logger.debug(name);
			assertTrue("nameE1".equals(name) || "nameE2".equals(name));
		}
	}

	@Test
	public void testFindIncludeQMO1() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF2");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 2);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 2);

		FModel model = service.find(FModel.class, "include:dModels");
		
		assertNotNull(model);
		
		assertEquals("nameF1", model.getFName());

		assertEquals(1, model.dModels().size());
		assertEquals("nameD1", model.dModels().iterator().next().getDName());
		
	}

	@Test
	public void testFindIncludeQOM1() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 2);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 2);
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF2");

		DModel model = service.find(DModel.class, "include:fModel");
		
		assertNotNull(model);
		
		assertEquals("nameD1", model.getDName());
		
		assertNotNull(model.getFModel());
		assertEquals("nameF1", model.getFModel().getFName());
	}

}
