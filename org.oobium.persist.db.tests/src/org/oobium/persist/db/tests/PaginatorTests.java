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
package org.oobium.persist.db.tests;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Test;
import org.oobium.persist.Paginator;
import org.oobium.persist.db.tests.models.AModel;

public class PaginatorTests extends DbPersistorTestCase {

	@Test
	public void testPaginator() throws SQLException {
		createDatabase("a");
		
		int perPage = 3;
		int total = 8;
		
		for(int i = 1; i <= total; i++) {
			service.executeUpdate("INSERT INTO a_models(a_name) VALUES(?)", "nameA" + i);
		}
		
		Paginator<AModel> paginator;
		
		int i = 1;
		for(; i <= total/perPage; i++) {
			paginator = Paginator.paginate(AModel.class, i, perPage);
			assertEquals(3, paginator.size());
			assertEquals(i, paginator.getPage());
			assertEquals(perPage, paginator.getPerPage());
			assertEquals(total, paginator.getTotal());
			for(int j = 0; j < paginator.size(); j++) {
				assertEquals("nameA" + (((i-1)*perPage)+j+1), paginator.get(j).getAName());
			}
		}
		
		paginator = Paginator.paginate(AModel.class, i, perPage);
		assertEquals(total%perPage, paginator.size());
		for(int j = 0; j < paginator.size(); j++) {
			assertEquals("nameA" + (((i-1)*perPage)+j+1), paginator.get(j).getAName());
		}

		i++;
		paginator = Paginator.paginate(AModel.class, i, perPage);
		assertEquals(0, paginator.size());
	}
	
}
