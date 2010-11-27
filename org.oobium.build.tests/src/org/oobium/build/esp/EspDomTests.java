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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.oobium.build.esp.EspPart.Type.*;

import org.junit.Ignore;
import org.junit.Test;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.build.esp.elements.CommentElement;
import org.oobium.build.esp.elements.ConstructorElement;
import org.oobium.build.esp.elements.HtmlElement;
import org.oobium.build.esp.elements.ImportElement;
import org.oobium.build.esp.elements.InnerTextElement;
import org.oobium.build.esp.elements.JavaElement;
import org.oobium.build.esp.parts.JavaPart;

public class EspDomTests {

	private EspDom dom(String esp) {
		return new EspDom("MyEsp", esp);
	}
	
	private EspElement elem(String esp) {
		return elem(esp, 0);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T elem(String esp, Class<T> type) {
		return (T) elem(esp, 0);
	}
	
	private EspElement elem(String esp, int index) {
		return new EspDom("MyEsp", esp).get(index);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T elem(String esp, int index, Class<T> type) {
		return (T) new EspDom("MyEsp", esp).get(index);
	}
	
	@Test
	public void testConstructor() throws Exception {
		String esp;
		esp = "MyEsp";
		assertEquals("MyEsp", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertFalse(elem(esp, ConstructorElement.class).hasArgs());

		esp = "MyEsp(";
		assertEquals("MyEsp(", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertFalse(elem(esp, ConstructorElement.class).hasArgs());

		esp = "MyEsp()";
		assertEquals("MyEsp()", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertFalse(elem(esp, ConstructorElement.class).hasArgs());

		esp = "MyEsp(String";
		assertEquals("MyEsp(String", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertTrue(elem(esp, ConstructorElement.class).hasArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("String", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertNull(elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());

		esp = "MyEsp(String ";
		assertEquals("MyEsp(String ", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertTrue(elem(esp, ConstructorElement.class).hasArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("String", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertNull(elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());

		esp = "MyEsp(String arg1)";
		assertEquals("MyEsp(String arg1)", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertTrue(elem(esp, ConstructorElement.class).hasArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("String", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg1", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());

		esp = "MyEsp(  String  arg1  )";
		assertEquals("MyEsp(  String  arg1  )", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertTrue(elem(esp, ConstructorElement.class).hasArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("String", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg1", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());

		esp = "MyEsp(String arg1,";
		assertEquals("MyEsp(String arg1,", elem(esp).getText());
		assertTrue(elem(esp).isA(ConstructorElement));
		assertTrue(elem(esp, ConstructorElement.class).hasArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("String", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg1", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());

		esp = "MyEsp(String arg1, byte[] arg2)";
		assertEquals("MyEsp(String arg1, byte[] arg2)", elem(esp).getText());
		assertEquals(2, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("String", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg1", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());
		assertEquals("byte[]", elem(esp, ConstructorElement.class).getArgs().get(1).getVarType());
		assertEquals("arg2", elem(esp, ConstructorElement.class).getArgs().get(1).getVarName());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(1).isVarArgs());

		esp = "MyEsp(int ... arg)";
		assertEquals("MyEsp(int ... arg)", elem(esp).getText());
		assertNotNull(elem(esp, ConstructorElement.class).getArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("int", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertTrue(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());

		esp = "MyEsp(int...)";
		assertEquals("MyEsp(int...)", elem(esp).getText());
		assertNotNull(elem(esp, ConstructorElement.class).getArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("int", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertNull(elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertTrue(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());

		esp = "MyEsp(...arg)";
		assertEquals("MyEsp(...arg)", elem(esp).getText());
		assertNotNull(elem(esp, ConstructorElement.class).getArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertNull(elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertTrue(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());

		esp = "MyEsp( String arg1, byte[] arg2, int...arg3 )";
		assertEquals("MyEsp( String arg1, byte[] arg2, int...arg3 )", elem(esp).getText());
		assertNotNull(elem(esp, ConstructorElement.class).getArgs());
		assertEquals(3, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("String", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg1", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());
		assertEquals("byte[]", elem(esp, ConstructorElement.class).getArgs().get(1).getVarType());
		assertEquals("arg2", elem(esp, ConstructorElement.class).getArgs().get(1).getVarName());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(1).isVarArgs());
		assertEquals("int", elem(esp, ConstructorElement.class).getArgs().get(2).getVarType());
		assertEquals("arg3", elem(esp, ConstructorElement.class).getArgs().get(2).getVarName());
		assertTrue(elem(esp, ConstructorElement.class).getArgs().get(2).isVarArgs());

		esp = "MyEsp(int arg=0)";
		assertEquals("MyEsp(int arg=0)", elem(esp).getText());
		assertNotNull(elem(esp, ConstructorElement.class).getArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("int", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());
		assertTrue(elem(esp, ConstructorElement.class).getArgs().get(0).hasDefaultValue());
		assertEquals("0", elem(esp, ConstructorElement.class).getArgs().get(0).getDefaultValue());

		esp = "MyEsp(int arg=)";
		assertEquals("MyEsp(int arg=)", elem(esp).getText());
		assertNotNull(elem(esp, ConstructorElement.class).getArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("int", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertEquals("arg", elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(0).hasDefaultValue());

		esp = "MyEsp(int =0)";
		assertEquals("MyEsp(int =0)", elem(esp).getText());
		assertNotNull(elem(esp, ConstructorElement.class).getArgs());
		assertEquals(1, elem(esp, ConstructorElement.class).getArgs().size());
		assertEquals("int", elem(esp, ConstructorElement.class).getArgs().get(0).getVarType());
		assertNull(elem(esp, ConstructorElement.class).getArgs().get(0).getVarName());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(0).isVarArgs());
		assertFalse(elem(esp, ConstructorElement.class).getArgs().get(0).hasDefaultValue());
	}
	
	@Test
	public void testElementTypes() throws Exception {
		String esp;
		esp = "div";
		assertEquals(1, dom(esp).size());
		
		esp = "div\ndiv";
		assertEquals(2, dom(esp).size());
		
		esp = "div\n\tdiv";
		assertEquals(1, dom(esp).size());
		
		esp = "div <- div//comment\ndi//v";
		assertEquals(2, dom(esp).size());

		esp = "div <- style\n\t.class1\n\t\tprop1: val1\n//comment\ndi//v";
		assertEquals(3, dom(esp).size());
	}

	@Test
	public void testGetElement() {
		String esp;
		esp = "div#id1 <- div#id2\nstyle\n\tdiv#id3\ndiv#id4";
		assertEquals("div#id1 <- div#id2", dom(esp).getPart(0).getElement().getText());
		assertEquals("div#id2", dom(esp).getPart(15).getElement().getText());
		assertEquals("style\n\tdiv#id3", dom(esp).getPart(22).getElement().getText());
		assertEquals("style\n\tdiv#id3", dom(esp).getPart(32).getElement().getText());
		assertEquals("div#id4", dom(esp).getPart(40).getElement().getText());
	}
	
	@Test
	public void testHtmlGetPart() {
		String esp;
		int offset;

		offset = 0;
		esp = "import\ndiv t1";
		assertEquals("import", dom(esp).getPart(offset++).getText());
		assertEquals("import", dom(esp).getPart(offset++).getText());
		assertEquals("import", dom(esp).getPart(offset++).getText());
		assertEquals("import", dom(esp).getPart(offset++).getText());
		assertEquals("import", dom(esp).getPart(offset++).getText());
		assertEquals("import", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(DOM));
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(InnerTextPart));
		assertTrue(dom(esp).getPart(offset++).isA(InnerTextPart));

		offset = 0;
		esp = "div#id1.c1.c2(k1:v1, k2 : v2 ):s t1 <- div#id2.c3.c4( k3 : v3 ,k4:v4):h t2\n\n\tdiv#id3.c5.c6( k5 : v5 ,k6:v6) t3";
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("id1", dom(esp).getPart(offset++).getText());
		assertEquals("id1", dom(esp).getPart(offset++).getText());
		assertEquals("id1", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("c1", dom(esp).getPart(offset++).getText());
		assertEquals("c1", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("c2", dom(esp).getPart(offset++).getText());
		assertEquals("c2", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("k1", dom(esp).getPart(offset++).getText());
		assertEquals("k1", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertEquals("v1", dom(esp).getPart(offset++).getText());
		assertEquals("v1", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("k2", dom(esp).getPart(offset++).getText());
		assertEquals("k2", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertEquals("v2", dom(esp).getPart(offset++).getText());
		assertEquals("v2", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("s", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("t1 ", dom(esp).getPart(offset++).getText());
		assertEquals("t1 ", dom(esp).getPart(offset++).getText());
		assertEquals("t1 ", dom(esp).getPart(offset++).getText());

		assertEquals("div#id1.c1.c2(k1:v1, k2 : v2 ):s t1 <- div#id2.c3.c4( k3 : v3 ,k4:v4):h t2", dom(esp).getPart(offset++).getText());
		assertEquals("div#id1.c1.c2(k1:v1, k2 : v2 ):s t1 <- div#id2.c3.c4( k3 : v3 ,k4:v4):h t2", dom(esp).getPart(offset++).getText());
		assertEquals("div#id1.c1.c2(k1:v1, k2 : v2 ):s t1 <- div#id2.c3.c4( k3 : v3 ,k4:v4):h t2", dom(esp).getPart(offset++).getText());

		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("id2", dom(esp).getPart(offset++).getText());
		assertEquals("id2", dom(esp).getPart(offset++).getText());
		assertEquals("id2", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("c3", dom(esp).getPart(offset++).getText());
		assertEquals("c3", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("c4", dom(esp).getPart(offset++).getText());
		assertEquals("c4", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("k3", dom(esp).getPart(offset++).getText());
		assertEquals("k3", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertEquals("v3", dom(esp).getPart(offset++).getText());
		assertEquals("v3", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("k4", dom(esp).getPart(offset++).getText());
		assertEquals("k4", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertEquals("v4", dom(esp).getPart(offset++).getText());
		assertEquals("v4", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("h", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("t2", dom(esp).getPart(offset++).getText());
		assertEquals("t2", dom(esp).getPart(offset++).getText());

		assertTrue(dom(esp).getPart(offset++).isA(DOM));
		assertTrue(dom(esp).getPart(offset++).isA(DOM));
		assertTrue(dom(esp).getPart(offset++).isA(DOM));

		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("id3", dom(esp).getPart(offset++).getText());
		assertEquals("id3", dom(esp).getPart(offset++).getText());
		assertEquals("id3", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("c5", dom(esp).getPart(offset++).getText());
		assertEquals("c5", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("c6", dom(esp).getPart(offset++).getText());
		assertEquals("c6", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("k5", dom(esp).getPart(offset++).getText());
		assertEquals("k5", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertEquals("v5", dom(esp).getPart(offset++).getText());
		assertEquals("v5", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("k6", dom(esp).getPart(offset++).getText());
		assertEquals("k6", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertEquals("v6", dom(esp).getPart(offset++).getText());
		assertEquals("v6", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("t3", dom(esp).getPart(offset++).getText());
		assertEquals("t3", dom(esp).getPart(offset++).getText());
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "div#id t{ es }t";
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertEquals("div", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("id", dom(esp).getPart(offset++).getText());
		assertEquals("id", dom(esp).getPart(offset++).getText());
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertEquals("t{ es }t", dom(esp).getPart(offset++).getText());
		assertEquals("{ es }", dom(esp).getPart(offset++).getText());
		assertEquals("{ es }", dom(esp).getPart(offset++).getText());
		assertEquals("es", dom(esp).getPart(offset++).getText());
		assertEquals("es", dom(esp).getPart(offset++).getText());
		assertEquals("{ es }", dom(esp).getPart(offset++).getText());
		assertEquals("{ es }", dom(esp).getPart(offset++).getText());
		assertEquals("t{ es }t", dom(esp).getPart(offset++).getText());
		assertNull(dom(esp).getPart(offset++));
	}
	
	@Test
	public void testHtmlArgs() throws Exception {
		String esp;
		esp = "div()";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertFalse(elem(esp, HtmlElement.class).hasEntries());
		
		esp = "div( )";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertFalse(elem(esp, HtmlElement.class).hasEntries());
		
		esp = "div#myDiv.myClass1.myClass2(style:\"display:none\")";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertTrue(elem(esp, HtmlElement.class).hasEntry("style"));
		assertEquals("\"display:none\"", elem(esp, HtmlElement.class).getEntryValue("style").getText());
		
		esp = "view(var1)";
		assertTrue(elem(esp, HtmlElement.class).hasArgs());
		assertFalse(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getArgs().size());
		assertEquals("var1", elem(esp, HtmlElement.class).getArgs().get(0).getText());

		esp = "view(var1, var2)";
		assertTrue(elem(esp, HtmlElement.class).hasArgs());
		assertFalse(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(2, elem(esp, HtmlElement.class).getArgs().size());
		assertEquals("var1", elem(esp, HtmlElement.class).getArgs().get(0).getText());
		assertEquals("var2", elem(esp, HtmlElement.class).getArgs().get(1).getText());

		esp = "view( var1, var2, var3 )";
		assertTrue(elem(esp, HtmlElement.class).hasArgs());
		assertFalse(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(3, elem(esp, HtmlElement.class).getArgs().size());
		assertEquals("var1", elem(esp, HtmlElement.class).getArgs().get(0).getText());
		assertEquals("var2", elem(esp, HtmlElement.class).getArgs().get(1).getText());
		assertEquals("var3", elem(esp, HtmlElement.class).getArgs().get(2).getText());
	}

	@Test
	public void testHtmlArgsAndEntries() throws Exception {
		String esp;
		esp = "form( var1, var2, var3, key1: value1, key2: value2, key3: value3 )";
		assertTrue(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(3, elem(esp, HtmlElement.class).getArgs().size());
		assertEquals("var1", elem(esp, HtmlElement.class).getArgs().get(0).getText());
		assertEquals("var2", elem(esp, HtmlElement.class).getArgs().get(1).getText());
		assertEquals("var3", elem(esp, HtmlElement.class).getArgs().get(2).getText());
		assertEquals(3, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: value1", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());
		assertEquals("key2: value2", elem(esp, HtmlElement.class).getEntries().get("key2").getText());
		assertEquals("key2", elem(esp, HtmlElement.class).getEntries().get("key2").getKey().getText());
		assertEquals("value2", elem(esp, HtmlElement.class).getEntries().get("key2").getValue().getText());
		assertEquals("key3: value3", elem(esp, HtmlElement.class).getEntries().get("key3").getText());
		assertEquals("key3", elem(esp, HtmlElement.class).getEntries().get("key3").getKey().getText());
		assertEquals("value3", elem(esp, HtmlElement.class).getEntries().get("key3").getValue().getText());
	}
	
	@Test
	public void testHtmlArgumentParts() throws Exception {
		String esp;
		esp = "div(key1: \"value1 + value2\")";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: \"value1 + value2\"", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("\"value1 + value2\"", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());

		esp = "div(key1: \", key2: value2\")";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: \", key2: value2\"", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("\", key2: value2\"", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());

		esp = "img(src:\"/software/cdatetime.png\", width:200, height:200)";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(3, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("height:200", elem(esp, HtmlElement.class).getEntries().get("height").getText());
		assertEquals("height", elem(esp, HtmlElement.class).getEntries().get("height").getKey().getText());
		assertEquals("200", elem(esp, HtmlElement.class).getEntries().get("height").getValue().getText());
		
		esp = "div(key1: {java})";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: {java}", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("{java}", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());
		assertTrue(elem(esp, HtmlElement.class).getEntryValue("key1").hasParts());
		assertEquals(1, elem(esp, HtmlElement.class).getEntryValue("key1").getParts().size());
		assertTrue(elem(esp, HtmlElement.class).getEntryValue("key1").getParts().get(0).isA(JavaPart));

		esp = "div(key1: \"{java}\")";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: \"{java}\"", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("\"{java}\"", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());
		assertTrue(elem(esp, HtmlElement.class).getEntries().get("key1").getValue().hasParts());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getParts().size());
		assertTrue(elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getParts().get(0) instanceof JavaPart);

		esp = "div(attr1:v{var3}1)";
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("attr1:v{var3}1", elem(esp, HtmlElement.class).getEntries().get("attr1").getText());
		assertEquals("attr1", elem(esp, HtmlElement.class).getEntries().get("attr1").getKey().getText());
		assertEquals("v{var3}1", elem(esp, HtmlElement.class).getEntries().get("attr1").getValue().getText());
		assertTrue(elem(esp, HtmlElement.class).getEntries().get("attr1").getValue().hasParts());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().get("attr1").getValue().getParts().size());
		assertTrue(elem(esp, HtmlElement.class).getEntries().get("attr1").getValue().getParts().get(0) instanceof JavaPart);
	}

	@Test
	public void testHtmlChildren() throws Exception {
		String esp;
		esp = "div\nspan";
		assertEquals(2, dom(esp).size());
		assertFalse(elem(esp, HtmlElement.class).hasChildren());

		esp = "div\n\tspan";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertEquals(1, elem(esp, HtmlElement.class).getChildren().size());
		assertFalse(((HtmlElement) elem(esp, HtmlElement.class).getChildren().get(0)).hasChildren());

		esp = "div\n\tdiv\n\t\tdiv";
		assertEquals(1, dom(esp).size());
		assertEquals(1, elem(esp, HtmlElement.class).getChildren().size());
		assertEquals(1, ((HtmlElement) elem(esp, HtmlElement.class).getChildren().get(0)).getChildren().size());

		esp = "div\n\tdiv\n\t\tdiv\n\tdiv";
		assertEquals(1, dom(esp).size());
		assertEquals(2, elem(esp, HtmlElement.class).getChildren().size());
		assertEquals(1, ((HtmlElement) elem(esp, HtmlElement.class).getChildren().get(0)).getChildren().size());

		esp = "div\n\t\n\tdiv\n\t\tdiv";
		assertEquals(1, dom(esp).size());
		assertEquals(1, elem(esp, HtmlElement.class).getChildren().size());
		assertEquals(1, ((HtmlElement) elem(esp, HtmlElement.class).getChildren().get(0)).getChildren().size());
	}
	
	@Test
	public void testHtmlClassNames() throws Exception {
		assertNull(elem("div", HtmlElement.class).getClassNames());
		assertNull(elem("div.", HtmlElement.class).getClassNames());
		assertNull(elem("div.(key1:val1)", HtmlElement.class).getClassNames());

		String esp;
		esp = "div.myClass";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("myClass", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		
		esp = "div.myClass1.myClass2";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(2, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("myClass1", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertEquals("myClass2", elem(esp, HtmlElement.class).getClassNames().get(1).getText());

		esp = "div#myDiv.myClass1.myClass2";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(2, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("myClass1", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertEquals("myClass2", elem(esp, HtmlElement.class).getClassNames().get(1).getText());

		esp = "div#myDiv.myClass1.myClass2(style:\"display:none\")";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(2, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("myClass1", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertEquals("myClass2", elem(esp, HtmlElement.class).getClassNames().get(1).getText());

		esp = "div.{my}Class";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("{my}Class", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertTrue(elem(esp, HtmlElement.class).getClassNames().get(0).hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getClassNames().get(0).getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().get(0).getParts().size());
		assertEquals("{my}", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0).getText());
		assertEquals("my", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0)).getSource());
		
		esp = "div.my{Cla}ss";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("my{Cla}ss", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertTrue(elem(esp, HtmlElement.class).getClassNames().get(0).hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getClassNames().get(0).getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().get(0).getParts().size());
		assertEquals("{Cla}", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0).getText());
		assertEquals("Cla", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0)).getSource());

		esp = "div.my{Class}";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("my{Class}", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertTrue(elem(esp, HtmlElement.class).getClassNames().get(0).hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getClassNames().get(0).getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().get(0).getParts().size());
		assertEquals("{Class}", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0).getText());
		assertEquals("Class", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0)).getSource());

		esp = "div.{myClass}";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("{myClass}", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertTrue(elem(esp, HtmlElement.class).getClassNames().get(0).hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getClassNames().get(0).getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().get(0).getParts().size());
		assertEquals("{myClass}", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0).getText());
		assertEquals("myClass", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0)).getSource());

		esp = "div.{my}{Class}";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("{my}{Class}", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertTrue(elem(esp, HtmlElement.class).getClassNames().get(0).hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getClassNames().get(0).getParts());
		assertEquals(2, elem(esp, HtmlElement.class).getClassNames().get(0).getParts().size());
		assertEquals("{my}", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0).getText());
		assertEquals("my", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0)).getSource());
		assertEquals("{Class}", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(1).getText());
		assertEquals("Class", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(1)).getSource());

		esp = "div.{ my }{ Class }";
		assertNotNull(elem(esp, HtmlElement.class).getClassNames());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());
		assertEquals("{ my }{ Class }", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertTrue(elem(esp, HtmlElement.class).getClassNames().get(0).hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getClassNames().get(0).getParts());
		assertEquals(2, elem(esp, HtmlElement.class).getClassNames().get(0).getParts().size());
		assertEquals("{ my }", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0).getText());
		assertEquals("my", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(0)).getSource());
		assertEquals("{ Class }", elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(1).getText());
		assertEquals("Class", ((JavaPart) elem(esp, HtmlElement.class).getClassNames().get(0).getParts().get(1)).getSource());
	}

	@Test
	public void testHtmlComment() throws Exception {
		assertTrue(elem("// comment").isA(Type.CommentElement));
		assertTrue(elem("div innerHtml// comment", HtmlElement.class).hasChildren());
		assertTrue(elem("div innerHtml// comment", HtmlElement.class).getChild(0).isA(Type.CommentElement));
		assertEquals("//comment", elem("div innerHtml//comment", HtmlElement.class).getChild(0).getText());
		assertTrue(elem("div innerHtml//comment", HtmlElement.class).getChild(0).isA(CommentElement));
		assertEquals("comment", ((CommentElement) elem("div innerHtml//comment", HtmlElement.class).getChild(0)).getComment());
		assertEquals("// comment", elem("div innerHtml// comment", HtmlElement.class).getChild(0).getText());
		assertTrue(elem("div innerHtml//comment", HtmlElement.class).getChild(0).isA(CommentElement));
		assertEquals("comment", ((CommentElement) elem("div innerHtml// comment", HtmlElement.class).getChild(0)).getComment());
		assertEquals("// comment", elem("div innerHtml // comment", HtmlElement.class).getChild(0).getText());
		assertTrue(elem("div innerHtml//comment", HtmlElement.class).getChild(0).isA(CommentElement));
		assertEquals("comment", ((CommentElement) elem("div innerHtml // comment", HtmlElement.class).getChild(0)).getComment());
		assertEquals("//comment", elem("div innerHtml //comment", HtmlElement.class).getChild(0).getText());
		assertTrue(elem("div innerHtml//comment", HtmlElement.class).getChild(0).isA(CommentElement));
		assertEquals("comment", ((CommentElement) elem("div innerHtml //comment", HtmlElement.class).getChild(0)).getComment());
		assertEquals("// comment", elem("div { \"http://mydomain.com\" } // comment", HtmlElement.class).getChild(0).getText());
		assertTrue(elem("div innerHtml//comment", HtmlElement.class).getChild(0).isA(CommentElement));
		assertEquals("comment", ((CommentElement) elem("div { \"http://mydomain.com\" } // comment", HtmlElement.class).getChild(0)).getComment());
	}

	@Test
	public void testHtmlDiv() throws Exception {
		String esp = "div";
		assertNotNull(dom(esp));
		assertFalse(dom(esp).isEmpty());
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isElementA(HtmlElement));
		assertEquals("div", elem(esp, HtmlElement.class).getTag());
	}

	@Test
	public void testHtmlEntries() throws Exception {
		String esp;
		esp = "div(: value1)";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals(": value1", elem(esp, HtmlElement.class).getEntries().get("").getText());
		assertNull(elem(esp, HtmlElement.class).getEntries().get("").getKey());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("").getValue().getText());

		esp = "div( : value1)";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals(": value1", elem(esp, HtmlElement.class).getEntries().get("").getText());
		assertNull(elem(esp, HtmlElement.class).getEntries().get("").getKey());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("").getValue().getText());

		esp = "div(key1:)";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1:", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertNull(elem(esp, HtmlElement.class).getEntries().get("key1").getValue());

		esp = "div(key1: )";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1:", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertNull(elem(esp, HtmlElement.class).getEntries().get("key1").getValue());

		esp = "div(key1: value1)";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: value1", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());

		esp = "div(key1 : value1)";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1 : value1", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());

		esp = "div(key1: value1, : value2)";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(2, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: value1", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());
		assertEquals(": value2", elem(esp, HtmlElement.class).getEntries().get("").getText());
		assertNull(elem(esp, HtmlElement.class).getEntries().get("").getKey());
		assertEquals("value2", elem(esp, HtmlElement.class).getEntries().get("").getValue().getText());

		esp = "div(key1: value1, key2: value2)";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(2, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: value1", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());
		assertEquals("key2: value2", elem(esp, HtmlElement.class).getEntries().get("key2").getText());
		assertEquals("key2", elem(esp, HtmlElement.class).getEntries().get("key2").getKey().getText());
		assertEquals("value2", elem(esp, HtmlElement.class).getEntries().get("key2").getValue().getText());

		esp = "div( key1: value1, key2: value2, key3: value3 )";
		assertNotNull(elem(esp, HtmlElement.class));
		assertFalse(elem(esp, HtmlElement.class).hasArgs());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertEquals(3, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals("key1: value1", elem(esp, HtmlElement.class).getEntries().get("key1").getText());
		assertEquals("key1", elem(esp, HtmlElement.class).getEntries().get("key1").getKey().getText());
		assertEquals("value1", elem(esp, HtmlElement.class).getEntries().get("key1").getValue().getText());
		assertEquals("key2: value2", elem(esp, HtmlElement.class).getEntries().get("key2").getText());
		assertEquals("key2", elem(esp, HtmlElement.class).getEntries().get("key2").getKey().getText());
		assertEquals("value2", elem(esp, HtmlElement.class).getEntries().get("key2").getValue().getText());
		assertEquals("key3: value3", elem(esp, HtmlElement.class).getEntries().get("key3").getText());
		assertEquals("key3", elem(esp, HtmlElement.class).getEntries().get("key3").getKey().getText());
		assertEquals("value3", elem(esp, HtmlElement.class).getEntries().get("key3").getValue().getText());
	}

	@Test
	public void testHtmlH2() throws Exception {
		String esp = "h2";
		assertNotNull(dom(esp));
		assertFalse(dom(esp).isEmpty());
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isElementA(HtmlElement));
		assertEquals("h2", elem(esp, HtmlElement.class).getTag());
	}
	
	@Test
	public void testHtmlHidden() throws Exception {
		assertTrue(elem("div:hidden", HtmlElement.class).isHidden());
		assertTrue(elem("div:hidden text", HtmlElement.class).isHidden());
		assertTrue(elem("div#myDiv:hidden", HtmlElement.class).isHidden());
		assertTrue(elem("div.myClass:hidden", HtmlElement.class).isHidden());
		assertTrue(elem("div(key1:val1):hidden", HtmlElement.class).isHidden());
		assertTrue(elem("div():hidden", HtmlElement.class).isHidden());
		assertFalse(elem("div :hidden", HtmlElement.class).isHidden());
	}
	
	@Test
	public void testHtmlId() throws Exception {
		String esp;
		
		assertNull(elem("div", HtmlElement.class).getId());
		assertNull(elem("div#", HtmlElement.class).getId());
		assertNull(elem("div#.myClass", HtmlElement.class).getId());
		assertNotNull(elem("div#myDiv", HtmlElement.class).getId());
		assertEquals("myDiv", elem("div#myDiv", HtmlElement.class).getId().getText());
		assertEquals("myDiv", elem("div#myDiv.myClass1", HtmlElement.class).getId().getText());
		assertEquals("myDiv", elem("div#myDiv(style:\"display:none\")", HtmlElement.class).getId().getText());

		esp = "div#{my}Div";
		assertNotNull(elem(esp, HtmlElement.class).getId());
		assertEquals("{my}Div", elem(esp, HtmlElement.class).getId().getText());
		assertTrue(elem(esp, HtmlElement.class).getId().hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getId().getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getId().getParts().size());
		assertEquals("{my}", elem(esp, HtmlElement.class).getId().getParts().get(0).getText());
		assertEquals("my", ((JavaPart) elem(esp, HtmlElement.class).getId().getParts().get(0)).getSource());

		esp = "div#{ my }Div";
		assertNotNull(elem(esp, HtmlElement.class).getId());
		assertEquals("{ my }Div", elem(esp, HtmlElement.class).getId().getText());
		assertTrue(elem(esp, HtmlElement.class).getId().hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getId().getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getId().getParts().size());
		assertEquals("{ my }", elem(esp, HtmlElement.class).getId().getParts().get(0).getText());
		assertEquals("my", ((JavaPart) elem(esp, HtmlElement.class).getId().getParts().get(0)).getSource());

		esp = "div#m{yDi}v";
		assertNotNull(elem(esp, HtmlElement.class).getId());
		assertEquals("m{yDi}v", elem(esp, HtmlElement.class).getId().getText());
		assertTrue(elem(esp, HtmlElement.class).getId().hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getId().getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getId().getParts().size());
		assertEquals("{yDi}", elem(esp, HtmlElement.class).getId().getParts().get(0).getText());
		assertEquals("yDi", ((JavaPart) elem(esp, HtmlElement.class).getId().getParts().get(0)).getSource());

		esp = "div#my{Div}";
		assertNotNull(elem(esp, HtmlElement.class).getId());
		assertEquals("my{Div}", elem(esp, HtmlElement.class).getId().getText());
		assertTrue(elem(esp, HtmlElement.class).getId().hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getId().getParts());
		assertEquals(1, elem(esp, HtmlElement.class).getId().getParts().size());
		assertEquals("{Div}", elem(esp, HtmlElement.class).getId().getParts().get(0).getText());
		assertEquals("Div", ((JavaPart) elem(esp, HtmlElement.class).getId().getParts().get(0)).getSource());

		esp = "div#{my}{Div}";
		assertNotNull(elem(esp, HtmlElement.class).getId());
		assertEquals("{my}{Div}", elem(esp, HtmlElement.class).getId().getText());
		assertTrue(elem(esp, HtmlElement.class).getId().hasParts());
		assertNotNull(elem(esp, HtmlElement.class).getId().getParts());
		assertEquals(2, elem(esp, HtmlElement.class).getId().getParts().size());
		assertEquals("{my}", elem(esp, HtmlElement.class).getId().getParts().get(0).getText());
		assertEquals("my", ((JavaPart) elem(esp, HtmlElement.class).getId().getParts().get(0)).getSource());
		assertEquals("{Div}", elem(esp, HtmlElement.class).getId().getParts().get(1).getText());
		assertEquals("Div", ((JavaPart) elem(esp, HtmlElement.class).getId().getParts().get(1)).getSource());
	}
	
	@Test
	public void testHtmlInnerText() throws Exception {
		assertFalse(elem("div ", HtmlElement.class).hasInnerText());
		assertEquals(" ", elem("div  ", HtmlElement.class).getInnerText().getText());
		assertEquals("  ", elem("div   ", HtmlElement.class).getInnerText().getText());
		assertEquals("/", elem("div /", HtmlElement.class).getInnerText().getText());
		assertEquals("a", elem("div a", HtmlElement.class).getInnerText().getText());
		assertEquals("innerHtml", elem("div innerHtml", HtmlElement.class).getInnerText().getText());
		assertEquals("innerHtml", elem("div innerHtml// comment", HtmlElement.class).getInnerText().getText());
		assertEquals("innerHtml ", elem("div innerHtml // comment", HtmlElement.class).getInnerText().getText());
		assertEquals(" innerHtml ", elem("div  innerHtml // comment", HtmlElement.class).getInnerText().getText());
		assertEquals("{ innerHtml }", elem("div { innerHtml }", HtmlElement.class).getInnerText().getText());
		assertEquals("{ \"innerHtml\" }", elem("div { \"innerHtml\" }", HtmlElement.class).getInnerText().getText());
		assertEquals("{ \"http://mydomain.com\" }", elem("div { \"http://mydomain.com\" }", HtmlElement.class).getInnerText().getText());
		assertEquals("{ \"http://mydomain.com\" } ", elem("div { \"http://mydomain.com\" } // comment", HtmlElement.class).getInnerText().getText());
		assertEquals(" { \"http://mydomain.com\" } ", elem("div  { \"http://mydomain.com\" } // comment", HtmlElement.class).getInnerText().getText());
		assertEquals("<a href=\"/contact\">contact me</a>", elem("span <a href=\"/contact\">contact me</a>", HtmlElement.class).getInnerText().getText());
	}
	
	@Test
	public void testInnerText() throws Exception {
		String esp;
		esp = "div start";
		assertTrue(elem(esp, HtmlElement.class).hasInnerText());
		assertEquals("start", elem(esp, HtmlElement.class).getInnerText().getText());

		esp = "div st{ar}t";
		assertTrue(elem(esp, HtmlElement.class).hasInnerText());
		assertEquals("st{ar}t", elem(esp, HtmlElement.class).getInnerText().getText());
		assertTrue(elem(esp, HtmlElement.class).getInnerText().hasParts());
		assertEquals("{ar}", elem(esp, HtmlElement.class).getInnerText().getParts().get(0).getText());

		esp = "div st{ \"ar\" }t";
		assertTrue(elem(esp, HtmlElement.class).hasInnerText());
		assertEquals("st{ \"ar\" }t", elem(esp, HtmlElement.class).getInnerText().getText());
		assertTrue(elem(esp, HtmlElement.class).getInnerText().hasParts());
		assertEquals("{ \"ar\" }", elem(esp, HtmlElement.class).getInnerText().getParts().get(0).getText());

		esp = "div st{ \"\" }t";
		assertTrue(elem(esp, HtmlElement.class).hasInnerText());
		assertEquals("st{ \"\" }t", elem(esp, HtmlElement.class).getInnerText().getText());
		assertTrue(elem(esp, HtmlElement.class).getInnerText().hasParts());
		assertEquals("{ \"\" }", elem(esp, HtmlElement.class).getInnerText().getParts().get(0).getText());

		esp = "div st{ \" }t";
		assertTrue(elem(esp, HtmlElement.class).hasInnerText());
		assertEquals("st{ \" }t", elem(esp, HtmlElement.class).getInnerText().getText());
		assertTrue(elem(esp, HtmlElement.class).getInnerText().hasParts());
		assertEquals("{ \" }t", elem(esp, HtmlElement.class).getInnerText().getParts().get(0).getText());

		esp = "div start\n\t+=";
		assertFalse(elem(esp, HtmlElement.class).hasChildren());

		esp = "div start\n\t+= ";
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertTrue(elem(esp, HtmlElement.class).getChild(0).isA(InnerTextElement));
		assertFalse(((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).hasInnerText());

		esp = "div start\n\t+=  ";
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertTrue(elem(esp, HtmlElement.class).getChild(0).isA(InnerTextElement));
		assertTrue(((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).hasInnerText());
		assertEquals(" ", ((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).getInnerText().getText());

		esp = "div start\n\t+= end";
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertTrue(elem(esp, HtmlElement.class).getChild(0).isA(InnerTextElement));
		assertTrue(((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).hasInnerText());
		assertEquals("end", ((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).getInnerText().getText());

		esp = "div start\n\t+=end";
		assertFalse(elem(esp, HtmlElement.class).hasChildren());

		esp = "div start\n\t+=  end";
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertTrue(elem(esp, HtmlElement.class).getChild(0).isA(InnerTextElement));
		assertTrue(((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).hasInnerText());
		assertEquals(" end", ((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).getInnerText().getText());

		esp = "div start\n\t+=  {end}";
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertTrue(elem(esp, HtmlElement.class).getChild(0).isA(InnerTextElement));
		assertTrue(((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).hasInnerText());
		assertEquals(" {end}", ((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).getInnerText().getText());
		assertTrue(((InnerTextElement) elem(esp, HtmlElement.class).getChild(0)).hasParts());
	}
	
	@Test
	public void testHtmlLevels() throws Exception {
		assertEquals(0, elem("div").getLevel());
		assertEquals(1, elem("\tdiv").getLevel());
		assertEquals(2, elem("\t\tdiv").getLevel());
	}
	
	@Test
	public void testHtmlSameLineChildren() throws Exception {
		String esp;
		esp = "div <- img";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertEquals(1, elem(esp, HtmlElement.class).getChildren().size());
		assertEquals("div <- img", elem(esp).getText());
		assertEquals("img", elem(esp, HtmlElement.class).getChild(0).getText());
		assertEquals("div", elem(esp, HtmlElement.class).getTag());
		assertEquals("img", ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getTag());

		esp = "div#myDiv.myClass1(key1:\"val0;val1\") <- img#myImg.myClass2(key2:val2)";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertEquals(1, elem(esp, HtmlElement.class).getChildren().size());
		assertEquals("div#myDiv.myClass1(key1:\"val0;val1\") <- img#myImg.myClass2(key2:val2)", elem(esp).getText());
		assertEquals("img#myImg.myClass2(key2:val2)", elem(esp, HtmlElement.class).getChild(0).getText());
		assertTrue(elem(esp).isA(HtmlElement));
		assertTrue(elem(esp, HtmlElement.class).getChild(0).isA(HtmlElement));
		assertEquals("div", elem(esp, HtmlElement.class).getTag());									// tags
		assertEquals("img", ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getTag());
		assertEquals("myDiv", elem(esp, HtmlElement.class).getId().getText());						// ids
		assertEquals("myImg", ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getId().getText());
		assertEquals(1, elem(esp, HtmlElement.class).getClassNames().size());						// classes
		assertEquals(1, ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getClassNames().size());
		assertEquals("myClass1", elem(esp, HtmlElement.class).getClassNames().get(0).getText());
		assertEquals("myClass2", ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getClassNames().get(0).getText());
		assertTrue(elem(esp, HtmlElement.class).hasEntries());
		assertTrue(((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).hasEntries());
		assertEquals(1, elem(esp, HtmlElement.class).getEntries().size());
		assertEquals(1, ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getEntries().size());
		assertTrue(elem(esp, HtmlElement.class).hasEntry("key1"));
		assertTrue(((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).hasEntry("key2"));
		assertEquals("\"val0;val1\"", elem(esp, HtmlElement.class).getEntryValue("key1").getText());
		assertEquals("val2", ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getEntryValue("key2").getText());
	}
	
	@Test
	public void testHtmlTag() throws Exception {
		assertEquals("div", elem("div", HtmlElement.class).getTag());
		assertEquals("div", elem("\tdiv", HtmlElement.class).getTag());
		assertEquals("div", elem("\tdiv#myDiv", HtmlElement.class).getTag());
		assertEquals("div", elem("\tdiv.myClass", HtmlElement.class).getTag());
		assertEquals("div", elem("\tdiv(key1:val1)", HtmlElement.class).getTag());
	}
	
	@Test
	public void testHtmlUppercaseTag() throws Exception {
		String esp = "DIV";
		assertTrue(dom(esp).isEmpty());
	}
	
	@Test
	public void testHtmlView() throws Exception {
		HtmlElement elem;
		elem = elem("view", HtmlElement.class);
		assertEquals("view", elem.getTag());

		elem = elem("view<MyEsp>", HtmlElement.class);
		assertEquals("view", elem.getTag());
		assertEquals("MyEsp", elem.getJavaType());

		elem = elem("view<MyEsp>(arg1)", HtmlElement.class);
		assertEquals("view", elem.getTag());
		assertEquals("MyEsp", elem.getJavaType());
		assertNull(elem.getId());
		assertTrue(elem.hasArgs());
		assertEquals(1, elem.getArgs().size());
		assertEquals("arg1", elem.getArgs().get(0).getText());

		elem = elem("view<MyEsp>(\"arg1\")", HtmlElement.class);
		assertEquals("view", elem.getTag());
		assertEquals("MyEsp", elem.getJavaType());
		assertNull(elem.getId());
		assertTrue(elem.hasArgs());
		assertEquals(1, elem.getArgs().size());
		assertEquals("\"arg1\"", elem.getArgs().get(0).getText());

		elem = elem("view<MyEsp>(\"arg1\",arg2)", HtmlElement.class);
		assertEquals("view", elem.getTag());
		assertEquals("MyEsp", elem.getJavaType());
		assertNull(elem.getId());
		assertTrue(elem.hasArgs());
		assertEquals(2, elem.getArgs().size());
		assertEquals("\"arg1\"", elem.getArgs().get(0).getText());
		assertEquals("arg2", elem.getArgs().get(1).getText());
	}

	@Test
	public void testImport() throws Exception {
		String esp;
		esp = "import com.mydomain.MyClass";
		assertNotNull(dom(esp));
		assertFalse(dom(esp).isEmpty());
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isElementA(ImportElement));
		assertEquals("com.mydomain.MyClass", ((ImportElement) elem(esp)).getImport());
		assertEquals(7, ((ImportElement) elem(esp)).getImportPart().getStart());

		esp = "import      com.mydomain.MyClass";
		assertEquals("com.mydomain.MyClass", ((ImportElement) elem(esp)).getImport());
		assertEquals(12, ((ImportElement) elem(esp)).getImportPart().getStart());

		esp = "import  static  com.mydomain.MyClass.*";
		assertEquals("com.mydomain.MyClass.*", ((ImportElement) elem(esp)).getImport());
		assertEquals(16, ((ImportElement) elem(esp)).getImportPart().getStart());
		assertTrue(((ImportElement) elem(esp)).isStatic());
	}
	
	@Test
	public void testInlineHtmlDivs() throws Exception {
		String esp;
		esp = "div<-div";
		assertEquals(1, dom(esp).size());
		assertEquals("div", elem(esp, HtmlElement.class).getTag());
		assertEquals("div", ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getTag());

		esp = "div<-";
		assertEquals(1, dom(esp).size());
		assertEquals("div", elem(esp, HtmlElement.class).getTag());

		esp = "<-div";
		assertTrue(dom(esp).isEmpty());
		
		esp = "<-";
		assertTrue(dom(esp).isEmpty());

		esp = "div <- div";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp, HtmlElement.class).hasChildren());
		assertEquals("div", elem(esp, HtmlElement.class).getTag());
		assertEquals("div", ((HtmlElement) elem(esp, HtmlElement.class).getChild(0)).getTag());
	}
	
	@Ignore
	@Test
	public void testJavaChildren() throws Exception {
		String esp;
		esp = "- line1\n- line2";
		assertEquals(2, dom(esp).size());
		assertEquals(0, elem(esp, JavaElement.class).getChildren().size());
		assertEquals("line1", elem(esp, JavaElement.class).getSource());
		assertEquals(0, elem(esp, 1, JavaElement.class).getChildren().size());
		assertEquals("line1", elem(esp, 1, JavaElement.class).getSource());

		esp = "- java1\n\tdiv\n- java2";
		assertEquals(3, dom(esp).size());
		assertEquals(1, elem(esp, JavaElement.class).getChildren().size());
		assertEquals("java1", elem(esp, JavaElement.class).getSource());
		assertTrue(elem(esp, JavaElement.class).getChildren().get(0).isA(HtmlElement));
		assertEquals("div", ((HtmlElement) elem(esp, JavaElement.class).getChildren().get(0)).getTag());

		esp = "- java1\n\t- javaChild\n- java2";
		assertEquals(3, dom(esp).size());
		assertEquals(1, elem(esp, JavaElement.class).getChildren().size());
		assertEquals("java1", elem(esp, JavaElement.class).getSource());
		assertTrue(elem(esp, JavaElement.class).getChildren().get(0).isA(JavaElement));
		assertEquals("div", ((JavaElement) elem(esp, JavaElement.class).getChildren().get(0)).getSource());

		esp = "- if(true) {\n\tdiv hello\n- } else {\n\tdiv goodbye\n- }";
		assertEquals(3, dom(esp).size());
		assertEquals(1, elem(esp, JavaElement.class).getChildren().size());
		assertTrue(elem(esp, JavaElement.class).getChildren().get(0).isA(HtmlElement));
		assertEquals("\tdiv hello", ((HtmlElement) elem(esp, JavaElement.class).getChildren().get(0)).getTag());
		assertEquals(1, elem(esp, 1, JavaElement.class).getChildren().size());
		assertTrue(elem(esp, 1, JavaElement.class).getChildren().get(0).isA(HtmlElement));
		assertEquals("\tdiv hello", ((HtmlElement) elem(esp, 1, JavaElement.class).getChildren().get(0)).getTag());
		assertEquals(0, elem(esp, 2, JavaElement.class).getChildren().size());
	}

	@Test
	public void testJavaLine() throws Exception {
		String esp;
		esp = "-";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isA(JavaElement));
		assertEquals("", elem(esp, JavaElement.class).getSource());

		esp = "- ";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isA(JavaElement));
		assertEquals("", elem(esp, JavaElement.class).getSource());

		esp = "- hello";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isA(JavaElement));
		assertEquals("hello", elem(esp, JavaElement.class).getSource());

		esp = "\t-  hello ";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isA(JavaElement));
		assertEquals("hello", elem(esp, JavaElement.class).getSource());
	}
	
	@Test
	public void testTitle() throws Exception {
		String esp;
		esp = "title hello";
		assertEquals(1, dom(esp).size());
		assertTrue(elem(esp).isA(HtmlElement));
		assertEquals("hello", elem(esp, HtmlElement.class).getInnerText().getText());
	}

	@Test
	public void testStyleGetPart() throws Exception {
		int offset;
		String esp;

		offset = 0;
		esp = "style\n\ts1{k1:v1\n\t\tk2:v2} \n\ts2{k3:v3}";
		System.out.println(esp);
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style s1{k1:v1}\n\ts2{k2:v2}";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));
		
		offset = 0;
		esp = "style\n\ts1{k1:v1}\n\ts2{k2:v2}";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style\n\ts1\n\t\tk1:v1\n\t\tk2:v2";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "head\n\tstyle\n\t\ts1{k1:v1}\n\t\ts2";
		System.out.println(esp);
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "head\n\tstyle\n\t\ts1{k1:v1;\n\t\t\tk2:v2;\n\t\t\tk3:v3}\n\t\ts2";
		System.out.println(esp);
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style s1{k1:v1; k2 : v2 ;k3:v3}";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style s1 s2 { k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style s1{k1:v1} s2{k2:v2}";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));
	}

	@Test
	public void testStyleWithComment() throws Exception {
		String esp = "/*style s1 s2 { k1: v1 }";
		for(int offset = 0; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentElement));
		}
		
		esp = "/*\tstyle s1 s2 { k1: v1 }";
		for(int offset = 0; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentElement));
		}
		
		int offset = 0;
		esp = "\t/*style s1 s2 { k1: v1 }";
		assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(DOM));
		for(offset = offset+1; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentElement));
		}
		
		offset = 0;
		esp = "st/*yle s1 s2 { k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style/* s1 s2 { k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style /*s1 s2 { k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1/* s2 { k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2/* { k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2/*{ k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 {/* k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { /*k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { k/*1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { k1:/* v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { k1: /*v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { k1: v/*1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { k1: v1 /*}";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { k1: v1 }/*";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		for( ; offset < esp.length(); offset++) {
			assertTrue(dom(esp).getPart(offset) + " @ " + offset, dom(esp).getPart(offset).isA(CommentPart));
		}
		
		offset = 0;
		esp = "style s1 s2 { /*k1:*/ v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style s1 s2 { /*k1:v1;*/ k2:v2 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style s1 s2 { k/*1:*/ v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(CommentPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "style s1 s2 { k1: v1 }";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertNull(dom(esp).getPart(offset++));
	}
	
	@Test
	public void testStyleGetPart_Partial() throws Exception {
		int offset;
		String esp;
		
		offset = 0;
		esp = "style\n\ts1\n\t\tk1";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleSelectorPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StyleChildElement));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertNull(dom(esp).getPart(offset++));
	}
	
	@Test
	public void testStyleEntryPart_GetPart() throws Exception {
		int offset;
		String esp;
		
		offset = 0;
		esp = "div(style:\"s1:k1\")";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "div(style:\"{s1:k1}\")";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "div(style:\"s1:k1; s2 : k2 \")";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "div(style:\"s1:k1; s2 : \")";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertNull(dom(esp).getPart(offset++));

		offset = 0;
		esp = "div(style:\"s1:k1;s2:\")";
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(TagPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryKeyPart));
		assertTrue(dom(esp).getPart(offset++).isA(EntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyValuePart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyNamePart));
		assertTrue(dom(esp).getPart(offset++).isA(StylePropertyPart));
		assertTrue(dom(esp).getPart(offset++).isA(StyleEntryPart));
		assertTrue(dom(esp).getPart(offset++).isA(HtmlElement));
		assertNull(dom(esp).getPart(offset++));
	}
	
}
