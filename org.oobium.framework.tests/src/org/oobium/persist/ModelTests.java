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
package org.oobium.persist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ModelTests {

	@ModelDescription(
		attrs = { @Attribute(name="name", type=String.class) },
		hasOne = { @Relation(name="model", type=Model.class) },
		hasMany = { @Relation(name="models", type=Model.class) }
	)
	class AModel extends Model {
		
	}
	
	
	@Test
	public void testModelIsEmpty() throws Exception {
		AModel model = new AModel();
		assertTrue(model.isEmpty());
	}

	@Test
	public void testModelClear() throws Exception {
		AModel model = new AModel();
		model.setId(1);
		model.set("name", "name1");
		model.set("model", new AModel());

		assertTrue(model.isSet("name"));
		assertTrue(model.isSet("model"));
		assertNotNull(model.get("name"));
		assertNotNull(model.get("model"));

		model.clear();
		
		assertFalse(model.isSet("name"));
		assertFalse(model.isSet("model"));
		assertNull(model.get("name"));
		assertNull(model.get("model"));
		
		assertTrue(model.isEmpty());
	}

	@Test
	public void testModelCreateHasMany() throws Exception {
		AModel model = new AModel();
		assertFalse(model.isSet("models"));
		assertTrue(model.get("models") instanceof List<?>);
		assertTrue(model.isSet("models"));
	}
	
}
