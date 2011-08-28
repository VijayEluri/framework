package org.oobium.persist.mongo;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;
import org.oobium.persist.Text;

public class CreateTests extends BaseMongoTestCase {

	@Test
	public void testAttrString() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
		
		Model a = am.newInstance();
		a.set("name", "bob");
		a.create();
		
		assertNoErrors(a);
		
		assertNotNull(persistService.find("a_models", a.getId()));
		assertEquals("bob", persistService.find("a_models", a.getId()).get("name"));
	}
	
	@Test
	public void testAttrBoolean() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("success", "Boolean.class").addAttr("failure", "Boolean.class").addAttr("huh", "Boolean.class");
		
		Model a = am.newInstance();
		a.set("success", true);
		a.set("failure", false);
		a.set("huh", null);
		a.create();
		
		assertNoErrors(a);
		
		assertNotNull(persistService.find("a_models", a.getId()));
		assertEquals(true, persistService.find("a_models", a.getId()).get("success"));
		assertEquals(false, persistService.find("a_models", a.getId()).get("failure"));
		assertNull(persistService.find("a_models", a.getId()).get("huh"));
	}
	
	@Test
	public void testAttrBoolean_Primitive() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("success", "boolean.class").addAttr("failure", "boolean.class").addAttr("huh", "boolean.class");
		
		Model a = am.newInstance();
		a.set("success", true);
		a.set("failure", false);
		a.set("huh", null);
		a.create();
		
		assertNoErrors(a);
		
		assertNotNull(persistService.find("a_models", a.getId()));
		assertEquals(true, persistService.find("a_models", a.getId()).get("success"));
		assertEquals(false, persistService.find("a_models", a.getId()).get("failure"));
		assertEquals(false, persistService.find("a_models", a.getId()).get("huh"));
	}
	
	@Test
	public void testAttrText() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addImport(Text.class).addAttr("attr", "Text.class");
		
		Model a = am.newInstance();
		a.set("attr", "hello");
		a.create();
		
		assertNoErrors(a);
		
		assertNotNull(persistService.find("a_models", a.getId()));
		assertEquals("hello", persistService.find("a_models", a.getId()).get("attr"));
	}
	
	@Test
	public void testAttr_Empty() throws Exception {
		
		// creating a model with no attributes is currently allowed with MongoDB... should it be this way?
		
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
		
		Model a = am.newInstance();
		a.create();
		
		assertNoErrors(a);
	}
	
	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		Model b = bm.newInstance();
		b.set("name", "bob");
		
		Model a = am.newInstance();
		a.set("bModel", b);
		a.create();
		
		assertNoErrors(a);
		
		assertNotNull(persistService.find("a_models", a.getId()));
		assertNotNull(persistService.find("b_models", b.getId()));
		assertEquals(b.getId(), persistService.find("a_models", a.getId()).get("bModel"));
		assertEquals("bob", persistService.find("b_models", b.getId()).get("name"));
	}

	@Test
	public void testHasOne_Embedded() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class", "embedded=true");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		Model b = bm.newInstance();
		b.set("name", "bob");
		
		Model a = am.newInstance();
		a.set("bModel", b);
		a.create();
		
		assertNoErrors(a);
		
		assertNull(b.getId());
		assertNotNull(persistService.find("a_models", a.getId()));
		
		assertNull(persistService.find("b_models", b.getId())); // embedded, so there is no b_models...
		assertNotNull(persistService.find("a_models", a.getId()).get("bModel"));
		assertEquals("bob", ((Map<?,?>) persistService.find("a_models", a.getId()).get("bModel")).get("name"));
		assertEquals(0, persistService.count(bm.getModelClass()));
	}

	@Test
	public void testHasMany_Embedded() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasMany("bModels", "BModel.class", "embedded=true");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		Model b1 = bm.newInstance().set("name", "bob");
		Model b2 = bm.newInstance().set("name", "joe");
		
		Model a = am.newInstance();
		a.set("bModels", new Model[] { b1, b2 });
		a.create();
		
		assertNoErrors(a);
		
		assertNull(b1.getId());
		assertNotNull(persistService.find("a_models", a.getId()));
		
		assertNull(persistService.find("b_models", b1.getId())); // embedded, so there is no b_models...
		assertNotNull(persistService.find("a_models", a.getId()).get("bModels"));
		assertEquals(2, ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).size());
		assertEquals("bob", ((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(0)).get("name"));
		assertEquals("joe", ((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(1)).get("name"));
		assertEquals(0, persistService.count(bm.getModelClass()));
	}

	@Test
	public void testHasMany_EmbedWithId() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasMany("bModels", "BModel.class", "embed=\"name\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class").addAttr("age", "int.class");
		
		Model b1 = bm.newInstance().set("name", "bob").set("age", 30).setId(new ObjectId());
		Model b2 = bm.newInstance().set("name", "joe").set("age", 40).setId(new ObjectId());
		
		Model a = am.newInstance();
		a.set("bModels", new Model[] { b1, b2 });
		a.create();
		
		assertNoErrors(a);
		
		assertNotNull(b1.getId());
		assertNotNull(b2.getId());
		assertNotNull(persistService.find("a_models", a.getId()));
		
		assertNull(persistService.find("b_models", b1.getId())); // embedded, so there is no b_models...
		assertNotNull(persistService.find("a_models", a.getId()).get("bModels"));
		assertEquals(2, ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).size());
		assertEquals("bob", ((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(0)).get("name"));
		assertEquals("joe", ((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(1)).get("name"));
		assertFalse(((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(0)).containsKey("age"));
		assertFalse(((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(1)).containsKey("age"));
		assertTrue(((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(0)).containsKey("id"));
		assertTrue(((Map<?,?>) ((List<?>) persistService.find("a_models", a.getId()).get("bModels")).get(1)).containsKey("id"));
		assertEquals(0, persistService.count(bm.getModelClass()));
	}

}
