package org.oobium.persist.db;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Ignore;
import org.junit.Test;
import org.oobium.persist.Model;
import org.oobium.persist.dyn.DynModel;
import org.oobium.persist.dyn.DynModels;

public class CreateTests extends BaseDbTestCase {

	@Test
	public void testAttr() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").addAttr("name", "String.class");
		
		migrate(am);

		Model a = am.newInstance();
		a.set("name", "bob");
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertEquals("bob", persistService.executeQueryValue("SELECT name from a_models where id=?", 1));
	}
	
	@Test
	public void testAttr_Empty() throws Exception {
		
		// creating a model with no attributes set currently throws an exception... should it be this way?
		
		DynModel am = DynModels.getClass(pkg, "AModel").addAttr("name", "String.class");
		
		migrate(am);

		Model a = am.newInstance();
		a.create();
		
		assertEquals(1, a.getErrorCount());
		assertTrue(a.getError(0).startsWith("can not create an empty model:"));
	}
	
	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynModel bm = DynModels.getClass(pkg, "BModel").addAttr("name", "String.class");
		
		migrate(am, bm);

		Model b = spy(bm.newInstance());
		b.set("name", "bob");
		
		Model a = am.newInstance();
		a.set("bModel", b);
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertEquals(1, persistService.executeQueryValue("SELECT b_model FROM a_models WHERE id=?", 1));
		assertEquals("bob", persistService.executeQueryValue("SELECT name FROM b_models WHERE id=?", 1));
	}

	@Test
	public void testHasOne_LinkBack_OneSide() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynModel bm = DynModels.getClass(pkg, "BModel").addHasOne("aModel", "AModel.class");
		
		migrate(am, bm);

		Model b = spy(bm.newInstance());
		Model a = am.newInstance();
		a.set("bModel", b);
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());

		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT count(*) from b_models"));
		
		verify(b, never()).create();
	}

	@Ignore
	@Test
	public void testHasOne_LinkBack_BothSides() throws Exception {
		
		// Requires 3 steps: insert AModel with null key, insert BModel, then update AModel... not yet implemented
		
		DynModel am = DynModels.getClass(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynModel bm = DynModels.getClass(pkg, "BModel").addHasOne("aModel", "AModel.class");
		
		migrate(am, bm);

//		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(null)");
//		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 1);
//		persistService.executeUpdate("UPDATE a_models SET b_model=? WHERE id=?", 1, 1);

		Model b = spy(bm.newInstance());
		Model a = am.newInstance();
		b.set("aModel", a);
		a.set("bModel", b);
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());

		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT count(*) from b_models"));
		
		verify(b, never()).create();
	}

	@Test
	public void testHasOneToOne() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		Model b = spy(bm.newInstance());
		Model a = am.newInstance();
		a.set("bModel", b);
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());

		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT count(*) from b_models"));
		
		verify(b, never()).create();
	}
	
	@Test
	public void testHasOneToOne_FromNonKey() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

//		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
//		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = bm.newInstance();
		Model a = spy(am.newInstance());
		a.set("bModel", b);
		b.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());

		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT count(*) from b_models"));
		
		verify(a, never()).create();
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		Model a = am.newInstance();
		Model b = spy(bm.newInstance());
		a.set("bModel", b);
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());

		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT count(*) from b_models"));
		
		verify(b, never()).create();
	}

	@Test
	public void testHasManyToOne() throws Exception {
		// same as testHasOnToMany, except save the from the "many" side
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		Model a = spy(am.newInstance());
		Model b = bm.newInstance();
		b.set("aModels", new Model[] { a });
		a.get("bModel");
		b.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());

		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT count(*) from b_models"));
		
		verify(a, never()).create();
	}

	@Test
	public void testHasManyToNone() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class");

		migrate(am, bm);

		Model b = spy(bm.newInstance());
		Model a = am.newInstance();
		a.set("bModels", new Model[] { b });
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());

		assertEquals(1, persistService.executeQueryValue("SELECT mk_a_model__b_models from b_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT count(*) from a_models"));
		
		verify(b, never()).create();
	}

	@Test
	public void testHasManyToMany() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");

		migrate(am, bm);

		Model b1 = spy(bm.newInstance());
		Model b2 = spy(bm.newInstance());
		Model a = am.newInstance();
		a.set("bModels", new Model[] { b1, b2 });
		a.create();
		
		assertEquals(1, persistService.executeQueryValue("SELECT id from a_models where id=?", 1));
		assertEquals(1, persistService.executeQueryValue("SELECT id from b_models where id=?", 1));
		assertEquals(2, persistService.executeQueryValue("SELECT count(*) from a_models__b_models___b_models__a_models"));
		
		verify(b1, never()).create();
		verify(b2, never()).create();
	}

}
