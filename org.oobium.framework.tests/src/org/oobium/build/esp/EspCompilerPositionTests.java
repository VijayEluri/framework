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
import org.oobium.build.esp.compiler.ESourceFile;
import org.oobium.build.esp.compiler.EspCompiler;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.parser.EspBuilder;

public class EspCompilerPositionTests {

	private ESourceFile jf(String esp) {
		EspDom dom = EspBuilder.newEspBuilder("MyClass").parse(esp);
		EspCompiler compiler = EspCompiler.newEspCompiler("com.domain");
		return compiler.compile(dom);
	}
	
	@Test
	public void testImport() throws Exception {
		ESourceFile jf = jf("import com.domain.MyOtherClass");
		assertEquals("domain", jf.getSource().substring(jf.getJavaOffset(11), jf.getJavaOffset(17)));
	}
	
	@Test
	public void testConstructor() throws Exception {
		ESourceFile jf = jf("MyClass(String str, int[] array, int...varArgs)");
		assertEquals("Str", jf.getSource().substring(jf.getJavaOffset(8), jf.getJavaOffset(10) + 1));
		assertEquals(" ", jf.getSource().substring(jf.getJavaOffset(14), jf.getJavaOffset(14) + 1));
		assertEquals("g s", jf.getSource().substring(jf.getJavaOffset(13), jf.getJavaOffset(15) + 1));
		assertEquals("[]", jf.getSource().substring(jf.getJavaOffset(23), jf.getJavaOffset(24) + 1));
		assertEquals("rr", jf.getSource().substring(jf.getJavaOffset(27), jf.getJavaOffset(28) + 1));
		assertEquals("int", jf.getSource().substring(jf.getJavaOffset(33), jf.getJavaOffset(35) + 1));
		assertEquals("...", jf.getSource().substring(jf.getJavaOffset(36), jf.getJavaOffset(38) + 1));
		assertEquals("Arg", jf.getSource().substring(jf.getJavaOffset(42), jf.getJavaOffset(44) + 1));
	}
	
	@Test
	public void testJavaPartInHtml() throws Exception {
		ESourceFile jf = jf("div#{id}");
		assertEquals("id", jf.getSource().substring(jf.getJavaOffset(5), jf.getJavaOffset(6) + 1));

		jf = jf("div\n\tdiv#{id}");
		assertEquals("id", jf.getSource().substring(jf.getJavaOffset(10), jf.getJavaOffset(11) + 1));

		jf = jf("div\n\tdiv#{ \"id\" }");
		assertEquals("id", jf.getSource().substring(jf.getJavaOffset(12), jf.getJavaOffset(13) + 1));
	}
	
	@Test
	public void testJavaPartInTitle() throws Exception {
		ESourceFile jf = jf("title my{ page }Title");
		assertEquals("page", jf.getSource().substring(jf.getJavaOffset(10), jf.getJavaOffset(13) + 1));

		jf = jf("title my{ \"page\" }Title");
		assertEquals("page", jf.getSource().substring(jf.getJavaOffset(11), jf.getJavaOffset(14) + 1));
	}
	
	@Test
	public void testJavaPartInTitleAndHtml() throws Exception {
		ESourceFile jf = jf("title my{ page }\n\ndiv as { \"df\" }");
		assertEquals("page", jf.getSource().substring(jf.getJavaOffset(10), jf.getJavaOffset(13) + 1));
		assertEquals("df", jf.getSource().substring(jf.getJavaOffset(28), jf.getJavaOffset(29) + 1));
	}
	
	@Test
	public void testJavaLines() throws Exception {
		ESourceFile jf; 
		jf = jf("- if(greeting) {\n\t\tdiv hello world!\n- } else {\n\t\tdiv good bye...\n- }");
		assertEquals("greeting", jf.getSource().substring(jf.getJavaOffset(5), jf.getJavaOffset(12) + 1));
		assertEquals("else", jf.getSource().substring(jf.getJavaOffset(40), jf.getJavaOffset(43) + 1));

		jf = jf("head\n\tscript\n- if(greeting) {\n\t\tdiv hello world!\n- } else {\n\t\tdiv good bye...\n- }");
		assertEquals("greeting", jf.getSource().substring(jf.getJavaOffset(18), jf.getJavaOffset(25) + 1));
		assertEquals("else", jf.getSource().substring(jf.getJavaOffset(53), jf.getJavaOffset(56) + 1));
	}
	
	@Test
	public void testJavaLines_GetEspOffset() throws Exception {
		String esp;
		ESourceFile jf;
		esp = "- if(greeting) {\n\t\tdiv hello world!\n- } else {\n\t\tdiv good bye...\n- }";
		jf = jf(esp);
		int jo1 = jf.getJavaOffset(5);
		int jo2 = jf.getJavaOffset(12);
		assertEquals("greeting", esp.substring(jf.getEspOffset(jo1), jf.getEspOffset(jo2) + 1));
	}
	
}
