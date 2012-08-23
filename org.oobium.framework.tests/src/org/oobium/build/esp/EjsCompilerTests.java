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
package org.oobium.build.esp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EjsCompilerTests extends BaseEspTester {

	@Before
	public void setup() {
		trimComments(true);
	}
	
	@Override
	protected String getFileName() {
		return "MyEjs.ejs";
	}
	
	@Test
	public void testEmpty() throws Exception {
		assertFalse(esf("").hasMethod("render"));
	}
	
	@Test
	public void testImport() throws Exception {
		assertTrue(esf("import com.mydomain.MyClass").hasImport("com.mydomain.MyClass"));
	}
	
	@Test
	public void testIncludeExternalScript() throws Exception {
		assertEquals(
				"var test = 'hello';",
				asset("- includeExternalScript(\"test\");\n\nvar test = 'hello';"));
	}
	
	@Test
	public void testConstructor() throws Exception {
		String esp;
		esp = "MyEjs(String arg1)";
		assertTrue(esf(esp).hasVariable("arg1"));
		assertEquals("public String arg1", esf(esp).getVariable("arg1"));
		assertEquals(1, esf(esp).getConstructorCount());
		assertTrue(esf(esp).hasConstructor(0));
		assertEquals("\tpublic MyEjs(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", esf(esp).getConstructor(0));
	}
	
	@Test
	public void testJsOnly() throws Exception {
		assertEquals(
				"",
				render("alert('hello');"));
		assertEquals(
				"alert('hello');",
				asset("alert('hello');"));

		assertEquals(
				"",
				render("if(true) {\n\talert('hello');\n}"));
		assertEquals(
				"if(true) {\n\talert('hello');\n}",
				asset("if(true) {\n\talert('hello');\n}"));
	}

	@Test
	public void testWithJavaParts() throws Exception {
		assertEquals(
				"__body__.append(\"Oobium.vars.myEjs13 = \");\n" +
				"__body__.append(j(width * 2));\n" +
				"__body__.append(\";\");",
				render("var height = ${width * 2};"));
		assertEquals(
				"var height = Oobium.vars.myEjs13;",
				asset("var height = ${width * 2};"));

		assertEquals(
				"__body__.append(\"Oobium.vars.myEjs0 = \");\n" +
				"__body__.append(j(callMethod()));\n" +
				"__body__.append(\";\");",
				render("${callMethod()}"));
		assertEquals(
				"Oobium.vars.myEjs0",
				asset("${callMethod()}"));

	}
	
	@Test
	public void testWithJavaLines() throws Exception {
		assertEquals(
				"int width = 10;\n" +
				"__body__.append(\"Oobium.vars.myEjs50 = \");\n" +
				"__body__.append(j(width * 2));\n" +
				"__body__.append(\";\");",
				render("-int width = 10;\nvar size = { height: 100, width: ${width * 2} };"));
		assertEquals(
				"var size = { height: 100, width: Oobium.vars.myEjs50 };",
				asset("-int width = 10;\nvar size = { height: 100, width: ${width * 2} };"));

		assertEquals(
				"String msg = \"hello\";\n" +
				"__body__.append(\"Oobium.vars.myEjs30 = \");\n" +
				"__body__.append(j(msg));\n" +
				"__body__.append(\";\");",
				render("-String msg = \"hello\";\n\nalert(${msg});"));
		assertEquals(
				"alert(Oobium.vars.myEjs30);",
				asset("-String msg = \"hello\";\n\nalert(${msg});"));
	}

}
