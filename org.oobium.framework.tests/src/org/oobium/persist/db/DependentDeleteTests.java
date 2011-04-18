package org.oobium.persist.db;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.persist.Model;

public class DependentDeleteTests extends BaseDbTestCase {

	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class", "dependent=Relation.DELETE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "name1");
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", b);
		a.destroy();
		
		assertEquals(0, count("a_models"));
		assertEquals(0, count("b_models"));
		
		verify(b, never()).destroy();
		
		// double check it works even if bModel is not set
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "name1");
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 2);

		a.setId(2);
		a.destroy();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertEquals(0, count("a_models"));
		assertEquals(0, count("b_models"));
	}

	@Test
	public void testHasOne_LinkBack_OneSide() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class", "dependent=Relation.DELETE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addHasOne("aModel", "AModel.class");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(null)");
		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 1);
		persistService.executeUpdate("UPDATE a_models SET b_model=? WHERE id=?", 1, 1);

		Model b = spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);

		a.set("bModel", b);
		a.destroy();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b, never()).destroy();
	}

	@Test
	public void testHasOne_LinkBack_BothSides() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class", "dependent=Relation.DELETE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addHasOne("aModel", "AModel.class");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(null)");
		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 1);
		persistService.executeUpdate("UPDATE a_models SET b_model=? WHERE id=?", 1, 1);

		Model b = spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);

		b.set("aModel", a);
		a.set("bModel", b);
		a.destroy();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b, never()).destroy();
	}

	@Test
	public void testHasOneToOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"", "dependent=Relation.DELETE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", b);
		
		a.destroy();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b, never()).destroy();
	}
	
	@Test
	public void testHasOneToOne_FromNonKey() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"", "dependent=Relation.DELETE");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = bm.newInstance();
		b.setId(1);
		
		Model a = spy(am.newInstance());
		a.setId(1);
		a.set("bModel", b);
		
		b.destroy();
		
		assertFalse(b.getErrors().toString(), b.hasErrors());
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(a, never()).destroy();
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"", "dependent=Relation.DELETE");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = bm.newInstance();
		b.setId(1);

		Model a = spy(am.newInstance());
		a.setId(1);
		a.set("bModel", b);

		b.destroy();
		
		assertFalse(b.getErrors().toString(), b.hasErrors());
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(a, never()).destroy();
	}

	@Test
	public void testHasManyToNone() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "dependent=Relation.DELETE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__null(a,b) VALUES(?,?)", 1, 1);

		Model b = spy(bm.newInstance());
		b.setId(1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModels", new Model[] { b });

		a.destroy();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b, never()).destroy();
	}

	@Test
	public void testHasManyToMany() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "opposite=\"aModels\"", "dependent=Relation.DELETE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 1);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 2);

		Model b = spy(bm.newInstance());
		b.setId(1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModels", new Model[] { b });

		a.destroy();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		assertEquals(2, persistService.executeQueryValue("SELECT id from a_models where id=?", 2));
		assertEquals(2, persistService.executeQueryValue("SELECT id from b_models where id=?", 2));
		assertEquals(1, count("a_models__b_models___b_models__a_models"));
		
		verify(b, never()).destroy();
	}

}
