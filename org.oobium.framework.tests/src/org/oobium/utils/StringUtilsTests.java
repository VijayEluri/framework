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
import static org.oobium.utils.StringUtils.*;

import org.junit.Test;

public class StringUtilsTests {

	@Test
	public void testCamelCase() throws Exception {
		assertEquals("Null", 		camelCase(null));
		assertEquals("Mymodel", 	camelCase("mymodel"));
		assertEquals("MyModel", 	camelCase("my_model"));
		assertEquals("MyModel", 	camelCase("MY_MODEL"));
		assertEquals("Model", 		camelCase("MODEL"));
		assertEquals("MyModel", 	camelCase("myModel"));
		assertEquals("MyModel", 	camelCase("my model"));
		assertEquals("MyModel", 	camelCase("MY MODEL"));
		assertEquals("MyModel", 	camelCase("my  model"));
		assertEquals("MyModel", 	camelCase("MY  MODEL"));
		assertEquals("MyModel", 	camelCase("my_ Model"));
		assertEquals("MyModel", 	camelCase("MY _ MODEL"));
		assertEquals("AModel",		camelCase("AModel"));
		assertEquals("AbcModel",	camelCase("ABCModel"));
	}
	
	@Test
	public void testTitleize() throws Exception {
		assertEquals("Null", 		titleize(null));
		assertEquals("Mymodel", 	titleize("mymodel"));
		assertEquals("My Model", 	titleize("my_model"));
		assertEquals("My Model", 	titleize("MY_MODEL"));
		assertEquals("Model",		titleize("MODEL"));
		assertEquals("My Model", 	titleize("MyModel"));
		assertEquals("My Model", 	titleize("myModel"));
		assertEquals("My Model", 	titleize("my model"));
		assertEquals("My Model", 	titleize("MY MODEL"));
		assertEquals("My Model", 	titleize("my  model"));
		assertEquals("My Model", 	titleize("MY  MODEL"));
		assertEquals("My Model", 	titleize("my_ Model"));
		assertEquals("My Model", 	titleize("MY _ MODEL"));
	}
	
	@Test
	public void testUnderscore() throws Exception {
		assertEquals("null",		underscored(null));
		assertEquals("model",		underscored("model"));
		assertEquals("model",		underscored("Model"));
		assertEquals("my_model",	underscored("MyModel"));
		assertEquals("my_model",	underscored("My Model"));
		assertEquals("a_model",		underscored("A Model"));
		assertEquals("a_model",		underscored("AModel"));
		assertEquals("abc_model",	underscored("ABCModel"));
		assertEquals("com.test.ab",	underscored("com.test.Ab"));
		assertEquals("esp_files",	underscored("ESP Files"));
	}
	
	@Test
	public void testVarName() throws Exception {
		assertNull(varName((String) null));
		assertEquals("mymodel", varName("mymodel"));
		assertEquals("myModel", varName("my_model"));
		assertEquals("myModel", varName("MY_MODEL"));
		assertEquals("model",	varName("MODEL"));
		assertEquals("mYmodel", varName("MYmodel"));
		assertEquals("myModel", varName("myModel"));
		assertEquals("myModel", varName("my model"));
		assertEquals("myModel", varName("MY MODEL"));
		assertEquals("myModel", varName("my  model"));
		assertEquals("myModel", varName("MY  MODEL"));
		assertEquals("myModel", varName("my_ Model"));
		assertEquals("myModel", varName("MY _ MODEL"));
	}
	
	@Test
	public void testColumnName() throws Exception {
		assertEquals("null",		columnName(null));
		assertEquals("model",		columnName("model"));
		assertEquals("model",		columnName("Model"));
		assertEquals("my_model",	columnName("MyModel"));
		assertEquals("my_model",	columnName("My Model"));
		assertEquals("a_model",		columnName("A Model"));
		assertEquals("a_model",		columnName("AModel"));
		assertEquals("abc_model",	columnName("ABCModel"));
	}
	
	@Test
	public void testHtmlEscape() throws Exception {
		assertEquals("is a &gt; 0 &amp; a &lt; 10?", htmlEscape("is a > 0 & a < 10?"));
	}
	
	@Test
	public void testJsonEscape() throws Exception {
		assertEquals("is a \u003E 0 \u0026 a \u003C 10?", jsonEscape("is a > 0 & a < 10?"));
	}

	@Test
	public void testRange() throws Exception {
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, range(1, 5));
		assertArrayEquals(new int[] { 2, 3, 4, 5 }, range(2, 5));
		assertArrayEquals(new int[] { 3, 4, 5 }, range(3, 5, false));
		assertArrayEquals(new int[] { 3, 4 }, range(3, 5, true));
	}
	
}
