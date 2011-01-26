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

import org.junit.Test;
import org.oobium.build.esp.EspCompiler;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.ESourceFile;

public class EjsCompilerTests {

	private String body(String method) {
		int s1 = 0;
		while(s1 < method.length() && method.charAt(s1) != '{') {
			s1++;
		}
		s1++;
		while(s1 < method.length() && Character.isWhitespace(method.charAt(s1))) {
			s1++;
		}
		return method.substring(s1, method.length() - 3).replace("\n\t\t", "\n");
	}
	
	private String js(String ejs) {
		ESourceFile src = src(ejs);
		String str = body(src.getMethod("doRender"));
		System.out.println(src.getMethod("doRender").replace("\n\t", "\n"));
		return str;
	}
	
	private ESourceFile src(String ejs) {
		EspDom dom = new EspDom("MyEss.ejs", ejs);
		EspCompiler e2j = new EspCompiler("com.mydomain", dom);
		return e2j.compile();
	}

	@Test
	public void testEmpty() throws Exception {
		assertFalse(src("").hasMethod("render"));
	}
	
	@Test
	public void testImport() throws Exception {
		assertTrue(src("import com.mydomain.MyClass").hasImport("com.mydomain.MyClass"));
	}
	
	@Test
	public void testConstructor() throws Exception {
		String esp;
		esp = "MyEss(String arg1)";
		assertTrue(src(esp).hasVariable("arg1"));
		assertEquals("public String arg1", src(esp).getVariable("arg1"));
		assertEquals(1, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEss(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(0));
	}
	
	@Test
	public void testJsOnly() throws Exception {
		String ejs;
		ejs = "alert('hello');";
		assertEquals("__sb__.append(\"alert('hello');\");", js(ejs));

		ejs = "if(true) {\n\talert('hello');\n}";
		assertEquals("__sb__.append(\"if(true) {\\n\\talert('hello');\\n}\");", js(ejs));
	}

	@Test
	public void testJava() throws Exception {
		String ejs;
		ejs = "var size = { height: 100, width:= height * 2 };";
		assertEquals("__sb__.append(\"var size = { height: 100, width:\").append(height * 2).append(\"};\");", js(ejs));
		
		ejs = "-int width = 10;\n\nvar size = { height: 100, width:= width * 2 };";
		assertEquals("int width = 10;\n__sb__.append(\"var size = { height: 100, width:\").append(width * 2).append(\"};\");", js(ejs));

		ejs = "var height := width * 2;";
		assertEquals("__sb__.append(\"var height =\").append(width * 2).append(\";\");", js(ejs));
	}

}
