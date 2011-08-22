package org.oobium.persist.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class UpdateTests extends BaseMongoTestCase {

	@Test
	public void testAttr() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
		
		Object id = persistService.insert("a_models", "name:?", "bob");

		Model a = am.newInstance();
		a.setId(id);
		a.set("name", "joe");
		a.update();
		
		assertNoErrors(a);
		
		assertNotNull("joe", persistService.find("a_models", id));
		assertEquals("joe", persistService.find("a_models", id).get("name"));
	}
	
	@Test
	public void testPartialAttr() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class").addAttr("age", "int.class");
		
		Object id = persistService.insert("a_models", "name:?,age:?", "bob", 30);

		Model a = am.newInstance();
		a.setId(id);
		a.set("name", "joe");
		a.update();
		
		assertNoErrors(a);
		
		assertNotNull(persistService.find("a_models", id));
		assertEquals("joe", persistService.find("a_models", id).get("name"));
		assertEquals(30, persistService.find("a_models", id).get("age"));
	}
	
	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");

		Object bId1 = persistService.insert("b_models", "name:bob");
		Object bId2 = persistService.insert("b_models", "name:joe");
		Object aId = persistService.insert("a_models", "bModel:?", bId1);

		Model a = am.newInstance();
		a.setId(aId);
		a.set("bModel", bId2);
		a.update();
		
		assertNoErrors(a);

		assertNotNull(persistService.find("a_models", aId));
		assertNotNull(persistService.find("a_models", aId).get("bModel"));
		assertEquals(bId2, persistService.find("a_models", aId).get("bModel"));
	}

	@Test
	public void testHasOne_Embedded() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class", "embedded=true");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");

		Object id = persistService.insert("a_models", "bModel:{name:'bob'}");

		Model b = bm.newInstance();
		b.set("name", "joe");
		
		Model a = am.newInstance();
		a.setId(id);
		a.set("bModel", b);
		a.update();
		
		assertNoErrors(a);

		assertNotNull(persistService.find("a_models", id));
		assertNotNull(persistService.find("a_models", id).get("bModel"));
		assertEquals("joe", ((Map<?,?>) persistService.find("a_models", id).get("bModel")).get("name"));
	}

	@Ignore
	@Test
	public void testHasOneToOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		Object bId1 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object bId2 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object aId1 = persistService.insert("a_models", "bModel:?", bId1);
		Object aId2 = persistService.insert("a_models", "bModel:?", bId2);

		System.out.println(persistService.find("a_models", aId1));
		System.out.println(persistService.find("a_models", aId2));
		
		Model a = am.newInstance();
		a.setId(aId1);
		a.set("bModel", bId2);
		a.update();
		
		assertNoErrors(a);

		System.out.println(persistService.find("a_models", aId1));
		System.out.println(persistService.find("a_models", aId2));

		assertNotNull(persistService.find("a_models", aId1));
		assertEquals(bId2, persistService.find("a_models", aId1).get("bModel"));
		assertNotNull(persistService.find("a_models", aId2));
		assertNull(persistService.find("a_models", aId2).get("bModel"));
	}
	
	@Ignore
	@Test
	public void testHasOneToOne_FromNonKey() throws Exception {
		DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		Object bId1 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object bId2 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object aId1 = persistService.insert("a_models", "bModel:?", bId1);
		Object aId2 = persistService.insert("a_models", "bModel:?", bId2);

		System.out.println(persistService.find("a_models", aId1));
		System.out.println(persistService.find("a_models", aId2));
		
		Model b = bm.newInstance();
		b.setId(bId1);
		b.set("aModel", aId2);
		b.update();
		
		assertNoErrors(b);

		System.out.println(persistService.find("a_models", aId1));
		System.out.println(persistService.find("a_models", aId2));

		assertNotNull(persistService.find("a_models", aId1));
		assertNull(persistService.find("a_models", aId1).get("bModel"));
		assertNotNull(persistService.find("a_models", aId2));
		assertEquals(bId1, persistService.find("a_models", aId2).get("bModel"));
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		Object bId1 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object bId2 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object aId = persistService.insert("a_models", "bModel:?", bId1);

		Model a = am.newInstance();
		a.setId(aId);
		a.set("bModel", bId2);
		a.update();
		
		assertNoErrors(a);

		assertNotNull(persistService.find("a_models", aId));
		assertEquals(bId2, persistService.find("a_models", aId).get("bModel"));
	}

	@Ignore
	@Test
	public void testHasManyToOne() throws Exception {
		DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		Object bId1 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object bId2 = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object aId1 = persistService.insert("a_models", "bModel:?", bId1);
		Object aId2 = persistService.insert("a_models", "bModel:?", bId2);

		Model b = bm.newInstance();
		b.setId(bId1);
		b.set("aModels", new Object[] { aId2 });
		b.update();
		
		assertNoErrors(b);

		assertNotNull(persistService.find("a_models", aId1));
		assertNull(persistService.find("a_models", aId1).get("bModel"));
		assertNotNull(persistService.find("a_models", aId2));
		assertEquals(bId1, persistService.find("a_models", aId2).get("bModel"));
	}

}
