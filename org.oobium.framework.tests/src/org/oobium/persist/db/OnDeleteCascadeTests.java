package org.oobium.persist.db;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.sql.SQLSyntaxErrorException;
import java.sql.Timestamp;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class OnDeleteCascadeTests extends BaseDbTestCase {

	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addHasOne("aModel", "AModel.class", "onDelete=Relation.CASCADE");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "name1");
		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 1);

		Model b = spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", b);
		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b, never()).destroy();
		
		// double check it works even if bModel is not set
		persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "name1");
		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 2);

		a.setId(2);
		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 2));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 2));
	}

	@Test(expected=SQLSyntaxErrorException.class)
	public void testHasOneToOne() throws Exception {
		
		// passes Derby, fails MySQL and Postgres...
		
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"", "onDelete=Relation.CASCADE");

		migrate(am, bm);
	}
	
	@Test(expected=SQLSyntaxErrorException.class)
	public void testHasOneToOne_FromNonKey() throws Exception {
		
		// passes Derby, fails MySQL and Postgres...
		
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"", "onDelete=Relation.CASCADE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"", "onDelete=Relation.CASCADE");
		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = bm.newInstance();
		b.setId(1);

		Model a = spy(am.newInstance());
		a.setId(1);
		a.set("bModel", b);

		b.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(a, never()).destroy();
	}

}
