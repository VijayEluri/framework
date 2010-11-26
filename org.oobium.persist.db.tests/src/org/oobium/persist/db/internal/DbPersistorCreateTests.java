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

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.oobium.persist.db.tests.DbPersistorTestCase;
import org.oobium.persist.db.tests.models.AModel;
import org.oobium.persist.db.tests.models.BModel;
import org.oobium.persist.db.tests.models.CModel;
import org.oobium.persist.db.tests.models.DModel;
import org.oobium.persist.db.tests.models.EModel;
import org.oobium.persist.db.tests.models.FModel;
import org.oobium.persist.db.tests.models.GModel;
import org.oobium.persist.db.tests.models.HModel;

public class DbPersistorCreateTests extends DbPersistorTestCase {

	@Test
	public void testCreateEmpty() throws Exception {
		createDatabase("a");
		
		AModel model = new AModel();

		service.create(model);
		
		assertFalse(model.isNew());
		assertTrue(model.isEmpty());
	}
	
	@Test
	public void testCreateAttr() throws Exception {
		createDatabase("a");

		AModel model = new AModel();
		model.setAName("nameA1");

		service.create(model);
		
		assertFalse(model.isNew());
		
		assertEquals("nameA1", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
	}
	
	@Test
	public void testCreateHasOnes() throws Exception {
		createDatabase("abc");

		BModel bModel = new BModel();
		bModel.setBName("nameB1");
		
		CModel cModel = new CModel();
		cModel.setCName("nameC1");

		AModel model = new AModel();
		model.setBModel(bModel);
		model.setCModel(cModel);
		model.setAName("nameA1");

		service.create(model);
		
		assertFalse(model.isNew());
		
		assertEquals("nameA1", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
		assertEquals("nameB1", service.executeQueryValue("SELECT b_name FROM b_models WHERE id=1"));
		assertEquals("nameC1", service.executeQueryValue("SELECT c_name FROM c_models WHERE id=1"));
	}

	@Test
	public void testCreateHasOneWithHasOneAndAttrs() throws Exception {
		createDatabase("abc");

		CModel cModel = new CModel();
		cModel.setCName("nameC1");
		
		BModel bModel = new BModel();
		bModel.setBName("nameB1");
		bModel.setCModel(cModel);
		
		AModel model = new AModel();
		model.setAName("nameA1");
		model.setBModel(bModel);

		service.create(model);
		
		assertFalse(model.isNew());
		
		assertEquals("nameA1", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals("nameB1", service.executeQueryValue("SELECT b_name FROM b_models WHERE id=1"));
		assertEquals("nameC1", service.executeQueryValue("SELECT c_name FROM c_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertNull(service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT c_model FROM b_models WHERE id=1"));
	}
	
	@Test
	public void testCreateManyToNone() throws Exception {
		createDatabase("ab");

		BModel bModel1 = new BModel();
		bModel1.setBName("nameB1");
		
		BModel bModel2 = new BModel();
		bModel2.setBName("nameB2");
		
		AModel model = new AModel();
		model.setAName("nameA1");
		model.bModels().add(bModel1);
		model.bModels().add(bModel2);

		service.create(model);
		
		assertFalse(model.isNew());
		assertFalse(bModel1.isNew());
		assertFalse(bModel2.isNew());
		
		assertEquals("nameA1", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertNull(service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertNull(service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
		assertEquals(2, service.executeQueryValue("SELECT COUNT(*) FROM b_models"));
		assertEquals(1, service.executeQueryValue("SELECT COUNT(*) FROM b_models WHERE b_name='nameB1'"));
		assertEquals(1, service.executeQueryValue("SELECT COUNT(*) FROM b_models WHERE b_name='nameB2'"));
		assertEquals(2, service.executeQueryValue("SELECT COUNT(*) FROM a_models__b_models___b_models__null"));
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM a_models__b_models___b_models__null");
		assertEquals(2, values.size());
		assertEquals(1, values.get(0).get("aModelsBModels"));
		assertEquals(2, values.get(1).get("aModelsBModels"));
		assertEquals(1, values.get(0).get("bModelsNull"));
		assertEquals(1, values.get(1).get("bModelsNull"));
	}

	@Test
	public void testCreateManyToMany() throws Exception {
		createDatabase("de");

		DModel dmodel = new DModel();
		dmodel.setDName("nameD1");
		
		EModel emodel1 = new EModel();
		emodel1.setEName("nameE1");
		
		EModel emodel2 = new EModel();
		emodel2.setEName("nameE2");
		dmodel.eModels().add(emodel1);
		dmodel.eModels().add(emodel2);
		
		service.create(dmodel);
		
		assertFalse(dmodel.isNew());
		assertFalse(emodel1.isNew());
		assertFalse(emodel2.isNew());

		assertEquals("nameD1", service.executeQueryValue("SELECT d_name FROM d_models WHERE id=1"));
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models__e_models___e_models__d_models order by d_models__e_models ASC");
		assertEquals(2, values.size());
		assertEquals(1, values.get(0).get("dModelsEModels"));
		assertEquals(2, values.get(1).get("dModelsEModels"));
		assertEquals(1, values.get(0).get("eModelsDModels"));
		assertEquals(1, values.get(1).get("eModelsDModels"));
	}
	
	@Test
	public void testCreateManyToOne() throws Exception {
		createDatabase("df");

		DModel dModel1 = new DModel();
		dModel1.setDName("nameD1");
		
		DModel dModel2 = new DModel();
		dModel2.setDName("nameD2");

		FModel model = new FModel();
		model.setFName("nameF");
		model.dModels().add(dModel1);
		model.dModels().add(dModel2);

		service.create(model);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models ORDER BY d_name ASC");
		assertEquals(2, values.size());
		assertEquals("nameD1", values.get(0).get("dName"));
		assertEquals(1, values.get(0).get("fModel"));
		assertEquals("nameD2", values.get(1).get("dName"));
		assertEquals(1, values.get(1).get("fModel"));
	}
	
	@Test
	public void testCreateManyToOneRequired() throws Exception {
		createDatabase("gh");

		GModel gModel1 = new GModel();
		gModel1.setGName("nameG1");
		
		GModel gModel2 = new GModel();
		gModel2.setGName("nameG2");

		HModel hModel = new HModel();
		hModel.setHName("nameH");
		hModel.gModels().add(gModel1);
		hModel.gModels().add(gModel2);

		service.create(hModel);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM g_models ORDER BY g_name ASC");
		assertEquals(2, values.size());
		assertEquals("nameG1", values.get(0).get("gName"));
		assertEquals(1, values.get(0).get("hModel"));
		assertEquals("nameG2", values.get(1).get("gName"));
		assertEquals(1, values.get(1).get("hModel"));
	}
	
	@Test
	public void testCreateOneToMany() throws Exception {
		createDatabase("df");

		FModel fModel = new FModel();
		fModel.setFName("nameF1");

		DModel dModel = new DModel();
		dModel.setDName("nameD1");
		dModel.setFModel(fModel);
		
		service.create(dModel);

		assertEquals("nameD1", service.executeQueryValue("SELECT d_name FROM d_models WHERE id=1"));
		assertEquals("nameF1", service.executeQueryValue("SELECT f_name FROM f_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT f_model FROM d_models WHERE id=1"));
	}
	
	@Test
	public void testCreateOneRequiredToMany() throws Exception {
		createDatabase("gh");

		HModel hModel = new HModel();
		hModel.setHName("nameH1");

		GModel gModel = new GModel();
		gModel.setGName("nameG1");
		gModel.setHModel(hModel);

		service.create(gModel);

		assertEquals("nameG1", service.executeQueryValue("SELECT g_name FROM g_models WHERE id=1"));
		assertEquals("nameH1", service.executeQueryValue("SELECT h_name FROM h_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT h_model FROM g_models WHERE id=1"));
	}

	@Test(expected=SQLException.class)
	public void testCreateOneRequiredToManyFail() throws Exception {
		createDatabase("g");

		GModel gModel = new GModel();
		gModel.setGName("nameG1");
		service.create(gModel);
	}

}
