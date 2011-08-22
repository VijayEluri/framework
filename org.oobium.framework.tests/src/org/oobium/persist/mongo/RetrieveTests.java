package org.oobium.persist.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class RetrieveTests extends BaseMongoTestCase {

	@Test
	public void testAttr() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
		
		Object id = persistService.insert("a_models", "name:?", "bob");

		Model a = am.newInstance();
		a.setId(id);
		a.load();
		
		assertEquals("bob", a.get("name"));
	}
	
	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
		              DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		Object bId = persistService.insert("b_models", "name:?", "bob");
		Object aId = persistService.insert("a_models", "bModel:?", bId);

		Model a = am.newInstance();
		a.setId(aId);
		a.load();
		
		assertNotNull(a.get("bModel"));
		assertEquals("bob", ((Model) a.get("bModel")).get("name"));
	}

	@Test
	public void testHasOneToOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		              DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		long createdAt = System.currentTimeMillis();
		Object bId = persistService.insert("b_models", "createdAt:?", createdAt);
		Object aId = persistService.insert("a_models", "bModel:?", bId);

		Model a = am.newInstance();
		a.setId(aId);
		a.load();
		
		assertNotNull(a.get("bModel"));
		assertNotNull(((Model) a.get("bModel")).get("createdAt"));
		assertEquals(createdAt, ((Date) ((Model) a.get("bModel")).get("createdAt")).getTime());
	}
	
	@Ignore
	@Test
	public void testHasOneToOne_FromNonKey() throws Exception {
		              DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		Object bId = persistService.insert("b_models", "createdAt:?", new Timestamp(System.currentTimeMillis()));
		Object aId = persistService.insert("a_models", "bModel:?", bId);

		Model b = bm.newInstance();
		b.setId(aId);
		b.load();
		
		assertNotNull(b.get("aModel"));
		assertEquals(b, ((Model) b.get("aModel")).get("bModel"));
	}
	
}
