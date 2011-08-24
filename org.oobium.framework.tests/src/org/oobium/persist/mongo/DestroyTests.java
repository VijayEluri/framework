package org.oobium.persist.mongo;

import static org.junit.Assert.*;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class DestroyTests extends BaseMongoTestCase {

	@Test
	public void testDestroyById() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		Object id = persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");

		assertNotNull(persistService.find("a_models", id));
		assertEquals(2, persistService.count(am.getModelClass()));

		Model model = am.newInstance();
		model.setId(id);
		model.destroy();
		
		assertNull(persistService.find("a_models", id));
		assertEquals(1, persistService.count(am.getModelClass()));
	}

	@Test
	public void testDestroyByString() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		String id = persistService.insert("a_models", "name:?", "bob").toString();
		persistService.insert("a_models", "name:?", "joe");

		assertNotNull(persistService.find("a_models", id));
		assertEquals(2, persistService.count(am.getModelClass()));

		Model model = am.newInstance();
		model.setId(id);
		model.destroy();
		
		assertNull(persistService.find("a_models", id));
		assertEquals(1, persistService.count(am.getModelClass()));
	}

}
