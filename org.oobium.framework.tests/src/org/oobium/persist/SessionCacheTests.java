/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.oobium.persist.SessionCache.*;

import org.junit.Test;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;

public class SessionCacheTests {

	@ModelDescription()
	class TestModel extends Model{
	}
	
	@Test
	public void testCacheAdapter() throws Exception {
		TestModel model = new TestModel();
		model.setId(1);

		setCache(model);
		
		assertEquals(model, getCacheById(model.getClass(), model.getId()));
		
		expireCache();
		
		assertNull(getCacheById(model.getClass(), model.getId()));
	}
	
	@Test
	public void testCacheModel() throws Exception {
		TestModel model = new TestModel();
		model.setId(1);
		
		setCache(model);
		
		assertEquals(model, getCacheById(model.getClass(), model.getId(int.class)));
		
		expireCache();
		
		assertNull(getCacheById(model.getClass(), model.getId(int.class)));
	}
	
}
