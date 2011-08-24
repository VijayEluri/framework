package org.oobium.persist.mongo;

import static org.junit.Assert.*;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class FindByIdTests extends BaseMongoTestCase {

	@Test
	public void testById() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		Object id = persistService.insert("a_models", "name:?", "bob").toString();
		persistService.insert("a_models", "name:?", "joe");

		Model model = persistService.findById(am.getModelClass(), id);
		
		assertNotNull(model);
		assertEquals("bob", model.get("name"));
	}

	@Test
	public void testByString() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		String id = persistService.insert("a_models", "name:?", "bob").toString();
		persistService.insert("a_models", "name:?", "joe");

		Model model = persistService.findById(am.getModelClass(), id);
		
		assertNotNull(model);
		assertEquals("bob", model.get("name"));
	}

}
