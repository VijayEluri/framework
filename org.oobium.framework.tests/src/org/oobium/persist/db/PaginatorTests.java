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
package org.oobium.persist.db;

import static org.junit.Assert.*;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.persist.Model;
import org.oobium.persist.Paginator;

public class PaginatorTests extends BaseDbTestCase {

	@Test
	public void testPaginator() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addAttr("name", "String.class");
		
		migrate(am);
		
		int perPage = 3;
		int total = 8;
		
		for(int i = 1; i <= total; i++) {
			persistService.executeUpdate("INSERT INTO a_models(name) VALUES(?)", "nameA" + i);
		}
		
		Paginator<? extends Model> paginator;
		
		int i = 1;
		for(; i <= total/perPage; i++) {
			paginator = Paginator.paginate(am.getModelClass(), i, perPage);
			assertEquals(3, paginator.size());
			assertEquals(i, paginator.getPage());
			assertEquals(perPage, paginator.getPerPage());
			assertEquals(total, paginator.getTotal());
			for(int j = 0; j < paginator.size(); j++) {
				assertEquals("nameA" + (((i-1)*perPage)+j+1), paginator.get(j).get("name"));
			}
		}
		
		paginator = Paginator.paginate(am.getModelClass(), i, perPage);
		assertEquals(total%perPage, paginator.size());
		for(int j = 0; j < paginator.size(); j++) {
			assertEquals("nameA" + (((i-1)*perPage)+j+1), paginator.get(j).get("name"));
		}

		i++;
		paginator = Paginator.paginate(am.getModelClass(), i, perPage);
		assertEquals(0, paginator.size());
	}
	
}
