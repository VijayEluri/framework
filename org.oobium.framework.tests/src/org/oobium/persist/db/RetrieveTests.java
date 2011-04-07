package org.oobium.persist.db;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Collection;

import org.junit.Test;
import org.oobium.persist.Model;
import org.oobium.persist.dyn.DynModel;
import org.oobium.persist.dyn.DynModels;

public class RetrieveTests extends BaseDbTestCase {

	@Test
	public void testAttr() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "bob");

		Model a = am.newInstance();
		a.setId(1);
		a.load();
		
		assertEquals("bob", a.get("name"));
	}
	
	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").addHasOne("bModel", "BModel.class");
		DynModel bm = DynModels.getClass(pkg, "BModel").addAttr("name", "String.class");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "bob");
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model a = am.newInstance();
		a.setId(1);
		a.load();
		
		assertNotNull(a.get("bModel"));
		assertEquals("bob", ((Model) a.get("bModel")).get("name"));
	}

	@Test
	public void testHasOneToOne() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		Timestamp ts = new Timestamp(System.currentTimeMillis());
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", ts);
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model a = am.newInstance();
		a.setId(1);
		a.load();
		
		assertNotNull(a.get("bModel"));
		assertTrue(ts.equals(((Model) a.get("bModel")).get("createdAt")));
	}
	
	@Test
	public void testHasOneToOne_FromNonKey() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = bm.newInstance();
		b.setId(1);
		b.load();
		
		assertNotNull(b.get("aModel"));
		assertEquals(b, ((Model) b.get("aModel")).get("bModel"));
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model a = am.newInstance();
		a.setId(1);
		a.load();
		
		assertNotNull(a.get("bModel"));
		assertEquals(1, ((Collection<?>) ((Model) a.get("bModel")).get("aModels")).size());
	}

	@Test
	public void testHasManyToOne() throws Exception {
		// same as testHasOneToMany, except save the from the "many" side
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = bm.newInstance();
		b.setId(1);
		b.load();
		
		assertEquals(1, ((Collection<?>) b.get("aModels")).size());
		assertEquals(b, ((Model) ((Collection<?>) b.get("aModels")).iterator().next()).get("bModel"));
	}

	@Test
	public void testHasManyToNone() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(mk_a_model__b_models) VALUES(?)", 1);

		Model a = am.newInstance();
		a.setId(1);
		a.load();
		
		assertEquals(1, ((Collection<?>) a.get("bModels")).size());
	}

	@Test
	public void testHasManyToMany() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 1);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 2);

		Model a = am.newInstance();
		a.setId(1);
		a.load();

		assertEquals(2, ((Collection<?>) a.get("bModels")).size());
	}

	@Test
	public void testHasManyToMany_FromB() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 1);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 1);
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 2, 2);

		Model b = bm.newInstance();
		b.setId(1);
		b.load();

		assertEquals(1, ((Collection<?>) b.get("aModels")).size());

		b = bm.newInstance();
		b.setId(2);
		b.load();

		assertEquals(2, ((Collection<?>) b.get("aModels")).size());
	}

}