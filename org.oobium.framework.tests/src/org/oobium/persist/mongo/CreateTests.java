package org.oobium.persist.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

import org.junit.Ignore;
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
	
	@Ignore
	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		Model b = spy(bm.newInstance());
		b.set("name", "bob");
		
		Model a = am.newInstance();
		a.set("bModel", b);
		a.create();
		
		assertNoErrors(a);
		
		assertNotNull(persistService.find("a_models", a.getId()));
		assertNotNull(persistService.find("b_models", b.getId()));
		assertEquals(b.getId(), persistService.find("a_models", a.getId()).get("b_model"));
		assertEquals("bob", persistService.find("b_models", b.getId()).get("name"));
	}

	@Ignore
	@Test
	public void testHasOne_Embedded() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class", "embedded=true");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		Model b = spy(bm.newInstance());
		b.set("name", "bob");
		
		Model a = am.newInstance();
		a.set("bModel", b);
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertNotNull(persistService.find("a_models", a.getId()));
		
		// embedded, so there is no b_models...
		
		assertNotNull(persistService.find("b_models", b.getId()));
		assertEquals(b.getId(), persistService.find("a_models", a.getId()).get("b_model"));
		assertEquals("bob", persistService.find("b_models", b.getId()).get("name"));
	}

}
