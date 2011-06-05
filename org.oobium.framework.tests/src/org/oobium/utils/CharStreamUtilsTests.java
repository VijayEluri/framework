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
import static org.oobium.utils.CharStreamUtils.*;

import org.junit.Test;

public class CharStreamUtilsTests {

	@Test
	public void testGetDouble() throws Exception {
		assertNull(getDouble("1".toCharArray(), 0, 1));
		assertNull(getDouble("1.0".toCharArray(), 0, 1));
		assertEquals(new Double(1.0), getDouble("1.0".toCharArray(), 0, 3));
	}
	
	@Test
	public void testGetLong() throws Exception {
		assertEquals(new Long(1), getLong("1".toCharArray(), 0, 1));
		assertEquals(new Long(-1), getLong("-1".toCharArray(), 0, 2));
		assertEquals(new Long(1), getLong("1".toCharArray(), 0, 10));
		assertEquals(new Long(1), getLong("010".toCharArray(), 1, 2));
		assertNull(getLong("1.0".toCharArray(), 0, 3));
		assertNull(getLong("bob".toCharArray(), 0, 3));
		assertEquals(new Long(0), getLong("b0b".toCharArray(), 1, 2));
		
		String maxIntPlus1 = String.valueOf((long) Integer.MAX_VALUE + 1);
		assertEquals(new Long(maxIntPlus1), getLong(maxIntPlus1.toCharArray(), 0, maxIntPlus1.length()));
	}
	
	@Test
	public void testGetInteger() throws Exception {
		assertEquals(new Integer(1), getInteger("1".toCharArray(), 0, 1));
		assertEquals(new Integer(-1), getInteger("-1".toCharArray(), 0, 2));
		assertEquals(new Integer(1), getInteger("1".toCharArray(), 0, 10));
		assertEquals(new Integer(1), getInteger("010".toCharArray(), 1, 2));
		assertNull(getInteger("1.0".toCharArray(), 0, 3));
		assertNull(getInteger("bob".toCharArray(), 0, 3));
		assertEquals(new Integer(0), getInteger("b0b".toCharArray(), 1, 2));
		
		String max = String.valueOf(Integer.MAX_VALUE);
		assertEquals(new Integer(Integer.MAX_VALUE), getInteger(max.toCharArray(), 0, max.length()));

		String maxIntPlus1 = String.valueOf((long) Integer.MAX_VALUE + 1);
		assertNull(getInteger(maxIntPlus1.toCharArray(), 0, maxIntPlus1.length()));
	}
	
	@Test
	public void testCloser() throws Exception {
		char[] ca = "(style:\"display:none\")".toCharArray();
		assertEquals(ca.length-1, closer(ca, 0, ca.length));
	}
	
}
