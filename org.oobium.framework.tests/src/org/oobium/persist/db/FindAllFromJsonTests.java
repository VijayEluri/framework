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

public class FindAllFromJsonTests extends BaseDbTestCase {

	@Test
	public void testFindAllWithLimit() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA1");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA2");
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA3");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "$limit:2");

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

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "$sort:\"name desc\"");
		
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

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "name:?", "nameA2");
		
		assertEquals(1, models.size());
		assertEquals("nameA2", models.get(0).get("name"));
	}

}
