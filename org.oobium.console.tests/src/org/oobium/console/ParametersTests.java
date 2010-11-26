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
package org.oobium.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.oobium.console.Parameters;

public class ParametersTests {

	@Test
	public void testNull() throws Exception {
		Parameters params = new Parameters(null);
		assertFalse(params.hasFlags());
		assertEquals(0, params.list.size());
		assertEquals(0, params.map.size());
	}
	
	@Test
	public void testEmpty() throws Exception {
		Parameters params = new Parameters("");
		assertFalse(params.hasFlags());
		assertEquals(0, params.list.size());
		assertEquals(0, params.map.size());
	}
	
	@Test
	public void testFlagsOnly() throws Exception {
		Parameters params = new Parameters("-xvf");
		assertEquals(3, params.flagCount());
		assertEquals(0, params.list.size());
		assertEquals(0, params.map.size());
	}
	
	@Test
	public void testFlagsTurnedOff() throws Exception {
		Parameters params;
		params = new Parameters("-xvf", false);
		assertEquals(0, params.flagCount());
		assertEquals(1, params.list.size());
		assertEquals(0, params.map.size());
		assertEquals("-xvf", params.list.get(0));

		params = new Parameters("-xvf.t", false);
		assertEquals(0, params.flagCount());
		assertEquals(1, params.list.size());
		assertEquals(0, params.map.size());
		assertEquals("-xvf.t", params.list.get(0));

		params = new Parameters("-xvf.t=false", false);
		assertEquals(0, params.flagCount());
		assertEquals(0, params.list.size());
		assertEquals(1, params.map.size());
		assertEquals("-xvf.t", params.map.keySet().iterator().next());
		assertEquals("false", params.map.values().iterator().next());
	}
	
	@Test
	public void testFlags() throws Exception {
		Parameters params = new Parameters("-xvf");
		assertEquals(3, params.flagCount());
		assertTrue(params.isSet('x'));
		assertTrue(params.isSet('v'));
		assertTrue(params.isSet('f'));
		
		assertFalse(params.isSet('z'));
		params.setFlag('z');
		assertTrue(params.isSet('z'));
		
		params.retainFlags('z', 'v', 'c');
		assertEquals(2, params.flagCount());
		assertFalse(params.isSet('x'));
		assertTrue(params.isSet('v'));
		assertFalse(params.isSet('f'));
		assertTrue(params.isSet('z'));
		assertFalse(params.isSet('c'));
		
		params.setFlag('Z');
		assertEquals(3, params.flagCount());
		assertTrue(params.isSet('Z'));

		params.unsetFlag('z');
		assertEquals(2, params.flagCount());
		assertTrue(params.isSet('Z'));

		params.unsetFlag('Z');
		assertEquals(1, params.flagCount());
		assertFalse(params.isSet('Z'));
	}
	
	@Test
	public void testListOnly() throws Exception {
		Parameters params = new Parameters("param1");
		assertFalse(params.hasFlags());
		assertEquals(1, params.list.size());
		assertEquals(0, params.map.size());
	}
	
	@Test
	public void testMapOnly_Colon() throws Exception {
		Parameters params = new Parameters("key1:val1");
		assertFalse(params.hasFlags());
		assertEquals(0, params.list.size());
		assertEquals(1, params.map.size());
	}
	
	@Test
	public void testMapOnly_Equals() throws Exception {
		Parameters params = new Parameters("key1=val1");
		assertFalse(params.hasFlags());
		assertEquals(0, params.list.size());
		assertEquals(1, params.map.size());
	}
	
	@Test
	public void testMaps_Mixed() throws Exception {
		Parameters params = new Parameters("key1=val1 key2:val2");
		assertFalse(params.hasFlags());
		assertEquals(0, params.list.size());
		assertEquals(2, params.map.size());
	}
	
	@Test
	public void testMapOrder() throws Exception {
		Parameters params = new Parameters("key1:val1 key2:val2 key3:val3 key4:val4 key5:val5");
		assertFalse(params.hasFlags());
		assertEquals(0, params.list.size());
		assertEquals(5, params.map.size());
		
		assertEquals("val1", params.map.get("key1"));
		assertEquals("val2", params.map.get("key2"));
		assertEquals("val3", params.map.get("key3"));
		assertEquals("val4", params.map.get("key4"));
		assertEquals("val5", params.map.get("key5"));
		
		String s = "";
		for(String key : params.map.keySet()) {
			s += key;
		}
		assertEquals("key1key2key3key4key5", s);
	}
	
	@Test
	public void testAll() throws Exception {
		Parameters params = new Parameters("-xvf param1 param2 key1=val1 key2:val2");
		assertEquals(3, params.flagCount());
		assertEquals(2, params.list.size());
		assertEquals(2, params.map.size());
		
		assertTrue(params.isSet('x'));
		assertTrue(params.isSet('v'));
		assertTrue(params.isSet('f'));
		
		assertEquals("param1", params.list.get(0));
		assertEquals("param2", params.list.get(1));
		
		assertEquals("val1", params.map.get("key1"));
		assertEquals("val2", params.map.get("key2"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMultipleFlags() throws Exception {
		new Parameters("-x -v");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFlagsAfterParams() throws Exception {
		new Parameters("param1 -xvf");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFlagsAfterEntries() throws Exception {
		new Parameters("key1:val1 -xvf");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testParamsAfterEntries() throws Exception {
		new Parameters("key1:val1 param1");
	}

	@Test
	public void testFilePaths() throws Exception {
		Parameters params;

		params = new Parameters("..");
		assertFalse(params.hasFlags());
		assertEquals(1, params.list.size());
		assertEquals(0, params.map.size());

		params = new Parameters("mypath");
		assertFalse(params.hasFlags());
		assertEquals(1, params.list.size());
		assertEquals(0, params.map.size());

		params = new Parameters("/myrootpath");
		assertFalse(params.hasFlags());
		assertEquals(1, params.list.size());
		assertEquals(0, params.map.size());

		params = new Parameters("/myrootpath/../");
		assertFalse(params.hasFlags());
		assertEquals(1, params.list.size());
		assertEquals(0, params.map.size());
	}

}
