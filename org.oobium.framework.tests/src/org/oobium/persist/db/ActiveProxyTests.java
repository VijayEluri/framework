package org.oobium.persist.db;

import static org.junit.Assert.*;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.persist.Model;

public class ActiveProxyTests extends BaseDbTestCase {

	@Test
	public void testAdd() throws Exception {
		fail("todo");
		
		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
		
		migrate(am);

		Model a = am.newInstance();
		a.set("name", "bob");
		a.create();
		
		assertFalse(a.getErrors().toString(), a.hasErrors());
		
		assertEquals("bob", persistService.executeQueryValue("SELECT name from a_models where id=?", 1));
	}
	
}
