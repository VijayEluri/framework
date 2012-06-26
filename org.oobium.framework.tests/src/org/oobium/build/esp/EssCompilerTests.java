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
import org.oobium.build.esp.compiler.EspCompiler;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;

public class EssCompilerTests extends BaseEspTester {

	@Before
	public void setup() {
		LogProvider.getLogger(EspCompiler.class).setConsoleLevel(Logger.DEBUG);
		trimComments(true);
	}
	
	@Override
	protected String getFileName() {
		return "MyEss.ess";
	}
	
	@Test
	public void testEmpty() throws Exception {
		assertEquals("", asset(""));
		assertEquals(".test1{}.test2{}", asset(".test1\n.test2"));
	}
	
	@Test
	public void testImport() throws Exception {
		assertTrue(esf("import com.mydomain.MyClass").hasImport("com.mydomain.MyClass"));
	}
	
	@Test
	public void testConstructor() throws Exception {
		String esp;
		esp = "MyEss(String arg1)";
		assertTrue(esf(esp).hasVariable("arg1"));
		assertEquals("public String arg1", esf(esp).getVariable("arg1"));
		assertEquals(1, esf(esp).getConstructorCount());
		assertTrue(esf(esp).hasConstructor(0));
		assertEquals("\tpublic MyEss(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", esf(esp).getConstructor(0));
	}
	
	@Test
	public void testCssOnly() throws Exception {
		assertEquals(
				".myClass{color:red}", 
				asset(".myClass { color : red }"));

		assertEquals(
				".myClass{color:red}.myOtherClass{color:blue}",
				asset(".myClass { color: red }\n\n.myOtherClass\n\tcolor: blue"));

		assertEquals(
				"input:not([type=submit]){-moz-border-radius:8px;border-radius:8px}input[type=text]{color:grey}",
				asset(
					"input:not([type=submit])\n"+
					"\t-moz-border-radius: 8px\n"+
					"\tborder-radius: 8px\n"+
					"input[type=text]\n"+
					"\tcolor: grey"));
	}

	@Test
	public void testJava() throws Exception {
		assertEquals(
				".myClass{width:\").append(h(width * 2)).append(\"px}",
				asset(".myClass { width: ${width * 2}px }"));

		assertEquals(
				"int width = 10;",
				erndr("-int width = 10;\n\n.myClass { width: ${width * 2}px }"));
		assertEquals(
				".myClass{width:\").append(h(width * 2)).append(\"px}",
				asset("-int width = 10;\n\n.myClass { width: ${width * 2}px }"));
	}

	@Test
	public void testComments() throws Exception {
		assertEquals(
				".test1{color:green}.test3{color:blue}",
				asset("//.teasset{color: red}\n.test1{color: green}\n//.test2{color: yellow}\n.test3{color: blue}"));

		assertEquals(
				".test1{color:green}", 
				asset(".test1{color: green} // red}"));
		
		// TODO comments still need work
		
		assertEquals(
				".test1{color:green}", 
				asset(".test1\n\tcolor: green // red"));
	}
	
	@Test
	public void testX() throws Exception {
		assertEquals("#header{color:red}", asset("#header { color: red; }"));
	}

	@Test
	public void testCss() throws Exception {
		String css =
				"#header{color:black}" +
				"#header .navigation{font-size:12px}" +
				"#header .logo{width:300px}" +
				"#header .logo:hover{text-decoration:none}";
		String ess =
				"#header { color: black }\n" +
				"#header .navigation { font-size: 12px }\n" +
				"#header .logo { width: 300px }\n" +
				"#header .logo:hover { text-decoration: none }";
		assertEquals(css, asset(ess));
	}
	
	@Test
	public void testStyleLayouts() throws Exception {
		String ess1 =
				"#header { color: black }\n" +
				"#header .navigation { font-size: 12px }\n" +
				"#header .logo { width: 300px }\n" +
				"#header .logo:hover { text-decoration: none }";
		String ess2 =
				("#header {\n" +
				 "  color: black;\n" +
				 "}\n" +
				 "#header .navigation {\n" +
				 "  font-size: 12px;\n" +
				 "}\n" +
				 "#header .logo {\n" +
				 "  width: 300px;\n" +
				 "}\n" +
				 "#header .logo:hover {\n" +
				 "  text-decoration: none;\n" +
				 "}").replace("  ", "\t");
		assertEquals(asset(ess1), asset(ess2));
	}
	
	@Test
	public void testStyleDelimiters() throws Exception {
		String ess1 =
				"#header { color: black }\n" +
				"#header .navigation { font-size: 12px }\n" +
				"#header .logo { width: 300px }\n" +
				"#header .logo:hover { text-decoration: none }";
		String ess2 =
				("#header\n" +
				 "  color: black\n" +
				 "#header .navigation\n" +
				 "  font-size: 12px\n" +
				 "#header .logo\n" +
				 "  width: 300px\n" +
				 "#header .logo:hover\n" +
				 "  text-decoration: none").replace("  ", "\t");
		assertEquals(asset(ess1), asset(ess2));
	}
	
	@Test
	public void testNestedStyle() throws Exception {
		String css =
				"#header{color:black}" +
				"#header .navigation{font-size:12px}";

		String ess =
				("#header\n" +
				 "  color: black\n" +
				 "  .navigation\n" +
				 "    font-size: 12px\n").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}
	
	@Test
	public void testNestedStyle_Continuous() throws Exception {
		String css =
				"#header{color:black}" +
				"#header.logo{width:300px}";

		String ess =
				("#header\n" +
				 "  color: black\n" +
				 "  &.logo\n" +
				 "    width: 300px").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}
	
	@Test
	public void testNestedStyle_Continuous_PsuedoClass() throws Exception {
		String css =
				"#header{color:black}" +
				"#header:hover{text-decoration:none}";

		String ess =
				("#header\n" +
				 "  color: black\n" +
				 "  &:hover\n" +
				 "    text-decoration: none").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}

	@Test
	public void testMixin_MissingClass() throws Exception {
		String css =
				"#menu a{" +
				  "color:#111" +
				"}" +
				".post a{" +
				  "color:red" +
				"}";

		String ess =
				("#menu a\n" +
				 "  color: #111\n" +
				 "  .bordered\n" +
				 ".post a\n" +
				 "  color: red\n" +
				 "  .bordered").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}
	
	@Test
	public void testMixin() throws Exception {
		String css =
				".bordered{" +
				  "border-top:dotted 1px black;" +
				  "border-bottom:solid 2px black" +
				"}" +
				"#menu a{" +
				  "color:#111;" +
				  "border-top:dotted 1px black;" +
				  "border-bottom:solid 2px black" +
				"}" +
				".post a{" +
				  "color:red;" +
				  "border-top:dotted 1px black;" +
				  "border-bottom:solid 2px black" +
				"}";

		String ess =
				(".bordered\n" +
				 "  border-top: dotted 1px black\n" +
				 "  border-bottom: solid 2px black\n" +
				 "#menu a\n" +
				 "  color: #111\n" +
				 "  .bordered\n" +
				 ".post a\n" +
				 "  color: red\n" +
				 "  .bordered").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}
	
	@Test
	public void testParamMixin() throws Exception {
		String css =
				"#header{" +
				  "border-radius:4px;" +
				  "-moz-border-radius:4px;" +
				  "-webkit-border-radius:4px" +
				"}" +
				".button{" +
				  "border-radius:6px;" +
				  "-moz-border-radius:6px;" +
				  "-webkit-border-radius:6px" +
				"}";

		String ess =
				(".border-radius(int radius)\n" +
				 "  border-radius: ${radius}px\n" +
				 "  -moz-border-radius: ${radius}px\n" +
				 "  -webkit-border-radius: ${radius}px\n" +
				 "#header\n" +
				 "  .border-radius(4)\n" +
				 ".button\n" +
				 "  .border-radius(6)").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}
	
	@Test
	public void testParamMixinWithDefault() throws Exception {
		String css =
				"#header{" +
				  "border-radius:4px;" +
				  "-moz-border-radius:4px;" +
				  "-webkit-border-radius:4px" +
				"}" +
				"#footer{" +
				  "border-radius:5px;" +
				  "-moz-border-radius:5px;" +
				  "-webkit-border-radius:5px" +
				"}" +
				".button{" +
				  "border-radius:6px;" +
				  "-moz-border-radius:6px;" +
				  "-webkit-border-radius:6px" +
				"}";

		String ess =
				(".border-radius(int radius = 5)\n" +
				 "  border-radius: ${radius}px\n" +
				 "  -moz-border-radius: ${radius}px\n" +
				 "  -webkit-border-radius: ${radius}px\n" +
				 "#header\n" +
				 "  .border-radius(4)\n" +
				 "#footer\n" +
				 "  .border-radius()\n" +
				 ".button\n" +
				 "  .border-radius(6)").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}
	
	@Test
	public void testParamMixin_Empty() throws Exception {
		String css =
				".button{" +
				  "border-radius:6px;" +
				  "-moz-border-radius:6px;" +
				  "-webkit-border-radius:6px" +
				"}";

		String ess =
				(".border-radius()\n" +
				 "  border-radius: 6px\n" +
				 "  -moz-border-radius: 6px\n" +
				 "  -webkit-border-radius: 6px\n" +
				 ".button\n" +
				 "  .border-radius()").replace("  ", "\t");

		assertEquals(css, asset(ess));
	}

}
