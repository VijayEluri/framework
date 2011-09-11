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

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class FindByIdTests extends BaseDbTestCase {

	@Test
	public void testFindByIdIncludeQO1() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel").addAttr("name", "String.class").addHasOne("cModel", "CModel.class");
		DynModel cm = DynClasses.getModel("CModel").addAttr("name", "String.class");
		
		migrate(am, bm, cm);
		
		persistService.executeUpdate("INSERT INTO c_models(name) VALUES(?)", "nameC1");
		persistService.executeUpdate("INSERT INTO b_models(name,c_model) VALUES(?,?)", "nameB1", 1);
		persistService.executeUpdate("INSERT INTO a_models(name,b_model) VALUES(?,?)", "nameA1", 1);

		Model a = persistService.findById(am.getModelClass(), 1, "include:bModel");
		
		assertNotNull(a);
		
		assertEquals("nameA1", a.get("name"));
		assertNotNull(a.get("bModel"));
		assertEquals("nameB1", ((Model) a.get("bModel")).get("name"));
		
		// BModel's CModel should exist but not be resolved
		assertNotNull(((Model) a.get("bModel")).get("cModel"));
		assertFalse(((Model) ((Model) a.get("bModel")).get("cModel")).isSet("name"));
	}
	
}
