package org.oobium.persist.db;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.persist.Model;

public class UpdateTests extends BaseDbTestCase {

	@Test
	public void testAttr() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");

		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "bob");

		Model a = am.newInstance();
		a.setId(1);
		a.set("name", "joe");
		a.update();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertEquals("joe", persistService.executeQueryValue("SELECT name from a_models where id=?", 1));
	}
	
	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "bob");
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "joe");
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", 2);
		a.update();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		assertEquals(2, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
	}

	@Test
	public void testHasOneToOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 2);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", 2);
		a.update();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		assertEquals(2, persistService.executeQueryValue("SELECT b_model from a_models where id=?", a.getId()));
		assertEquals(null, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 2));
	}
	
	@Test
	public void testHasOneToOne_FromNonKey() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 2);

		Model b = bm.newInstance();
		b.setId(1);
		b.set("aModel", 2);
		b.update();
		
		assertFalse(b.getErrors().toString(), b.hasErrors());
		assertEquals(null, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(b.getId(), persistService.executeQueryValue("SELECT b_model from a_models where id=?", 2));
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", 2);
		a.update();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		assertEquals(2, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
	}

	@Test
	public void testHasManyToOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 2);

		Model b = bm.newInstance();
		b.setId(1);
		b.set("aModels", new int[] { 2 });
		b.update();
		
		assertFalse(b.getErrors().toString(), b.hasErrors());
		assertEquals(null, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 2));
	}

	@Test
	public void testHasManyToNone() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));

		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 2, 1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModels", new int[] { 2 });
		a.update();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		assertEquals(2, count("a_models__b_models___b_models__null"));
		assertEquals(2, persistService.executeQueryValue("SELECT a from a_models__b_models___b_models__null where b=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT a from a_models__b_models___b_models__null where b=?", 2));
	}

	@Test
	public void testHasManyToMany() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));

		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 2);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModels", new int[] { 2 });
		a.update();
		
		assertEquals(2, count("a_models__b_models___b_models__a_models"));
		assertEquals(2, persistService.executeQueryValue("SELECT a from a_models__b_models___b_models__a_models where b=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT a from a_models__b_models___b_models__a_models where b=?", 2));
	}

}
