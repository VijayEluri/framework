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

public class DbPersistorUpdateTests extends DbPersistorTestCase {

	@Test
	public void testUpdateEmpty() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name, b_model, c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		AModel model = new AModel();
		model.setId(1);

		assertFalse(model.isSet("aName"));
		assertFalse(model.isSet("bModel"));
		assertFalse(model.isSet("cModel"));

		service.update(model);
		
		assertFalse(model.isNew());
		
		assertEquals("nameA1", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
	}
	
	@Test
	public void testUpdateAttrOnly() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name, b_model, c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		AModel model = new AModel();
		model.setId(1);
		model.setAName("nameA2");

		assertTrue(model.isSet("aName"));
		assertFalse(model.isSet("bModel"));
		assertFalse(model.isSet("cModel"));
		
		service.update(model);
		
		assertFalse(model.isNew());
		
		assertEquals("nameA2", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
	}
	
	@Test
	public void testUpdateSetAllToNull() throws Exception {
		createDatabase("a");
		
		service.executeUpdate("INSERT INTO a_models(a_name, b_model, c_model) VALUES(?,?,?)", "nameA1", 1, 1);

		AModel model = new AModel();
		model.setId(1);
		model.setAName(null);
		model.setBModel(null);
		model.setCModel(null);

		assertTrue(model.isSet("aName"));
		assertTrue(model.isSet("bModel"));
		assertTrue(model.isSet("cModel"));
		assertNull(model.getAName());
		assertNull(model.getBModel());
		assertNull(model.getCModel());
		
		service.update(model);
		
		assertFalse(model.isNew());
		
		assertEquals(null, service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals(null, service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertEquals(null, service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
	}

	@Test
	public void testUpdateHasOnesWithAttrs() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name, b_model) VALUES(?,?)", "nameA1", 1);
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO c_models(c_name) VALUES(?)", "nameC1");

		CModel cModel = new CModel();
		cModel.setId(1);

		AModel model = new AModel();
		model.setId(1);
		model.setBModel(null);
		model.setCModel(cModel);

		service.update(model);
		
		assertEquals("nameA1", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals(null, service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
		assertEquals("nameB1", service.executeQueryValue("SELECT b_name FROM b_models WHERE id=1"));
		assertEquals("nameC1", service.executeQueryValue("SELECT c_name FROM c_models WHERE id=1"));
	}

	@Test
	public void testUpdateNewHasOneWithNewHasOneAndAttrs() throws Exception {
		createDatabase("abc");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");

		CModel cModel = new CModel();
		cModel.setCName("nameC1");
		
		BModel bModel = new BModel();
		bModel.setBName("nameB1");
		bModel.setCModel(cModel);
		
		AModel model = new AModel();
		model.setId(1);
		model.setAName("nameA1");
		model.setBModel(bModel);

		assertTrue(bModel.isNew());
		assertTrue(cModel.isNew());

		service.update(model);
		
		assertFalse(bModel.isNew());
		assertFalse(cModel.isNew());
		
		assertEquals("nameA1", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals("nameB1", service.executeQueryValue("SELECT b_name FROM b_models WHERE id=1"));
		assertEquals("nameC1", service.executeQueryValue("SELECT c_name FROM c_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertNull(service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
		assertEquals(1, service.executeQueryValue("SELECT c_model FROM b_models WHERE id=1"));
	}
	
	@Test
	public void testUpdateNewManyToNoneWithAttrs() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");

		BModel bModel1 = new BModel();
		bModel1.setBName("nameB1");
		
		BModel bModel2 = new BModel();
		bModel2.setBName("nameB2");
		
		AModel model = new AModel();
		model.setId(1);
		model.setAName("nameA1");
		model.bModels().add(bModel1);
		model.bModels().add(bModel2);

		assertTrue(bModel1.isNew());
		assertTrue(bModel2.isNew());
		
		service.update(model);
		
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
	public void testUpdateNoSetFields() throws Exception {
		AModel model = new AModel();
		model.setId(1);

		service.update(model);
	}
	
	@Test
	public void testUpdateManyToNoneNotSet() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 1);
		
		AModel model = new AModel();
		model.setId(1);
		model.setAName("nameA2");

		service.update(model);

		assertEquals("nameA2", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertEquals(2, service.executeQueryValue("SELECT COUNT(*) FROM a_models__b_models___b_models__null"));
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM a_models__b_models___b_models__null ORDER BY a_models__b_models ASC");
		assertEquals(2, values.size());
		assertEquals(1, values.get(0).get("aModelsBModels"));
		assertEquals(2, values.get(1).get("aModelsBModels"));
		assertEquals(1, values.get(0).get("bModelsNull"));
		assertEquals(1, values.get(1).get("bModelsNull"));
	}
	
	@Test
	public void testUpdateManyToNoneReplace() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB3");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB4");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 1);

		BModel bModel1 = new BModel();
		bModel1.setId(1);
		
		BModel bModel2 = new BModel();
		bModel2.setId(2);
		
		BModel bModel3 = new BModel();
		bModel3.setId(3);
		
		BModel bModel4 = new BModel();
		bModel4.setId(4);
		
		AModel model = new AModel();
		model.setId(1);
		model.setAName("nameA2");
		model.bModels().remove(bModel1);
		model.bModels().remove(bModel2);
		model.bModels().add(bModel3);
		model.bModels().add(bModel4);

		service.update(model);
		
		assertEquals("nameA2", service.executeQueryValue("SELECT a_name FROM a_models WHERE id=1"));
		assertNull(service.executeQueryValue("SELECT b_model FROM a_models WHERE id=1"));
		assertNull(service.executeQueryValue("SELECT c_model FROM a_models WHERE id=1"));
		assertEquals(2, service.executeQueryValue("SELECT COUNT(*) FROM a_models__b_models___b_models__null"));
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM a_models__b_models___b_models__null ORDER BY a_models__b_models ASC");
		assertEquals(2, values.size());
		assertEquals(3, values.get(0).get("aModelsBModels"));
		assertEquals(4, values.get(1).get("aModelsBModels"));
		assertEquals(1, values.get(0).get("bModelsNull"));
		assertEquals(1, values.get(1).get("bModelsNull"));
	}

	@Test
	public void testUpdateManyToNoneAdd() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB3");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB4");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 1);

		BModel bModel1 = new BModel();
		bModel1.setId(1);
		
		BModel bModel2 = new BModel();
		bModel2.setId(2);
		
		BModel bModel3 = new BModel();
		bModel3.setId(3);
		
		BModel bModel4 = new BModel();
		bModel4.setId(4);
		
		AModel model = new AModel();
		model.setId(1);
		model.bModels().add(bModel1);
		model.bModels().add(bModel2);
		model.bModels().add(bModel3);
		model.bModels().add(bModel4);

		service.update(model);
		
		assertEquals(4, service.executeQueryValue("SELECT COUNT(*) FROM a_models__b_models___b_models__null"));
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM a_models__b_models___b_models__null ORDER BY a_models__b_models ASC");
		assertEquals(4, values.size());
		assertEquals(1, values.get(0).get("aModelsBModels"));
		assertEquals(2, values.get(1).get("aModelsBModels"));
		assertEquals(3, values.get(2).get("aModelsBModels"));
		assertEquals(4, values.get(3).get("aModelsBModels"));
		assertEquals(1, values.get(0).get("bModelsNull"));
		assertEquals(1, values.get(1).get("bModelsNull"));
		assertEquals(1, values.get(2).get("bModelsNull"));
		assertEquals(1, values.get(3).get("bModelsNull"));
	}

	@Test
	public void testUpdateManyToNoneRemove() throws Exception {
		createDatabase("ab");
		
		service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB1");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB2");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB3");
		service.executeUpdate("INSERT INTO b_models(b_name) VALUES(?)", "nameB4");
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 2, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 3, 1);
		service.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a_models__b_models,b_models__null) VALUES(?,?)", 4, 1);

		BModel bModel1 = new BModel();
		bModel1.setId(1);
		
		BModel bModel3 = new BModel();
		bModel3.setId(3);
		
		AModel model = new AModel();
		model.setId(1);
		model.bModels().remove(bModel1);
		model.bModels().remove(bModel3);

		service.update(model);
		
		assertEquals(2, service.executeQueryValue("SELECT COUNT(*) FROM a_models__b_models___b_models__null"));
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM a_models__b_models___b_models__null ORDER BY a_models__b_models ASC");
		assertEquals(2, values.size());
		assertEquals(2, values.get(0).get("aModelsBModels"));
		assertEquals(4, values.get(1).get("aModelsBModels"));
		assertEquals(1, values.get(0).get("bModelsNull"));
		assertEquals(1, values.get(1).get("bModelsNull"));
	}

	@Test
	public void testUpdateManyToManyNotSet() throws Exception {
		createDatabase("de");
		
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE2");
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 1);
		
		DModel model = new DModel();
		model.setId(1);
		model.setDName("nameD2");

		service.update(model);

		assertEquals("nameD2", service.executeQueryValue("SELECT d_name FROM d_models WHERE id=1"));
		assertEquals(2, service.executeQueryValue("SELECT COUNT(*) FROM d_models__e_models___e_models__d_models"));
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models__e_models___e_models__d_models ORDER BY d_models__e_models ASC");
		assertEquals(2, values.size());
		assertEquals(1, values.get(0).get("dModelsEModels"));
		assertEquals(2, values.get(1).get("dModelsEModels"));
		assertEquals(1, values.get(0).get("eModelsDModels"));
		assertEquals(1, values.get(1).get("eModelsDModels"));
	}
	
	@Test
	public void testUpdateManyToManyReplace() throws Exception {
		createDatabase("de");
		
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE2");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE3");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE4");
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 1);

		EModel eModel3 = new EModel();
		eModel3.setId(3);
		
		EModel eModel4 = new EModel();
		eModel4.setId(4);
		
		DModel model = new DModel();
		model.setId(1);
		model.setDName("nameD2");
		model.eModels().clear();
		model.eModels().add(eModel3);
		model.eModels().add(eModel4);

		service.update(model);
		
		assertEquals("nameD2", service.executeQueryValue("SELECT d_name FROM d_models WHERE id=1"));
		assertEquals(2, service.executeQueryValue("SELECT COUNT(*) FROM d_models__e_models___e_models__d_models"));
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models__e_models___e_models__d_models ORDER BY d_models__e_models ASC");
		assertEquals(2, values.size());
		assertEquals(3, values.get(0).get("dModelsEModels"));
		assertEquals(4, values.get(1).get("dModelsEModels"));
		assertEquals(1, values.get(0).get("eModelsDModels"));
		assertEquals(1, values.get(1).get("eModelsDModels"));
	}

	@Test
	public void testUpdateManyToManyAdd() throws Exception {
		createDatabase("de");
		
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE2");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE3");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE4");
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 1);

		EModel eModel1 = new EModel();
		eModel1.setId(1);
		
		EModel eModel2 = new EModel();
		eModel2.setId(2);
		
		EModel eModel3 = new EModel();
		eModel3.setId(3);
		
		EModel eModel4 = new EModel();
		eModel4.setId(4);
		
		DModel model = new DModel();
		model.setId(1);
		model.eModels().add(eModel1);
		model.eModels().add(eModel2);
		model.eModels().add(eModel3);
		model.eModels().add(eModel4);

		service.update(model);
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models__e_models___e_models__d_models ORDER BY d_models__e_models ASC");
		assertEquals(4, values.size());
		assertEquals(1, values.get(0).get("dModelsEModels"));
		assertEquals(2, values.get(1).get("dModelsEModels"));
		assertEquals(3, values.get(2).get("dModelsEModels"));
		assertEquals(4, values.get(3).get("dModelsEModels"));
		assertEquals(1, values.get(0).get("eModelsDModels"));
		assertEquals(1, values.get(1).get("eModelsDModels"));
		assertEquals(1, values.get(2).get("eModelsDModels"));
		assertEquals(1, values.get(3).get("eModelsDModels"));
	}

	@Test
	public void testUpdateManyToManyRemove() throws Exception {
		createDatabase("de");
		
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE1");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE2");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE3");
		service.executeUpdate("INSERT INTO e_models(e_name) VALUES(?)", "nameE4");
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 1, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 2, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 3, 1);
		service.executeUpdate("INSERT INTO d_models__e_models___e_models__d_models(d_models__e_models,e_models__d_models) VALUES(?,?)", 4, 1);

		EModel eModel1 = new EModel();
		eModel1.setId(1);
		
		EModel eModel3 = new EModel();
		eModel3.setId(3);
		
		DModel model = new DModel();
		model.setId(1);
		model.eModels().remove(eModel1);
		model.eModels().remove(eModel3);

		service.update(model);
		
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models__e_models___e_models__d_models ORDER BY d_models__e_models ASC");
		assertEquals(2, values.size());
		assertEquals(2, values.get(0).get("dModelsEModels"));
		assertEquals(4, values.get(1).get("dModelsEModels"));
		assertEquals(1, values.get(0).get("eModelsDModels"));
		assertEquals(1, values.get(1).get("eModelsDModels"));
	}

	@Test(expected=SQLException.class)
	public void testUpdateNonExistent() throws Exception {
		createDatabase("a");
		
		AModel model = new AModel();
		model.setId(1);
		model.setAName("nameA1"); // if no fields are set, the update won't even be attempted
		service.update(model);
	}
	
	@Test
	public void testUpdateManyToOneNotSet() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameH1");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD4", 1);

		FModel model = new FModel();
		model.setId(1);
		model.setFName("nameF2");

		service.update(model);

		assertEquals("nameF2", service.executeQueryValue("SELECT f_name FROM f_models WHERE id=1"));
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models ORDER BY d_name ASC");
		assertEquals(4, values.size());
		assertEquals(1, values.get(0).get("fModel"));
		assertEquals(1, values.get(1).get("fModel"));
		assertEquals(1, values.get(2).get("fModel"));
		assertEquals(1, values.get(3).get("fModel"));
	}
	
	@Test
	public void testUpdateManyToOneAddAndRemove() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameH1");
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameH2");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD4", 1);

		DModel dModel2 = new DModel();
		dModel2.setId(2);
		
		DModel dModel4 = new DModel();
		dModel4.setId(4);

		FModel fModel2 = new FModel();
		fModel2.setId(2);
		fModel2.dModels().add(dModel2);
		fModel2.dModels().add(dModel4);

		service.update(fModel2);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models ORDER BY d_name ASC");
		assertEquals(4, values.size());
		assertEquals("nameD1", values.get(0).get("dName"));
		assertEquals(1, values.get(0).get("fModel"));
		assertEquals("nameD2", values.get(1).get("dName"));
		assertEquals(2, values.get(1).get("fModel"));
		assertEquals("nameD3", values.get(2).get("dName"));
		assertEquals(1, values.get(2).get("fModel"));
		assertEquals("nameD4", values.get(3).get("dName"));
		assertEquals(2, values.get(3).get("fModel"));
	}
	
	@Test
	public void testUpdateOneToManyNotSet() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD4", 1);
		
		FModel model = new FModel();
		model.setId(1);
		model.setFName("nameF2");

		service.update(model);

		assertEquals("nameF2", service.executeQueryValue("SELECT f_name FROM f_models WHERE id=1"));
		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models ORDER BY d_name ASC");
		assertEquals(4, values.size());
		assertEquals(1, values.get(0).get("fModel"));
		assertEquals(1, values.get(1).get("fModel"));
		assertEquals(1, values.get(2).get("fModel"));
		assertEquals(1, values.get(3).get("fModel"));
	}
	
	@Test
	public void testUpdateOneToManyReplace() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 1);
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD3");
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD4");

		DModel dModel1 = new DModel();
		dModel1.setId(1);
		
		DModel dModel2 = new DModel();
		dModel2.setId(2);
		
		DModel dModel3 = new DModel();
		dModel3.setId(3);
		
		DModel dModel4 = new DModel();
		dModel4.setId(4);
		
		FModel model = new FModel();
		model.setId(1);
		model.dModels().remove(dModel1);
		model.dModels().remove(dModel2);
		model.dModels().add(dModel3);
		model.dModels().add(dModel4);

		service.update(model);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models ORDER BY d_name ASC");
		assertEquals(4, values.size());
		assertEquals(null, values.get(0).get("fModel"));
		assertEquals(null, values.get(1).get("fModel"));
		assertEquals(1, values.get(2).get("fModel"));
		assertEquals(1, values.get(3).get("fModel"));
	}
	
	@Test
	public void testUpdateOneToManyAdd() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 1);
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD3");
		service.executeUpdate("INSERT INTO d_models(d_name) VALUES(?)", "nameD4");

		DModel dModel3 = new DModel();
		dModel3.setId(3);
		
		DModel dModel4 = new DModel();
		dModel4.setId(4);
		
		FModel model = new FModel();
		model.setId(1);
		model.dModels().add(dModel3);
		model.dModels().add(dModel4);

		service.update(model);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models ORDER BY d_name ASC");
		assertEquals(4, values.size());
		assertEquals(1, values.get(0).get("fModel"));
		assertEquals(1, values.get(1).get("fModel"));
		assertEquals(1, values.get(2).get("fModel"));
		assertEquals(1, values.get(3).get("fModel"));
	}
	
	@Test
	public void testUpdateOneToManyRemove() throws Exception {
		createDatabase("df");
		
		service.executeUpdate("INSERT INTO f_models(f_name) VALUES(?)", "nameF1");
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD1", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD2", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD3", 1);
		service.executeUpdate("INSERT INTO d_models(d_name,f_model) VALUES(?,?)", "nameD4", 1);

		DModel dModel1 = new DModel();
		dModel1.setId(1);
		
		DModel dModel3 = new DModel();
		dModel3.setId(3);
		
		FModel model = new FModel();
		model.setId(1);
		model.dModels().remove(dModel1);
		model.dModels().remove(dModel3);

		service.update(model);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM d_models ORDER BY d_name ASC");
		assertEquals(4, values.size());
		assertEquals(null, values.get(0).get("fModel"));
		assertEquals(1, values.get(1).get("fModel"));
		assertEquals(null, values.get(2).get("fModel"));
		assertEquals(1, values.get(3).get("fModel"));
	}
	
	@Test
	public void testUpdateOneRequiredToManyNotSet() throws Exception {
		createDatabase("gh");
		
		service.executeUpdate("INSERT INTO h_models(h_name) VALUES(?)", "nameH1");
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG1", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG2", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG3", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG4", 1);

		GModel model = new GModel();
		model.setId(1);
		model.setGName("nameG0");

		service.update(model);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM g_models ORDER BY g_name ASC");
		assertEquals(4, values.size());
		assertEquals("nameG0", values.get(0).get("gName"));
		assertEquals(1, values.get(0).get("hModel"));
		assertEquals("nameG2", values.get(1).get("gName"));
		assertEquals(1, values.get(1).get("hModel"));
		assertEquals("nameG3", values.get(2).get("gName"));
		assertEquals(1, values.get(2).get("hModel"));
		assertEquals("nameG4", values.get(3).get("gName"));
		assertEquals(1, values.get(3).get("hModel"));
	}
	
	@Test
	public void testUpdateOneRequiredToManyAddAndRemove() throws Exception {
		createDatabase("gh");
		
		service.executeUpdate("INSERT INTO h_models(h_name) VALUES(?)", "nameH1");
		service.executeUpdate("INSERT INTO h_models(h_name) VALUES(?)", "nameH2");
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG1", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG2", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG3", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG4", 1);

		HModel hModel2 = new HModel();
		hModel2.setId(2);

		GModel gModel2 = new GModel();
		gModel2.setId(2);
		gModel2.setHModel(hModel2);

		service.update(gModel2);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM g_models ORDER BY g_name ASC");
		assertEquals(4, values.size());
		assertEquals("nameG1", values.get(0).get("gName"));
		assertEquals(1, values.get(0).get("hModel"));
		assertEquals("nameG2", values.get(1).get("gName"));
		assertEquals(2, values.get(1).get("hModel"));
		assertEquals("nameG3", values.get(2).get("gName"));
		assertEquals(1, values.get(2).get("hModel"));
		assertEquals("nameG4", values.get(3).get("gName"));
		assertEquals(1, values.get(3).get("hModel"));
	}
	
	@Test
	public void testUpdateManyToOneRequiredAddAndRemove() throws Exception {
		createDatabase("gh");
		
		service.executeUpdate("INSERT INTO h_models(h_name) VALUES(?)", "nameH1");
		service.executeUpdate("INSERT INTO h_models(h_name) VALUES(?)", "nameH2");
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG1", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG2", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG3", 1);
		service.executeUpdate("INSERT INTO g_models(g_name,h_model) VALUES(?,?)", "nameG4", 1);

		GModel gModel2 = new GModel();
		gModel2.setId(2);
		
		GModel gModel4 = new GModel();
		gModel4.setId(4);

		HModel hModel2 = new HModel();
		hModel2.setId(2);
		hModel2.gModels().add(gModel2);
		hModel2.gModels().add(gModel4);

		service.update(hModel2);

		List<Map<String, Object>> values = service.executeQuery("SELECT * FROM g_models ORDER BY g_name ASC");
		assertEquals(4, values.size());
		assertEquals("nameG1", values.get(0).get("gName"));
		assertEquals(1, values.get(0).get("hModel"));
		assertEquals("nameG2", values.get(1).get("gName"));
		assertEquals(2, values.get(1).get("hModel"));
		assertEquals("nameG3", values.get(2).get("gName"));
		assertEquals(1, values.get(2).get("hModel"));
		assertEquals("nameG4", values.get(3).get("gName"));
		assertEquals(2, values.get(3).get("hModel"));
	}
	
}
