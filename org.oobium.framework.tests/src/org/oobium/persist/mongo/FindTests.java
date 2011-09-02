package org.oobium.persist.mongo;

import static org.oobium.utils.literal.Map;
import static org.junit.Assert.*;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class FindTests extends BaseMongoTestCase {

	@Test
	public void testFindAllWithMapQuery() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		Model model = persistService.find(am.getModelClass(), Map("name", (Object) "joe"));
		
		assertNotNull(model);
		assertEquals("joe", model.get("name"));
	}
	
	@Test
	public void testFindAllWithMapQueryAndValue() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		Model model = persistService.find(am.getModelClass(), Map("name", (Object) "?"), "joe");
		
		assertNotNull(model);
		assertEquals("joe", model.get("name"));
	}
	
	@Test
	public void testFindAllWithQuery() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		Model model = persistService.find(am.getModelClass(), "$or:[{name:?},{name:?}]", "bob", "dan");
		
		assertNotNull(model);
		assertEquals("bob", model.get("name"));
	}
	
	@Test
	public void testFindAllWithOrder_Map() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "b");
		persistService.insert("a_models", "name:?", "a");
		persistService.insert("a_models", "name:?", "c");
		persistService.insert("a_models", "name:?", "e");
		persistService.insert("a_models", "name:?", "d");

		Model model;
		
		model = persistService.find(am.getModelClass(), Map("$order", (Object) "name:-1"));
		
		assertNotNull(model);
		assertEquals("e", model.get("name"));

		model = persistService.find(am.getModelClass(), Map("$order", (Object) "name:1"));
		
		assertNotNull(model);
		assertEquals("a", model.get("name"));

		model = persistService.find(am.getModelClass(), Map("$order", (Object) "_id:-1"));
		
		assertNotNull(model);
		assertEquals("d", model.get("name"));

		model = persistService.find(am.getModelClass(), Map("$order", (Object) "_id:1"));
		
		assertNotNull(model);
		assertEquals("b", model.get("name"));
	}
	
	@Test
	public void testFindAllWithOrder() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "b");
		persistService.insert("a_models", "name:?", "a");
		persistService.insert("a_models", "name:?", "c");
		persistService.insert("a_models", "name:?", "e");
		persistService.insert("a_models", "name:?", "d");

		Model model;
		
		model = persistService.find(am.getModelClass(), "{$order:{name:-1}}");
		
		assertNotNull(model);
		assertEquals("e", model.get("name"));

		model = persistService.find(am.getModelClass(), "{$order:{name:1}}");
		
		assertNotNull(model);
		assertEquals("a", model.get("name"));

		model = persistService.find(am.getModelClass(), "{$order:{_id:-1}}");
		
		assertNotNull(model);
		assertEquals("d", model.get("name"));

		model = persistService.find(am.getModelClass(), "{$order:{_id:1}}");
		
		assertNotNull(model);
		assertEquals("b", model.get("name"));
	}
	
	@Test
	public void testFindAllWithLimit() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		Model model = persistService.find(am.getModelClass(), Map("$limit", (Object) 2));
		
		assertNotNull(model);
		assertEquals("bob", model.get("name")); // #find forces a limit of 1, so passing in 2 is irrelevant
	}

	@Test
	public void testFindAllWithLimitAndSkip_Map() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		Model model = persistService.find(am.getModelClass(), "$limit:{1:2}");
		
		assertNotNull(model);
		assertEquals("joe", model.get("name"));
	}

	@Test
	public void testFindAllWithLimitAndSkip_String() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		Model model = persistService.find(am.getModelClass(), "$limit:'1,2'");
		
		assertNotNull(model);
		assertEquals("joe", model.get("name"));
	}

}
