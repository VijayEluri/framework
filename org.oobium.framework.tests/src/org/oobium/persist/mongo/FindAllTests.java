package org.oobium.persist.mongo;

import static org.oobium.utils.literal.Map;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class FindAllTests extends BaseMongoTestCase {

	@Test
	public void testFindAll() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");

		List<? extends Model> models = persistService.findAll(am.getModelClass());
		
		assertEquals(2, models.size());
		assertEquals("bob", models.get(0).get("name"));
		assertEquals("joe", models.get(1).get("name"));
	}

	@Test
	public void testFindAllWithMapQuery() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), Map("name", (Object) "joe"));
		
		assertEquals(1, models.size());
		assertEquals("joe", models.get(0).get("name"));
	}
	
	@Test
	public void testFindAllWithMapQueryAndValue() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), Map("name", (Object) "?"), "joe");
		
		assertEquals(1, models.size());
		assertEquals("joe", models.get(0).get("name"));
	}
	
	@Test
	public void testFindAllWithQuery() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "$or:[{name:?},{name:?}]", "bob", "dan");
		
		assertEquals(2, models.size());
		assertEquals("bob", models.get(0).get("name"));
		assertEquals("dan", models.get(1).get("name"));
	}
	
	@Test
	public void testFindAllWithOrder() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), Map("$order", (Object) "name:-1"));
		
		assertEquals(3, models.size());
		assertEquals("joe", models.get(0).get("name"));
		assertEquals("dan", models.get(1).get("name"));
		assertEquals("bob", models.get(2).get("name"));
	}
	
	@Test
	public void testFindAllWithLimit() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), Map("$limit", (Object) 2));
		
		assertEquals(2, models.size());
		assertEquals("bob", models.get(0).get("name"));
		assertEquals("joe", models.get(1).get("name"));
	}

	@Test
	public void testFindAllWithLimitAndSkip_Map() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "$limit:{2:1}");
		
		assertEquals(2, models.size());
		assertEquals("joe", models.get(0).get("name"));
		assertEquals("dan", models.get(1).get("name"));
	}

	@Test
	public void testFindAllWithLimitAndSkip_String() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		persistService.insert("a_models", "name:?", "bob");
		persistService.insert("a_models", "name:?", "joe");
		persistService.insert("a_models", "name:?", "dan");

		List<? extends Model> models = persistService.findAll(am.getModelClass(), "$limit:'2,1'");
		
		assertEquals(2, models.size());
		assertEquals("joe", models.get(0).get("name"));
		assertEquals("dan", models.get(1).get("name"));
	}

}
