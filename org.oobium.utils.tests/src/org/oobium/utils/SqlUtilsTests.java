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
package org.oobium.utils;

import static org.junit.Assert.*;
import static org.oobium.utils.SqlUtils.*;

import org.junit.Test;

public class SqlUtilsTests {

	@Test
	public void testLimitNull() throws Exception {
		assertEquals("LIMIT 1", limit(null, 1));
	}
	
	@Test
	public void testLimitEmpty() throws Exception {
		assertEquals("LIMIT 1", limit("", 1));
	}

	@Test
	public void testLimitAlreadyLimited() throws Exception {
		assertEquals("LIMIT 1", limit("limit 2", 1));
	}

	@Test
	public void testLimitWithLimit() throws Exception {
		assertEquals("select * from a LIMIT 1", limit("select * from a limit 2", 1));
	}

	@Test
	public void testLimitWithLimitAndInclude() throws Exception {
		assertEquals("select * from a LIMIT 1 include:a", limit("select * from a limit 2 include:a", 1));
	}

	@Test
	public void testLimitAlreadyLimitedWithInclude() throws Exception {
		assertEquals("LIMIT 1 include:a", limit("limit 2 include:a", 1));
	}

	@Test
	public void testLimit() throws Exception {
		assertEquals("select * from a LIMIT 1", limit("select * from a", 1));
	}

	@Test
	public void testLimitInclude() throws Exception {
		assertEquals("LIMIT 1 include:a", limit("include:a", 1));
	}

	@Test
	public void testLimitWithInclude() throws Exception {
		assertEquals("select * from a LIMIT 1 include:b", limit("select * from a include:b", 1));
	}

	@Test
	public void testPaginateNull() throws Exception {
		assertEquals("LIMIT 0,10", paginate(null, 1, 10));
	}
	
	@Test
	public void testPaginateEmpty() throws Exception {
		assertEquals("LIMIT 0,10", paginate("", 1, 10));
	}

	@Test
	public void testPaginate() throws Exception {
		assertEquals("select * from a LIMIT 0,10", paginate("select * from a", 1, 10));
	}

	@Test
	public void testPaginateInclude() throws Exception {
		assertEquals("LIMIT 0,10 include:a", paginate("include:a", 1, 10));
	}

	@Test
	public void testPaginateWithInclude() throws Exception {
		assertEquals("select * from a LIMIT 0,10 include:b", paginate("select * from a include:b", 1, 10));
	}

}
