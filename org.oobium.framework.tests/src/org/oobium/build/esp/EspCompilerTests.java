/*******************************************************************************
 * Copyright (c) 2010, 2012 Oobium, Inc.
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.oobium.app.AppService;
import org.oobium.app.http.Action;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.views.View;
import org.oobium.build.esp.compiler.ESourceFile;
import org.oobium.build.esp.compiler.EspCompiler;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.parser.EspBuilder;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.SimpleDynClass;
import org.oobium.logging.Logger;

public class EspCompilerTests {

	private static int count;

	@Before
	public void setup() throws Exception {
		DynClasses.reset();
	}

	private String body(String src) throws Exception {
		String page = page(src, false);
		return page.substring(page.indexOf("<body>")+6, page.indexOf("</body>"));
	}
	
	private String head(String src) throws Exception {
		String page = page(src, false);
		return page.substring(page.indexOf("<head>")+6, page.indexOf("</head>"));
	}
	
	private String page(String src) throws Exception {
		return page(src, false);
	}
	
	private String page(String src, boolean partial) throws Exception {
		return page(null, src, partial);
	}
	
	private String page(AppRouter router, String src) throws Exception {
		return page(router, src, false);
	}
	
	private String page(AppRouter router, String src, boolean partial) throws Exception {
		EspBuilder builder = EspBuilder.newEspBuilder("MyEsp.esp");
		EspDom dom = builder.parse(src);
		
		EspCompiler ec = EspCompiler.newEspCompiler("com.test"+(count++));
		ESourceFile sf = ec.compile(dom);
		
		String java = sf.getSource();
		System.out.println(java);
		
		Class<?> clazz = SimpleDynClass.getClass(sf.getCanonicalName(), java);
		View view = (View) clazz.newInstance();
		
		Logger logger = mock(Logger.class);

		AppService handler = mock(AppService.class);
		when(handler.getLogger()).thenReturn(logger);
		
		Request request = mock(Request.class);
		when(request.getHandler()).thenReturn(handler);
		if(partial) {
			when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		}
		
		Response response = 
				(router == null) ? 
						View.render(view, request) : 
							View.render(router, view, request);
		
		String html = response.getContentAsString();
		System.out.println(html);
		return html;
	}
	
	private String methodBody(String method) {
		int s1 = 0;
		while(s1 < method.length() && method.charAt(s1) != '{') {
			s1++;
		}
		s1++;
		while(s1 < method.length() && Character.isWhitespace(method.charAt(s1))) {
			s1++;
		}
		int s2 = method.length() - 3;
		if(s2 > s1) {
			return method.substring(s1, s2).replace("\n\t\t", "\n");
		}
		return "";
	}
	
	private String render(String esp) {
		ESourceFile src = src(esp);
		String str = methodBody(src.getMethod("render"));
//		System.out.println(src.getMethod("render").replace("\n\t", "\n"));
		return str;
	}
	
	private ESourceFile src(String esp) {
		EspBuilder builder = EspBuilder.newEspBuilder("MyEsp.esp");
		EspDom dom = builder.parse(esp);
		
		EspCompiler ec = EspCompiler.newEspCompiler("com.test"+(count++));
		ESourceFile sf = ec.compile(dom);

		System.out.println(sf.getSource());
		for(String e : sf.getAssets()) {
			System.out.println();
			System.out.println(e + ":");
			System.out.println("\t" + sf.getAsset(e).replace("\n", "\n\t"));
		}
		return sf;
	}
	
	@Test
	public void testComments() throws Exception {
		assertEquals("<div></div>", body("// c1\ndiv\n//c2"));
	}
	
	@Test
	public void testImport() throws Exception {
		assertTrue(src("import com.mydomain.MyClass").hasImport("com.mydomain.MyClass"));
		assertEquals("com.mydomain.MyClass", src("import com.mydomain.MyClass").getImport("com.mydomain.MyClass"));
		assertTrue(src("import com.mydomain.MyClass").getSource().contains("\nimport com.mydomain.MyClass;\n"));

		assertTrue(src("import com.mydomain.MyClass;").hasImport("com.mydomain.MyClass"));
		assertEquals("com.mydomain.MyClass", src("import com.mydomain.MyClass;").getImport("com.mydomain.MyClass"));
		assertTrue(src("import com.mydomain.MyClass;").getSource().contains("\nimport com.mydomain.MyClass;\n"));
	}
	
	@Test
	public void testConstructor() throws Exception {
		String esp;
		
		// single arg
		esp = "MyEsp(String arg1)";
		assertTrue(src(esp).hasVariable("arg1"));
		assertEquals("public String arg1", src(esp).getVariable("arg1"));
		assertEquals(1, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(0));
		
		// multiple args
		esp = "MyEsp(String arg1,  int  arg2 )";
		assertTrue(src(esp).hasVariable("arg1"));
		assertTrue(src(esp).hasVariable("arg2"));
		assertEquals("public String arg1", src(esp).getVariable("arg1"));
		assertEquals("public int arg2", src(esp).getVariable("arg2"));
		assertEquals(1, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEsp(String arg1, int arg2) {\n\t\tthis.arg1 = arg1;\n\t\tthis.arg2 = arg2;\n\t}", src(esp).getConstructor(0));
		
		// complex arg
		esp = "MyEsp(List<Map<String, Object>> arg1)";
		assertTrue(src(esp).hasVariable("arg1"));
		assertEquals("public List<Map<String, Object>> arg1", src(esp).getVariable("arg1"));
		assertEquals(1, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEsp(List<Map<String, Object>> arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(0));
		
		EspDom dom = EspBuilder.newEspBuilder("Error404.esp").parse("Error404(Exception exception)");
		ESourceFile jfile = EspCompiler.newEspCompiler("com.mydomain").compile(dom);
		assertTrue(jfile.hasVariable("exception"));
		assertEquals("public Exception exception", jfile.getVariable("exception"));
		assertEquals(1, jfile.getConstructorCount());
		assertTrue(jfile.hasConstructor(0));
		assertEquals("\tpublic Error404(Exception exception) {\n\t\tthis.exception = exception;\n\t}", jfile.getConstructor(0));
	}

	@Test
	public void testConstructors() throws Exception {
		String esp;
		esp = "MyEsp(String arg1)\nMyEsp(String arg1, int arg2)";
		assertTrue(src(esp).hasVariable("arg1"));
		assertEquals("public String arg1", src(esp).getVariable("arg1"));
		assertEquals(2, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(0));
		assertTrue(src(esp).hasConstructor(1));
		assertEquals("\tpublic MyEsp(String arg1, int arg2) {\n\t\tthis.arg1 = arg1;\n\t\tthis.arg2 = arg2;\n\t}", src(esp).getConstructor(1));
	}
	
	@Test
	public void testConstructorsWithDefaultValue() throws Exception {
		String esp;
		esp = "MyEsp(String arg1=\"hello\")";
		assertTrue(src(esp).hasVariable("arg1"));
		assertEquals("public String arg1", src(esp).getVariable("arg1"));
		assertEquals(2, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEsp() {\n\t\tthis(\"hello\");\n\t}", src(esp).getConstructor(0));
		assertTrue(src(esp).hasConstructor(1));
		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(1));

		esp = "MyEsp(String arg1, String arg2 = \"hello\")";
		assertTrue(src(esp).hasVariable("arg1"));
		assertEquals("public String arg1", src(esp).getVariable("arg1"));
		assertEquals(2, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis(arg1, \"hello\");\n\t}", src(esp).getConstructor(0));
		assertTrue(src(esp).hasConstructor(1));
		assertEquals("\tpublic MyEsp(String arg1, String arg2) {\n\t\tthis.arg1 = arg1;\n\t\tthis.arg2 = arg2;\n\t}", src(esp).getConstructor(1));

		esp = "MyEsp(String arg1, String arg2 = \"he${l}lo\")";
		assertTrue(src(esp).hasVariable("arg1"));
		assertEquals("public String arg1", src(esp).getVariable("arg1"));
		assertEquals(2, src(esp).getConstructorCount());
		assertTrue(src(esp).hasConstructor(0));
		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis(arg1, (\"he\" + (l) + \"lo\"));\n\t}", src(esp).getConstructor(0));
		assertTrue(src(esp).hasConstructor(1));
		assertEquals("\tpublic MyEsp(String arg1, String arg2) {\n\t\tthis.arg1 = arg1;\n\t\tthis.arg2 = arg2;\n\t}", src(esp).getConstructor(1));
	}
	
	@Test
	public void testTitle() throws Exception {
		assertEquals("<title>hello</title>", head("title hello"));
	}
	
	@Test
	public void testScript() throws Exception {
		assertEquals(1, src("script").getMethodCount());
		assertEquals("__body__.append(\"<script>function() { alert('hello') }</script>\");", render("script function() { alert('hello') }"));
		assertEquals("__body__.append(\"<script>function() { alert('hello') }\\n\\tfunction() { alert('goodbye') }</script>\");", render("script function() { alert('hello') }\n\tfunction() { alert('goodbye') }"));
	}
	
	@Test
	public void testScriptAfterJavaLine() throws Exception {
		assertEquals(
				"if(true)\n" +
				"\t__body__.append(\"<script>alert('hello');</script>\");",
				render("-if(true)\n\tscript alert('hello');"));
	}
	
	@Test
	public void testScriptWithEjs() throws Exception {
		assertEquals(
				"addExternalScript(new MyScripts());",
				render("script<MyScripts>"));
	}
	
	@Test
	public void testScriptWithEjsParamaterized() throws Exception {
		assertEquals(
				"addExternalScript(new MyScripts(arg1, arg2));",
				render("script<MyScripts>(arg1, arg2)"));
	}
	
	@Test
	public void testScriptWithComments() throws Exception {
		assertEquals(
				"__body__.append(\"<script>function() { $.getJSON('http://localhost'); }</script>\");",
				render("script function() { $.getJSON('http://localhost'); }"));
		
		assertEquals(
				"__body__.append(\"<script>function() { $.getJSON(\\\"http://localhost\\\"); }</script>\");",
				render("script function() { $.getJSON(\"http://localhost\"); }"));
		
		assertEquals(
				"__body__.append(\"<script>if(true) {// comment\\n\\t}</script>\");",
				render("script\n\tif(true) {// comment\n\t}"));
	}

	@Test
	public void testModels() throws Exception {
		assertEquals(
				"includeScriptModels();",
				render("models"));

		assertEquals(
				"includeScriptModels(Model.class);",
				render("models(Model.class)"));

		
		AppRouter router = mock(AppRouter.class);
		when(router.getModelRouteMap()).thenReturn(null);
		
		assertEquals(
				"<!DOCTYPE html><html>" +
				"<head>" +
				"<script>\n" +
				"window.Oobium = {};\n" +
				"Oobium.routes = {};\n" +
				"Oobium.vars = {};\n" +
				"</script>" +
				"<script type='text/javascript' src='/models.js'></script>" +
				"</head>" +
				"<body></body>" +
				"</html>",
				page(router, "models"));
	}

	@Test
	public void testDataBinding() throws Exception {
		assertEquals(
				"__body__.append(\"<div data-model=\\\"\");\n" +
				"__body__.append(includeScriptModel(member, 86));\n" +
				"__body__.append(\"\\\"></div>\");",
				render("div(data-model: member)"));

		assertEquals(
				"__body__.append(\"<div data-model=\\\"\");\n" +
				"__body__.append(includeScriptModel(member, 86));\n" +
				"__body__.append(\"\\\"><div data-field=\\\"\");\n" +
				"__body__.append(h(toJson(toMap(\"name\"))));\n" +
				"__body__.append(\"\\\"></div></div>\");",
				render("div(data-model: member) <- div(data-field: \"name\")"));

		assertEquals(
				"__body__.append(\"<div data-model=\\\"\");\n" +
				"__body__.append(includeScriptModel(member, 86));\n" +
				"__body__.append(\"\\\"><div data-field=\\\"\");\n" +
				"__body__.append(h(toJson(toMap(\"name\"))));\n" +
				"__body__.append(\"\\\"><div data-field=\\\"\");\n" +
				"__body__.append(h(toJson(toMap(\"age\"))));\n" +
				"__body__.append(\"\\\"></div></div></div>\");",
				render("div(data-model: member) <- div(data-field: \"name\") <- div(data-field: \"age\")"));

		assertEquals(
				"__body__.append(\"<div data-model=\\\"\");\n" +
				"__body__.append(includeScriptModel(member, 86));\n" +
				"__body__.append(\"\\\"><div data-field=\\\"\");\n" +
				"__body__.append(h(toJson(toMap(\"name\"))));\n" +
				"__body__.append(\"\\\"><div data-model=\\\"\");\n" +
				"__body__.append(includeScriptModel(account, 271));\n" +
				"__body__.append(\"\\\"><div data-field=\\\"\");\n" +
				"__body__.append(h(toJson(toMap(\"value\"))));\n" +
				"__body__.append(\"\\\"></div></div></div></div>\");",
				render(
						"div(data-model: member)\n" +
						"\tdiv(data-field: \"name\")\n" +
						"\t\tdiv(data-model: account)\n" +
						"\t\t\tdiv(data-field: \"value\")"));
	}
	
	@Test
	public void testDataBinding_Page() throws Exception {
		AppRouter router = mock(AppRouter.class);
		when(router.getModelRouteMap()).thenReturn(null);
		assertEquals(
				"<!DOCTYPE html><html>" +
				"<head>" +
				"<script>\n" +
				"window.Oobium = {};\n" +
				"Oobium.routes = {};\n" +
				"Oobium.vars = {};\n" +
				"Oobium.vars._null$119 = {\"type\": \"com.test" + count + ".MyEsp$1\", \"data\": {}};\n" +
				"</script>" +
				"<script type='text/javascript' src='/models.js'></script>" +
				"<script type='text/javascript' src='/models-data_binding.js'></script>" +
				"</head>" +
				"<body><div data-model=\"_null$119\"></div></body>" +
				"</html>",
				page(router,
						"import org.oobium.persist.Model\n" +
						"- Model member = new Model() {};\n" +
						"div(data-model: member)"));
	}
	
	@Test
	public void testScriptWithJava() throws Exception {
		
		// only qualifies by simple name - is this enough?
		
		assertEquals(
				"Object obj = null;\n" +
				"includeScriptEnvironment();\n" +
				"__body__.append(\"<script>Oobium.vars.myEsp37 = \");\n" +
				"__body__.append(j(obj));\n" +
				"__body__.append(\";var obj = Oobium.vars.myEsp37;</script>\");",
				render("-Object obj = null;\nscript var obj = ${obj};"));

		assertEquals(
				"int width = 10;\n" +
				"includeScriptEnvironment();\n" +
				"__body__.append(\"<script>Oobium.vars.myEsp57 = \");\n" +
				"__body__.append(j(width * 2));\n" +
				"__body__.append(\";var size = { height: 100, width: Oobium.vars.myEsp57 };</script>\");",
				render("-int width = 10;\nscript var size = { height: 100, width: ${width * 2} };"));

		assertEquals(
				"int var = 10;\n" +
				"includeScriptEnvironment();\n" +
				"__body__.append(\"<script>Oobium.vars.myEsp28 = \");\n" +
				"__body__.append(j(var));\n" +
				"__body__.append(\";alert(Oobium.vars.myEsp28);</script>\");",
				render("-int var = 10;\nscript alert(${var});"));
		
		assertEquals(
				"int var = 10;\n" +
				"includeScriptEnvironment();\n" +
				"__body__.append(\"<script>Oobium.vars.myEsp29 = \");\n" +
				"__body__.append(j(var));\n" +
				"__body__.append(\";alert(Oobium.vars.myEsp29);</script>\");",
				render("-int var = 10;\nscript\n\talert(${var});"));
		
		assertEquals(
				"<!DOCTYPE html><html>" +
				"<head><script>\n" +
				"window.Oobium = {};\n" +
				"Oobium.vars = {};\n" +
				"</script></head>" +
				"<body><script>" +
				"Oobium.vars.myEsp28 = 10;" +
				"alert(Oobium.vars.myEsp28);" +
				"</script></body>" +
				"</html>",
				page("-int var = 10;\nscript alert(${var});"));

		assertEquals(
				"String var = \"10\";\n" +
				"includeScriptEnvironment();\n" +
				"__body__.append(\"<script>Oobium.vars.myEsp34 = \");\n" +
				"__body__.append(j(var));\n" +
				"__body__.append(\";alert(Oobium.vars.myEsp34);</script>\");",
				render("-String var = \"10\";\nscript\n\talert(${var});"));
		
		assertEquals(
				"<!DOCTYPE html><html>" +
				"<head><script>\n" +
				"window.Oobium = {};\n" +
				"Oobium.vars = {};\n" +
				"</script></head>" +
				"<body><script>" +
				"Oobium.vars.myEsp33 = \"10\";" +
				"alert(Oobium.vars.myEsp33);" +
				"</script></body>" +
				"</html>",
				page("-String var = \"10\";\nscript alert(${var});"));

		assertEquals(
				"<!DOCTYPE html><html>" +
				"<head><script>\n" +
				"window.Oobium = {};\n" +
				"Oobium.vars = {};\n" +
				"</script></head>" +
				"<body><script>" +
				"Oobium.vars.myEsp28 = {\"k\": 10};" +
				"alert(Oobium.vars.myEsp28);" +
				"</script></body>" +
				"</html>",
				page("-int var = 10;\nscript alert(${\"{\\\"k\\\": 10}\"});"));

		assertEquals(
				"<!DOCTYPE html><html>" +
				"<head><script>\n" +
				"window.Oobium = {};\n" +
				"Oobium.vars = {};\n" +
				"</script></head>" +
				"<body><script>" +
				"Oobium.vars.myEsp34 = true;" +
				"alert(Oobium.vars.myEsp34);" +
				"</script></body>" +
				"</html>",
				page("-boolean var = true;\nscript alert(${var});"));
	}
	
	@Test
	public void testScriptInDomEvent() throws Exception {
		assertEquals(
				"__body__.append(\"<div onclick=\\\"\");\n" +
				"__body__.append(h(\"alert(\" + (exp) + \");\"));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div(onclick:\"alert(${exp});\""));
	}

	@Test
	public void testScriptAttr() throws Exception {
		assertEquals(
				"__body__.append(\"<div onmouseover=\\\"\");\n" +
				"__body__.append(h(\"alert('\" + (exp) + \"');\"));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div(onmouseover:\"alert('${exp}');\""));
	}

	@Test
	public void testStyle() throws Exception {
		assertEquals(
				"<div></div>", 
				body("div <- style"));
		
		assertEquals(
				"<style>.myClass{color:red}</style>", 
				body("style .myClass { color: red; }"));
		
		assertEquals(
				"<style>.myClass{color:red}</style>", 
				body("style\n\t.myClass { color: red; }"));
		
		assertEquals(
				"<div><style>.myClass{color:red}</style></div>", 
				body("div <- style .myClass { color: red; }"));
		
		assertEquals(
				"<div><style>.myClass{color:red}</style></div>", 
				body("div\n\tstyle .myClass { color: red; }"));
		
		assertEquals(
				"<div><style>.myClass{color:red}</style></div>", 
				body("div\n\tstyle\n\t\t.myClass { color: red; }"));
	}
	
	@Test
	public void testStyleMultiline() throws Exception {
		assertEquals(
				"<div><style>.myClass{color:red}</style></div>", 
				body("div\n\tstyle\n\t\t.myClass {\n\t\t\tcolor: red;\n\t\t}"));
		
		assertEquals(
				"<div><style>.myClass{color:red}</style></div>", 
				body("div\n\tstyle\n\t\t.myClass { color: red; }"));
	}
	
	@Test
	public void testStyleWithJava() throws Exception {
		assertEquals(
				"__body__.append(\"<div><style>.myClass{width:\");\n" +
				"__body__.append(h(height * 2));\n" +
				"__body__.append(\"px}</style></div>\");",
				render("div\n\tstyle\n\t\t.myClass { width: ${height * 2}px; }"));
		
		assertEquals(
				"String pageWidth = \"860px\";\n__body__.append(\"<div><style>.myClass{width:\");\n" +
				"__body__.append(h(pageWidth));\n" +
				"__body__.append(\";color:red}</style></div>\");",
				render("- String pageWidth = \"860px\";\ndiv\n\tstyle\n\t\t.myClass\n\t\t\twidth: ${pageWidth}\n\t\t\tcolor: red"));
	}
	
	@Test
	public void testStyleWithComment() throws Exception {
		assertEquals(
				"__body__.append(\"<style>.myClass{color:red}</style>\");",
				render("style .myClass { /* color:blue; */ color: red; }"));
		
		assertEquals(
				"__body__.append(\"<style>.myClass{color:red}</style>\");",
				render("style .myClass { /*\ncolor:blue;\n*/ color: red; }"));
	}
	
	@Test
	public void testStyleAfterJavaLine() throws Exception {
		assertEquals("if(true)\n\t__body__.append(\"<style>td{border:0}</style>\");",
				render("-if(true)\n\tstyle td { border: 0 }"));
	}
	
	@Test
	public void testStyleWithEss() throws Exception {
		assertEquals(
				"addExternalStyle(MyStyles.class);",
				render("style<MyStyles>"));
	}
	
	@Test
	public void testStyleInDomWithJava() throws Exception {
		assertEquals(
				"__body__.append(\"<div style=\\\"\");\n" +
				"__body__.append(h(\"color: \" + var));\n" +
				"__body__.append(\"\\\"></div>\");",
				render("div(style:\"color: $var\""));
		
		assertEquals(
				"__body__.append(\"<div style=\\\"\");\n" +
				"__body__.append(h(\"color: \" + (blue * 2)));\n" +
				"__body__.append(\"\\\"></div>\");",
				render("div(style:\"color: ${blue * 2}\""));
	}

	@Test
	public void testScriptInHead() throws Exception {
		assertEquals("<script>function { alert('hello'); }</script>", head("head <- script function { alert('hello'); }"));

		assertEquals("<script>function { alert('hello'); }</script>", head("head\n\tscript function { alert('hello'); }"));

		assertEquals("addExternalScript(myFile);", render("head <- script(myFile)"));

		assertEquals("addExternalScript(\"myFile.js\");", render("head <- script(\"myFile.js\")"));

		assertEquals("__head__.append(\"<script>function { alert('hello'); }\\n\\tfunction { alert('goodbye'); }</script>\");",
				render("head <- script function { alert('hello'); }\n\tfunction { alert('goodbye'); }"));
	}
	
	@Test
	public void testScriptInHeadWithEjs() throws Exception {
		assertEquals(
				"addExternalScript(new MyScripts());",
				render("head <- script<MyScripts>"));
	}
	
	@Test
	public void testStyleInHead() throws Exception {
		assertEquals(
				"__head__.append(\"<style>.myClass{color:red}</style>\");",
				render("head <- style .myClass { color: red; }"));

		assertEquals(
				"__head__.append(\"<style>.myClass{color:red}</style>\");",
				render("head\n\tstyle .myClass { color: red; }"));

		assertEquals(
				"__head__.append(\"<style>.myClass1{color:red}.myClass2{color:blue}</style>\");",
				render("head <- style .myClass1 { color: red; }\n\t.myClass2 { color: blue; }"));

		assertEquals(
				"__head__.append(\"<style>.myClass1{color:red}.myClass2{color:blue}</style>\");",
				render("head\n\tstyle\n\t\t.myClass1\n\t\t\tcolor: red\n\t\t.myClass2\n\t\t\tcolor: blue"));

		assertEquals("addExternalStyle(myFile);",
				render("head <- style(myFile)"));
		
		assertEquals("addExternalStyle(\"myFile.css\");",
				render("head <- style(\"myFile.css\")"));
	}
	
	@Test
	public void testStyleInHeadWithEss() throws Exception {
		assertEquals(
				"addExternalStyle(MyStyles.class);",
				render("head <- style<MyStyles>"));
	}
	
	@Test
	public void testEmpty() throws Exception {
		assertEquals(0, src("").getMethodCount());
	}
	
	@Test
	public void testHtmlDiv() throws Exception {
		assertEquals(
				"__body__.append(\"<div id=\\\"myDiv\\\" class=\\\"class1 class2\\\" attr1=\\\"\");\n" +
				"__body__.append(h(someMethod(arg1, arg2)));\n" +
				"__body__.append(\"\\\" attr2=\\\"string\\\">text</div>\");",
				render("div#myDiv.class1.class2(attr1: someMethod(arg1, arg2), attr2: \"string\") text"));

		assertEquals(
				"<div id=\"myDiv\" class=\"class1 class2\" attr1=\"value1\">text</div>",
				body("div#myDiv.class1.class2(attr1:\"value1\") text"));

		assertEquals(
				"__body__.append(\"<div id=\\\"myDiv\\\" class=\\\"class1 class2\\\" attr1=\\\"\");\n" +
				"__body__.append(h(value1));\n" +
				"__body__.append(\"\\\">text</div>\");",
				render("div#myDiv.class1.class2(attr1:value1) text"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"myDiv\\\" class=\\\"class1 class2\\\" attr1=\\\"\");\n" +
				"__body__.append(h(\"value\" + \"1\"));\n" +
				"__body__.append(\"\\\">text</div>\");",
				render("div#myDiv.class1.class2(attr1: \"value\" + \"1\") text"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"myDiv\\\" class=\\\"class1 class2\\\" attr1=\\\"\");\n" +
				"__body__.append(h(\"say \" + var + \" \" + (exp) + \" times\"));\n" +
				"__body__.append(\"\\\">text</div>\");",
				render("div#myDiv.class1.class2(attr1:\"say $var ${exp} times\") text"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"myDiv\\\" class=\\\"class1 class2\\\" attr1=\\\"\");\n" +
				"__body__.append(h((var1) + \"\\n\" + (var2)));\n" +
				"__body__.append(\"\\\">text</div>\");",
				render("div#myDiv.class1.class2(attr1:\"${var1}\\n${var2}\") text"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"myDiv\\\" class=\\\"class1 class2\\\" attr1=\\\"\");\n" +
				"__body__.append(h(\"say \" + var + \" \" + (10*i) + \" times\"));\n" +
				"__body__.append(\"\\\">text</div>\");",
				render("div#myDiv.class1.class2(attr1:\"say $var ${10*i} times\") text"));
	}

	@Test
	public void testMarkupWithConditionalAttribute() throws Exception {
		assertEquals(
				"__body__.append(\"<div\");\n" +
				"if(test) {\n" +
				"\t__body__.append(\" attr=\\\"value\\\"\");\n" +
				"}\n" +
				"__body__.append(\"></div>\");",
				render("div(attr(test): \"value\")"));

		assertEquals(
				"__body__.append(\"<div\");\n" +
				"if(test) {\n" +
				"\t__body__.append(\" data-attr=\\\"value\\\"\");\n" +
				"}\n" +
				"__body__.append(\"></div>\");",
				render("div(data-attr(test): \"value\")"));
	}

	@Test
	public void testMarkupWithArrayLiteral() throws Exception {
		assertEquals(
				"__body__.append(\"<div attr=\\\"\");\n" +
				"__body__.append(h(new String[] { \"1.0\", \"1.5\", \"2.0\" }));\n" +
				"__body__.append(\"\\\"></div>\");",
				render("div(attr: new String[] { \"1.0\", \"1.5\", \"2.0\" })"));
	}
	
	@Test
	public void testHtmlNestedDivs() throws Exception {
		assertEquals(
				"__body__.append(\"<div><div><div></div></div></div>\");", 
				render("div\n\tdiv\n\t\tdiv"));
		
		assertEquals(
				"__body__.append(\"<div><div><div></div></div></div>\");", 
				render("div\n\t\n\tdiv\n\t\tdiv"));
		
		assertEquals(
				"__body__.append(\"<div><div><div></div></div></div>\");", 
				render("div\n\n\tdiv\n\t\tdiv")); // blank lines
	}
	
	@Test
	public void testHtmlInlinedDivs() throws Exception {
		assertEquals(
				"__body__.append(\"<div><div><div></div></div></div>\");", 
				render("div <- div <- div"));
		
		assertEquals(
				"__body__.append(\"<div><div><div></div></div></div>\");", 
				render("div<- div <-div"));

		assertEquals(
				"__body__.append(\"<a><b><c></c></b><d><e></e></d></a>\");", 
				render(
						"a\n" +
						"\tb <- c\n" +
						"\td <- e"));
	}
	
	@Test
	public void testHtmlDivWithQuotesInInnerHtml() throws Exception {
		assertEquals(
				"__body__.append(\"<span><a href=\\\"/contact\\\">contact me</a></span>\");",
				render("span <a href=\"/contact\">contact me</a>"));
	}
	
	@Test
	public void testHtmlDivWithStyle() throws Exception {
		assertEquals("__body__.append(\"<div style=\\\"color:red\\\">text</div>\");", render("div(style:\"color:red\") text"));
	}
	
	@Test
	public void testHtmlDivHidden() throws Exception {
		assertEquals(
				"__body__.append(\"<div style=\\\"display:none\\\">text</div>\");", 
				render("div|hide text"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"myDiv\\\" style=\\\"display:none\\\">text</div>\");", 
				render("div#myDiv|hide text"));
		
		assertEquals(
				"__body__.append(\"<div style=\\\"color:red;display:none\\\">text</div>\");", 
				render("div|hide(style:\"color:red\") text"));
	}
	
	@Test
	public void testHtmlSingleLineMultiWithStyles() throws Exception {
		assertEquals(
				"__body__.append(\"<div style=\\\"float:left;width:200px\\\"><img height=\\\"200\\\" width=\\\"200\\\" src=\\\"/software/cdatetime.png\\\"></img></div>\");",
				render("div(style:\"float:left;width:200px\") <- img(src:\"/software/cdatetime.png\", width:\"200\", height:\"200\")"));
	}

	@Test
	public void testHtmlDivWithJavaPart() throws Exception {
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(h(var1));\n" +
				"__body__.append(\"Div\\\"></div>\");", 
				render("div#{var1}Div"));
		
		assertEquals(
				"__body__.append(\"<div class=\\\"a\");\n" +
				"__body__.append(h(var2));\n" +
				"__body__.append(\"Class\\\"></div>\");", 
				render("div.a{var2}Class"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(h(var1));\n" +
				"__body__.append(\"Div\\\" class=\\\"a\");\n" +
				"__body__.append(h(var2));\n" +
				"__body__.append(\"Class\\\" attr1=\\\"\");\n" +
				"__body__.append(h(\"v\" + (var3) + \"1\"));\n" +
				"__body__.append(\"\\\">t\");\n" +
				"__body__.append(h(var4));\n" +
				"__body__.append(\"xt</div>\");",
				render("div#{var1}Div.a{var2}Class(attr1:\"v${var3}1\") t{var4}xt"));
	}
	
	@Test
	public void testJavaParts() throws Exception {
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(h(id));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div#{id}"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(h(nid));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div#{nid}"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(h(n(id)));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div#{n(id)}"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(n(id));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div#{n id}"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(h(id));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div#{h id}"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(j(id));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div#{j id}"));
		
		assertEquals(
				"__body__.append(\"<div id=\\\"\");\n" +
				"__body__.append(id);\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div#{r id}"));

		assertEquals(
				"__body__.append(\"<div>\");\n" +
				"__body__.append(h(id));\n" +
				"__body__.append(\"</div>\");", 
				render("div {id}"));
	}
		
	@Test
	public void testCommentInJavaPart() throws Exception {
		assertEquals(
				"__body__.append(\"<div>te//st</div>\");", 
				render("div {\"te//st\"}"));
	}

	@Test
	public void testGStringInJavaPart() throws Exception {
		assertEquals(
				"__body__.append(\"<div class=\\\"\");\n" +
				"__body__.append(h((clazz) + \" selected\"));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div.{\"${clazz} selected\"}"));

		assertEquals(
				"__body__.append(\"<div class=\\\"\");\n" +
				"__body__.append(h(isPath(p) ? (clazz) + \" selected\" : clazz));\n" +
				"__body__.append(\"\\\"></div>\");", 
				render("div.{isPath(p) ? \"${clazz} selected\" : clazz}"));
	}

	@Test
	public void testStringInGStringNotSupported() throws Exception {
		assertEquals(
				"__body__.append(\"<div style=\\\"\");\n" +
				"__body__.append(h(\"display:\" +  + \"block\"}\")));\n" +
				"__body__.append(\"\\\"></div>\");",
				render("div(style:\"display:${\"block\"}\")"));
	}
	
	@Test
	public void testJavaLines() throws Exception {
		assertEquals(
				"if(hasParam(\"goto\")) {\n" +
				"\t__body__.append(\"<div></div>\");\n" +
				"}",
				render("- if(hasParam(\"goto\")) {\n\tdiv\n- }"));

		assertEquals(
				"String text = (\"say \" + var1 + \"!\");",
				render("- String text = \"say $var1!\";"));
		
		assertEquals(
				"String text = (\"the \" + fox + \" jumped over the \" + dog);", 
				render("- String text = \"the $fox jumped over the $dog\";"));
		
		assertEquals(
				"String text = (fox + \" jumped over the \" + dog);", 
				render("- String text = \"$fox jumped over the $dog\";"));
		
		assertEquals(
				"String text = (\"asd\" + (fgh) + \"jkl\");", 
				render("- String text = \"asd${fgh}jkl\";"));
	}
	
	@Test
	public void testJavaIfStatementWithoutBraces() throws Exception {
		assertEquals(
				"if(true)\n\t__body__.append(\"<div>hello</div>\");", 
				render("-if(true)\n\t\tdiv hello"));
		
		assertEquals(
				"if(true)\n\t__body__.append(\"<div>hello</div>\");\nelse\n\t__body__.append(\"<div>goodbye</div>\");", 
				render("-if(true)\n\t\tdiv hello\n-else\n\tdiv goodbye"));
		
		assertEquals(
				"if(true)\n\t__body__.append(\"<div>hello</div>\");\n__body__.append(\"<div>goodbye</div>\");", 
				render("-if(true)\n\t\tdiv hello\ndiv goodbye"));
		
		assertEquals(
				"if(true)\n\t__body__.append(\"<div>hello</div>\");\nelse\n\t__body__.append(\"<div>goodbye</div>\");\n__body__.append(\"<div>what?</div>\");", 
				render("-if(true)\n\t\tdiv hello\n-else\n\tdiv goodbye\ndiv what?"));
	}
	
	@Test
	public void testJavaLinesAfterFirstElement() throws Exception {
		assertEquals(
				"if(true) {\n" +
				"\t__body__.append(\"<div>hello</div>\");\n" +
				"} else {\n" +
				"\t__body__.append(\"<div>goodbye</div>\");\n" +
				"}", 
				render("head\n- if(true) {\n\t\tdiv hello\n- } else {\n\t\tdiv goodbye\n- }"));
		
		assertEquals(
				"if(true) {\n" +
				"\t__body__.append(\"<div>hello</div><div>bye</div>\");\n" +
				"} else {\n" +
				"\t__body__.append(\"<div>goodbye</div>\");\n" +
				"}", 
				render("head\n- if(true) {\n\t\tdiv hello\n\t\tdiv bye\n- } else {\n\t\tdiv goodbye\n- }"));
		
		assertEquals(
				"int i = 0;\nint j = 0;", 
				render("head\n- int i = 0;\n- int j = 0;"));
	}
	
	@Test
	public void testView() throws Exception {
		assertEquals("yield(new MyView());", render("view<MyView>"));
		assertEquals("yield(new MyView(arg1, arg2));", render("view<MyView>(arg1,arg2)"));
		assertEquals("yield(new MyView(arg1, arg2));", render("view<MyView>( arg1 , arg2  )"));
		assertEquals("yield(new MyView(\"arg1\", arg2));", render("view<MyView>(\"arg1\", arg2)"));
		assertEquals("yield(new MyView(\"arg1\", arg2));\nyield(new MyView(\"arg3\", arg4));", render("view<MyView>(\"arg1\", arg2)\nview<MyView>(\"arg3\", arg4)"));
		assertEquals("if(i == 0) {\n\tyield(new MyView());\n}", render("- if(i == 0) {\n\tview<MyView>\n- }"));
	}
	
	@Test
	public void testImg() throws Exception {
		assertEquals(
				"__body__.append(\"<img src=\\\"/software/cdatetime.png\\\"></img>\");", 
				render("img(\"/software/cdatetime.png\""));
		
		assertEquals(
				"__body__.append(\"<img src=\\\"/software/cdatetime.png\\\"></img>\");", 
				render("img(src:\"/software/cdatetime.png\""));
		
		assertEquals(
				"__body__.append(\"<img height=\\\"200\\\" width=\\\"200\\\" src=\\\"/software/cdatetime.png\\\"></img>\");",
				render("img(src:\"/software/cdatetime.png\", width:\"200\", height:\"200\")"));
		
		assertEquals(
				"__body__.append(\"<img height=\\\"\");\n" +
				"__body__.append(h(h));\n" +
				"__body__.append(\"\\\" width=\\\"\");\n" +
				"__body__.append(h(h/2));\n" +
				"__body__.append(\"\\\" src=\\\"/software/cdatetime.png\\\"></img>\");",
				render("img(src:\"/software/cdatetime.png\", width:h/2, height:h)"));
		
		assertEquals(
				"__body__.append(\"<img src=\\\"\");\n" +
				"__body__.append(h(\"/\" + (image) + \".png\"));\n" +
				"__body__.append(\"\\\"></img>\");", 
				render("img(src:\"/${image}.png\")"));
	}
	
	@Test
	public void testLink() throws Exception {
		assertEquals(
				"__body__.append(\"<a href=\\\"/home\\\">/home</a>\");", 
				render("a(\"/home\")"));
		
		assertEquals(
				"__body__.append(\"<a href=\\\"/home\\\"></a>\");", 
				render("a(href:\"/home\")"));

		assertEquals(
				"__body__.append(\"<a href=\\\"\");\n" +
				"__body__.append(h(pathTo(something)));\n" +
				"__body__.append(\"\\\">something</a>\");", 
				render("a(pathTo(something)) something"));

		assertEquals(
				"__body__.append(\"<a href=\\\"\");\n" +
				"__body__.append(h(pathTo(something) + \"?test\"));\n" +
				"__body__.append(\"\\\">something</a>\");", 
				render("a(pathTo(something) + \"?test\") something"));

		assertEquals(
				"__body__.append(\"<a href=\\\"http://mydomain.com/home\\\">http://mydomain.com/home</a>\");", 
				render("a(\"http://mydomain.com/home\")"));
		
		assertEquals(
				"__body__.append(\"<a href=\\\"http://mydomain.com/home\\\"></a>\");", 
				render("a(href:\"http://mydomain.com/home\")"));

		// two vars - convert to pathTo form (convenience for html forms)
		assertEquals(
				"__body__.append(\"<a href=\\\"\").append(pathTo(member, showNew)).append(\"\\\">New Member</a>\");", 
				render("a(member, showNew)"));
		
		assertEquals(
				"__body__.append(\"<a href=\\\"\").append(pathTo(member, showNew)).append(\"\\\">New</a>\");", 
				render("a(member, showNew) New"));
		
		assertTrue(src("a(member, showNew)").hasStaticImport(Action.class.getCanonicalName() + ".showNew"));

		assertEquals("__body__.append(\"<a href=\\\"\").append(pathTo(member, destroy)).append(\"\\\" onclick=\\\"if(confirm('Are you sure?')) {var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'post';f.action = '\").append(pathTo(member, destroy)).append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '_method');m.setAttribute('value', 'delete');f.appendChild(m);f.submit();}return false;\\\">Destroy Member</a>\");",
				render("a(member, destroy, confirm: \"Are you sure?\")"));

		// unknown action for second variable - pass it through so we get a compiler error and can fix it
		assertEquals("__body__.append(\"<a href=\\\"\").append(pathTo(member, new)).append(\"\\\">New</a>\");", render("a(member, new) New"));
	}

	@Test
	public void testLinkWithDeleteMethod() throws Exception {
		assertEquals(
				"__body__.append(\"<a href=\\\"/logout\\\" onclick=\\\"var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'post';f.action = '\").append(\"/logout\").append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '_method');m.setAttribute('value', 'delete');f.appendChild(m);f.submit();return false;\\\">/logout</a>\");", 
				render("a(\"/logout\", method: \"delete\")"));
	}

	@Test
	public void testMessages() throws Exception {
		assertEquals("messagesBlock(__body__);", render("messages"));
	}
	
	@Test
	public void testButton() throws Exception {
		assertEquals(
				"__body__.append(\"<button></button>\");", 
				render("button"));
		
		assertEquals(
				"__body__.append(\"<button href=\\\"/home\\\">/home</button>\");", 
				render("button(\"/home\""));
		
		assertEquals(
				"__body__.append(\"<button href=\\\"\").append(pathTo(member, show)).append(\"\\\">Show Member</button>\");", 
				render("button(member, show)"));
		
		assertEquals(
				"__body__.append(\"<button href=\\\"\").append(pathTo(Member.class, showAll)).append(\"\\\">All Members</button>\");", 
				render("button(Member.class, showAll)"));

		assertEquals(
				"__body__.append(\"<button href=\\\"\").append(pathTo(member, create)).append(\"\\\" onclick=\\\"var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'post';f.action = '\").append(pathTo(member, create)).append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '');m.setAttribute('value', '');f.appendChild(m);f.submit();return false;\\\">Create</button>\");",
				render("button(member, create) Create"));

		assertEquals(
				"__body__.append(\"<button href=\\\"\").append(pathTo(member, update)).append(\"\\\" onclick=\\\"var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'post';f.action = '\").append(pathTo(member, update)).append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '_method');m.setAttribute('value', 'put');f.appendChild(m);var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '');m.setAttribute('value', '');f.appendChild(m);f.submit();return false;\\\">Update</button>\");",
				render("button(member, update) Update"));

		assertEquals(
				"__body__.append(\"<button href=\\\"\").append(pathTo(member, destroy)).append(\"\\\" onclick=\\\"var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'post';f.action = '\").append(pathTo(member, destroy)).append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '_method');m.setAttribute('value', 'delete');f.appendChild(m);f.submit();return false;\\\">Destroy</button>\");", 
				render("button(member, destroy) Destroy"));
	}

	@Test
	public void testData() throws Exception {
		assertEquals(
				"__body__.append(\"<div data-test=\\\"value\\\"></div>\");",
				render("div(data-test: \"value\""));
	}
	
	@Test
	public void testDate() throws Exception {
		assertEquals(
				"__body__.append(\"<span>\");\n" +
				"dateTimeTags(__body__, \"datetime\", \"MMM/dd/yyyy\");\n" +
				"__body__.append(\"</span>\");",
				render("date"));
	}
	
	@Test
	public void testDateInForm() throws Exception {
		assertEquals(
				"__body__.append(\"<span>\");\n" +
				"dateTimeTags(__body__, \"datetime\", \"MMM/dd/yyyy\", new Date());\n" +
				"__body__.append(\"</span>\");",
			render("date(new Date())"));
		
		assertEquals(
				"__body__.append(\"<span>\");\n" +
				"dateTimeTags(__body__, \"datetime\", \"dd/MM/yy\", new Date());\n" +
				"__body__.append(\"</span>\");",
			render("date(new Date(), format: \"dd/MM/yy\")"));

		assertEquals(
				"String formModelName$0 = \"post\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(post, post.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"post\\\">\");\n" +
				"if(!post.isNew()) {\n" +
				"\t__body__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
				"}\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(post));\n" +
				"__body__.append(\"\\\" /><span id=\\\"\").append(formModelName$0).append(\"[publishedAt]\\\"\");\n" +
				"if(post.hasErrors(\"publishedAt\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(post.getError(\"publishedAt\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[publishedAt]\\\">\");\n" +
				"dateTimeTags(__body__, h(formModelName$0 + \"[\" + \"publishedAt\" + \"]\"), \"MMM/dd/yyyy\", post.getPublishedAt());\n" +
				"__body__.append(\"</span></form>\");",
			render("form(post)\n\tdate(\"publishedAt\")"));
		
		assertEquals(
				"String formModelName$0 = \"post\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(post, post.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"post\\\">\");\n" +
				"if(!post.isNew()) {\n" +
				"\t__body__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
				"}\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(post));\n" +
				"__body__.append(\"\\\" /><span id=\\\"\").append(formModelName$0).append(\"[\");\n" +
				"__body__.append(f(publishedAt));\n" +
				"__body__.append(\"]\\\"\");\n" +
				"if(post.hasErrors(publishedAt)) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(post.getError(publishedAt)).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[\");\n" +
				"__body__.append(f(publishedAt));\n" +
				"__body__.append(\"]\\\">\");\n" +
				"dateTimeTags(__body__, h(formModelName$0 + \"[\" + publishedAt + \"]\"), \"MMM/dd/yyyy\", post.get(publishedAt));\n" +
				"__body__.append(\"</span></form>\");",
			render("form(post)\n\tdate(publishedAt)"));
	}
	
	@Test
	public void testFile() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"file\\\" />\");", 
				render("file"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\" enctype=\\\"multipart/form-data\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"file\\\" /></form>\");", 
				render("form(member, create)\n\tfile"));
	}

	@Test
	public void testFields() throws Exception {
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(h(member));\n" +
				"__body__.append(\"\\\" />\");",
			render("fields(member)"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, member.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"post\\\">\");\n" +
				"if(!member.isNew()) {\n" +
				"\t__body__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
				"}\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" />\");\n" +
				"String formModelName$14 = \"parent\";\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$14).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(parent));\n" +
				"__body__.append(\"\\\" /></form>\");",
			render("form(member)\n\tfields(parent)"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, member.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"post\\\">\");\n" +
				"if(!member.isNew()) {\n" +
				"\t__body__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
				"}\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" />\");\n" +
				"String formModelName$14 = \"parent\";\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$14).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(parent));\n" +
				"__body__.append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$14).append(\"[\");\n" +
				"__body__.append(f(name));\n" +
				"__body__.append(\"]\\\"\");\n" +
				"if(parent.hasErrors(name)) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(parent.getError(name)).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$14).append(\"[\");\n" +
				"__body__.append(f(name));\n" +
				"__body__.append(\"]\\\" value=\\\"\");\n" +
				"__body__.append(f(parent.get(name)));\n" +
				"__body__.append(\"\\\" /></form>\");",
			render("form(member)\n\tfields(parent)\n\t\ttext(name)"));
	}
	
	@Test
	public void testForm() throws Exception {
		assertEquals(
				"__body__.append(\"<form></form>\");", 
				render("form"));
		
		assertEquals(
				"__body__.append(\"<form id=\\\"myForm\\\"></form>\");", 
				render("form#myForm"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, member.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"post\\\">\");\n" +
				"if(!member.isNew()) {\n" +
				"\t__body__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
				"}\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, action: create)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, update)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, action: update)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, destroy)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"DELETE\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, action: destroy)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, method: post)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, update)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, method: put)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, destroy)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"DELETE\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, method: delete)"));
		
		assertEquals(
				"__body__.append(\"<form action=\\\"/test\\\" method=\\\"post\\\"></form>\");",
				render("form(action: \"/test\", method: \"post\")"));
		
		assertEquals(
				"__body__.append(\"<form action=\\\"/test\\\" method=\\\"\");\n" +
				"__body__.append(f(post));\n" +
				"__body__.append(\"\\\"></form>\");",
				render("form(action: \"/test\", method: post)"));
		
		assertEquals(
				"__body__.append(\"<form action=\\\"\");\n" +
				"__body__.append(f(pathTo(\"sessions\")));\n" +
				"__body__.append(\"\\\" method=\\\"post\\\"></form>\");",
				render("form(action: pathTo(\"sessions\"), method: \"post\")"));
	
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /></form>\");",
				render("form(member, create)"));
	}

	private String line(String in) {
		return in.substring(0, in.indexOf('\n'));
	}
	
	@Test
	public void testFormWithExplicitType() throws Exception {
		assertEquals(
				"Member formModel$0 = object;",
				line(render("form<Member>(object)")));
	}
	
	@Test
	public void testFormWithImpliedType() throws Exception {
		assertEquals(
				"Member formModel$0 = new Member();",
				line(render("form(new Member())")));

		assertEquals(
				"Comment formModel$0 = new Comment().setMember(member).setWorkout(workout);",
				line(render("form(new Comment().setMember(member).setWorkout(workout))")));
	}
	
	@Test
	public void testFormWithFileAndHasManyRouting() throws Exception {
		assertEquals(
				"Comment formModel$0 = new Comment();\n" +
				"String formModelName$0 = \"comment\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(post, \"comments\")).append(\"\\\" method=\\\"post\\\" enctype=\\\"multipart/form-data\\\"><input type=\\\"hidden\\\" name=\\\"id\\\" value=\\\"\");\n" +
				"__body__.append(f(post));\n" +
				"__body__.append(\"\\\" /><input type=\\\"file\\\" /></form>\");", 
				render("form<Comment>(post, \"comments\")\n\tfile"));

		assertEquals(
				"Comment formModel$0 = new Comment();\n" +
				"String formModelName$0 = \"comment\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(post, \"comments\")).append(\"\\\" method=\\\"post\\\" enctype=\\\"multipart/form-data\\\"><input type=\\\"hidden\\\" name=\\\"id\\\" value=\\\"\");\n" +
				"__body__.append(f(post));\n" +
				"__body__.append(\"\\\" /><input type=\\\"file\\\" /></form>\");", 
				render("form(post, \"comments\")\n\tfile"));
	}

	@Test
	public void testErrors() throws Exception {
		assertEquals(
				"errorsBlock(__body__, member, null, null);", 
				render("errors(member)"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" />\");\n" +
				"errorsBlock(__body__, member, null, null);\n" +
				"__body__.append(\"</form>\");", 
				render("form(member, create)\n\terrors"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" />\");\n" +
				"errorsBlock(__body__, member, \"There are errors\", \"Please fix them\");\n" +
				"__body__.append(\"</form>\");", 
				render("form(member, create)\n\terrors(title: \"There are errors\", message: \"Please fix them\""));
	}
	
	@Test
	public void testCheck() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"checkbox\\\" />\");", 
				render("check"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"checkbox\\\" name=\\\"color\\\" value=\\\"red\\\" />\");", 
				render("check(name: \"color\", value: \"red\""));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[active]\\\" value=\\\"false\\\" /><input type=\\\"checkbox\\\" id=\\\"\").append(formModelName$0).append(\"[active]\\\"\");\n" +
				"if(member.hasErrors(\"active\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"active\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[active]\\\" value=\\\"true\\\"\");\nif(member.getActive()) {\n\t__body__.append(\" CHECKED\");\n}\n__body__.append(\" /></form>\");",
				render("form(member, create)\n\tcheck(\"active\")"));
	}
	
	@Test
	public void testHidden() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"hidden\\\" />\");", 
				render("hidden"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"hidden\\\" id=\\\"myField\\\" value=\\\"hello\\\" />\");", 
				render("hidden#myField(value: \"hello\")"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"hidden\\\" id=\\\"myField\\\" name=\\\"say\\\" value=\\\"hello\\\" />\");",
				render("hidden#myField(name: \"say\", value: \"hello\")"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"hidden\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\");\n" +
				"__body__.append(f(member.getFirstName()));\n" +
				"__body__.append(\"\\\" /></form>\");",
				render("form(member, create)\n\thidden(\"firstName\")"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"hidden\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"bob\\\" /></form>\");",
				render("form(member, create)\n\thidden(\"firstName\", value: \"bob\")"));
	}
	
	@Test
	public void testInput() throws Exception {
		assertEquals(
				"__body__.append(\"<input />\");", 
				render("input"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" id=\\\"myField\\\" value=\\\"hello\\\" />\");",
				render("input#myField(type: \"text\", value: \"hello\")"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" id=\\\"myField\\\" name=\\\"say\\\" value=\\\"hello\\\" />\");",
				render("input#myField(type: \"text\", name: \"say\", value: \"hello\")"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\");\n" +
				"__body__.append(f(member.getFirstName()));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, create)\n\tinput(\"firstName\")"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\");\n" +
				"__body__.append(f(member.getFirstName()));\n" +
				"__body__.append(\"\\\" /></form>\");", 
				render("form(member, create)\n\tinput(\"firstName\", type: \"text\")"));
		
		assertEquals("String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"bob\\\" /></form>\");", 
				render("form(member, create)\n\tinput(\"firstName\", type: \"text\", value: \"bob\")"));
	}
	
	@Test
	public void testLabel() throws Exception {
		assertEquals(
				"__body__.append(\"<label></label>\");", 
				render("label"));
		
		assertEquals(
				"__body__.append(\"<label id=\\\"myField\\\" for=\\\"firstName\\\"></label>\");", 
				render("label#myField(for: \"firstName\")"));
		
		assertEquals(
				"__body__.append(\"<label id=\\\"myField\\\" for=\\\"firstName\\\">First Name</label>\");", 
				render("label#myField(for: \"firstName\") First Name"));
		
		assertEquals(
				"if(true)\n\t__body__.append(\"<label></label>\");", 
				render("-if(true)\n\tlabel"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><label\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"labelWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" for=\\\"\").append(formModelName$0).append(\"[firstName]\\\">First Name\");\nif(member.isRequired(\"firstName\")) {\n\t__body__.append(\"<span class=\\\"required\\\">*</span>\");\n}\n__body__.append(\"</label></form>\");",
				render("form(member, create)\n\tlabel(\"firstName\")"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><label\");\n" +
				"if(member.hasErrors(firstName)) {\n" +
				"\t__body__.append(\" class=\\\"labelWithErrors\\\" error=\\\"\").append(member.getError(firstName)).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" for=\\\"\").append(formModelName$0).append(\"[\");\n" +
				"__body__.append(f(firstName));\n" +
				"__body__.append(\"]\\\">\");\n" +
				"__body__.append(h(titleize(firstName)));\n" +
				"if(member.isRequired(firstName)) {\n" +
				"\t__body__.append(\"<span class=\\\"required\\\">*</span>\");\n" +
				"}\n" +
				"__body__.append(\"</label></form>\");",
				render("form(member, create)\n\tlabel(firstName)"));

		// with "text" attribute
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><label\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"labelWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" for=\\\"\").append(formModelName$0).append(\"[firstName]\\\">Member Name\");\nif(member.isRequired(\"firstName\")) {\n\t__body__.append(\"<span class=\\\"required\\\">*</span>\");\n}\n" +
				"__body__.append(\"</label></form>\");",
				render("form(member, create)\n\tlabel(\"firstName\", text:\"Member Name\")"));

		// with inner text field
		assertEquals("String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><label\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"labelWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" for=\\\"\").append(formModelName$0).append(\"[firstName]\\\">Member Name\");\nif(member.isRequired(\"firstName\")) {\n\t__body__.append(\"<span class=\\\"required\\\">*</span>\");\n}\n" +
				"__body__.append(\"</label></form>\");",
		render("form(member, create)\n\tlabel(\"firstName\") Member Name"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><label\");\n" +
				"if(member.hasErrors(\"spouse\", \"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"labelWithErrors\\\" error=\\\"\").append(member.getError(\"spouse\", \"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" for=\\\"\").append(formModelName$0).append(\"[spouse][firstName]\\\">First Name\");\nif(member.isRequired(\"spouse\", \"firstName\")) {\n\t__body__.append(\"<span class=\\\"required\\\">*</span>\");\n}\n__body__.append(\"</label></form>\");", 
				render("form(member, create)\n\tlabel(\"spouse\", \"firstName\")"));
	}
	
	@Test
	public void testNumber() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" onkeypress=\\\"var k=window.event?event.keyCode:event.which;return (k==127||k<32||(k>47&&k<58));\\\" />\");",
				render("number"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" onkeypress=\\\"alert('hello');var k=window.event?event.keyCode:event.which;return (k==127||k<32||(k>47&&k<58));\\\" />\");",
				render("number(onkeypress: \"alert('hello')\")"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" onkeypress=\\\"alert('hello');var k=window.event?event.keyCode:event.which;return (k==127||k<32||(k>47&&k<58));\\\" />\");",
				render("number(onkeypress: \"alert('hello');\")"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" id=\\\"myField\\\" value=\\\"10\\\" onkeypress=\\\"var k=window.event?event.keyCode:event.which;return (k==127||k<32||(k>47&&k<58));\\\" />\");",
				render("number#myField(value:\"10\")"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[age]\\\"\");\n" +
				"if(member.hasErrors(\"age\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"age\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[age]\\\" value=\\\"\").append(f(member.getAge())).append(\"\\\" onkeypress=\\\"var k=window.event?event.keyCode:event.which;return (k==127||k<32||(k>47&&k<58));\\\" /></form>\");",
				render("form(member, create)\n\tnumber(\"age\")"));
	}
	
	@Test
	public void testPassword() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"password\\\" />\");", 
				render("password"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"password\\\" id=\\\"myPassword\\\" />\");", 
				render("password#myPassword"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"password\\\" id=\\\"\").append(formModelName$0).append(\"[passkey]\\\"\");\n" +
				"if(member.hasErrors(\"passkey\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"passkey\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[passkey]\\\" /></form>\");",
				render("form(member, create)\n\tpassword(\"passkey\")"));
	}
	
	@Test
	public void testRadio() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"radio\\\" />\");", 
				render("radio"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"radio\\\" id=\\\"myRadio\\\" />\");", 
				render("radio#myRadio"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"radio\\\" id=\\\"\").append(formModelName$0).append(\"[active]\\\"\");\n" +
				"if(member.hasErrors(\"active\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"active\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[active]\\\" value=\\\"\");\n" +
				"__body__.append(f(member.getActive()));\n" +
				"__body__.append(\"\\\" /></form>\");",
				render("form(member, create)\n\tradio(\"active\")"));
	}
	
	@Test
	public void testReset() throws Exception {
		assertEquals("__body__.append(\"<input type=\\\"reset\\\" value=\\\"Reset\\\" />\");", render("reset"));
	}
	
	@Test
	public void testSelect() throws Exception {
		assertEquals(
				"__body__.append(\"<select></select>\");", 
				render("select"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><select id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\"></select></form>\");",
				render("form(member, create)\n\tselect(\"firstName\")"));
	}
	
	@Test
	public void testSelectOptions() throws Exception {
		assertEquals(
				"__body__.append(\"<select></select>\");", 
				render("select<-options"));
		
		assertEquals(
				"__body__.append(\"<select>\");\n__body__.append(optionTags(members));\n__body__.append(\"</select>\");", 
				render("select<-options(members)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><select id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
						"if(member.hasErrors(\"firstName\")) {\n" +
						"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
						"}\n" +
						"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\">\");\n" +
						"__body__.append(optionTags(members, member.getFirstName(), member.isRequired(\"firstName\")));\n" +
						"__body__.append(\"</select></form>\");",
				render("form(member, create)\n\tselect(\"firstName\")<-options(members)"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><select id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\">\");\n" +
				"Object selection$43 = member.getFirstName();\nfor(Member member : members) {\n" +
				"\tboolean selected$43 = isEqual(((member.getId())), selection$43);\n" +
				"\t__body__.append(\"<option value=\\\"\").append(((member.getId()))).append(\"\\\" \").append(selected$43 ? \"selected >\" : \">\").append(((member.getNameLF()))).append(\"</option>\");\n" +
				"}\n" +
				"__body__.append(\"</select></form>\");",
				render("form(member, create)\n\tselect(\"firstName\")<-options<Member>(members, text:\"${member.getNameLF()}\", value:\"${member.getId()}\", sort:\"${member.getNameLF()}\")"));
	}
	
	@Test
	public void testSubmit() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"submit\\\" value=\\\"Submit\\\" />\");", 
				render("submit"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, member.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"post\\\">\");\n" +
				"if(!member.isNew()) {\n" +
				"\t__body__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
				"}\n" +
				"__body__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"submit\\\" value=\\\"\").append(member.isNew() ? \"Create \" : \"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");", 
				render("form(member)\n\tsubmit"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Create \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
				render("form(member, create)\n\tsubmit"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, update)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
				render("form(member, update)\n\tsubmit"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Create \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
				render("form(member, action:create)\n\tsubmit"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, update)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
				render("form(member, action:update)\n\tsubmit"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Create \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
				render("form(member, method: post)\n\tsubmit"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, update)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
				render("form(member, method: put)\n\tsubmit"));
	}
	
	@Test
	public void testText() throws Exception {
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" />\");", 
				render("text"));
		
		assertEquals(
				"__body__.append(\"<input type=\\\"text\\\" id=\\\"myText\\\" />\");", 
				render("text#myText"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\");\n" +
				"__body__.append(f(member.getFirstName()));\n" +
				"__body__.append(\"\\\" /></form>\");",
				render("form(member, create)\n\ttext(\"firstName\")"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value /></form>\");",
				render("form(member, create)\n\ttext(\"firstName\", value:)"));

		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\");\n" +
				"__body__.append(f(value));\n" +
				"__body__.append(\"\\\" /></form>\");",
				render("form(member, create)\n\ttext(\"firstName\", value: value)"));
	}
	
	@Test
	public void testTextArea() throws Exception {
		assertEquals(
				"__body__.append(\"<textarea></textarea>\");", 
				render("textArea"));
		
		assertEquals(
				"__body__.append(\"<textarea id=\\\"myText\\\"></textarea>\");", 
				render("textArea#myText"));
		
		assertEquals(
				"String formModelName$0 = \"member\";\n" +
				"__body__.append(\"<form action=\\\"\").append(pathTo(member, create)).append(\"\\\" method=\\\"post\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\");\n" +
				"__body__.append(f(member));\n" +
				"__body__.append(\"\\\" /><textarea id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
				"if(member.hasErrors(\"firstName\")) {\n" +
				"\t__body__.append(\" class=\\\"fieldWithErrors\\\" error=\\\"\").append(member.getError(\"firstName\")).append(\"\\\"\");\n" +
				"}\n" +
				"__body__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\">\").append(f(member.getFirstName())).append(\"</textarea></form>\");",
				render("form(member, create)\n\ttextArea(\"firstName\")"));
	}

	@Test
	public void testYield() throws Exception {
		assertEquals("yield();", render("yield"));
		
		assertEquals(
				"__body__.append(\"<div>\");\nyield();\n__body__.append(\"</div>\");", 
				render("div <- yield"));

		assertEquals("yield(\"name\");", render("yield(\"name\")"));
		
		assertEquals("yield(view);", render("yield(view)"));
	}
	
	@Test
	public void testYieldInContentFor() throws Exception {
		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
				 "yield(view, test$0);\n" +
				 "putContent(\"test\", test$0.toString());\n" +
				 "test$0 = null;",
			render("contentFor(\"test\")\n\tyield(view)"));

		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
				 "yield(test$0);\n" +
				 "putContent(\"test\", test$0.toString());\n" +
				 "test$0 = null;",
			render("contentFor(\"test\")\n\tyield"));
	}
	
	@Test
	public void testInnerText() throws Exception {
		// regular (can contain java parts)
		assertEquals("__body__.append(\"<div>start:</div>\");", render("div start:\n\t+end")); // space required
		assertEquals("__body__.append(\"<div>start:end</div>\");", render("div start:\n\t+ end"));
		assertEquals("__body__.append(\"<div>start : end</div>\");", render("div start :\n\t+  end"));
		assertEquals("__body__.append(\"<div>start:e\");\n__body__.append(h(n));\n__body__.append(\"d</div>\");", render("div start:\n\t+ e{n}d"));
		assertEquals("__body__.append(\"<div>start:<code>src</code></div>\");", render("div start:\n\t+ <code>src</code>"));

		// literal (no java parts)
		assertEquals("__body__.append(\"<div>start:</div>\");", render("div start:\n\t+=middle\n\t+=end")); // space required
		assertEquals("__body__.append(\"<div>start:middleend</div>\");", render("div start:\n\t+= middle\n\t+= end"));
		assertEquals("__body__.append(\"<div>start : middle end</div>\");", render("div start :\n\t+=  middle\n\t+=  end"));
		assertEquals("__body__.append(\"<div>start:e{n}d1e{n}d2</div>\");", render("div start:\n\t+= e{n}d1\n\t+= e{n}d2"));
		assertEquals("__body__.append(\"<div>start:</div>\");", render("div start:\n\t+= \n\t+= "));
		assertEquals("__body__.append(\"<div>start:<code>src</code></div>\");", render("div start:\n\t+= <code>src</code>"));
		
		// literal w/ word separators
		assertEquals("__body__.append(\"<div>start:</div>\");", render("div start:\n\t+wmiddle\n\t+wend")); // space required
		assertEquals("__body__.append(\"<div>start: middle end</div>\");", render("div start:\n\t+w middle\n\t+w end"));
		assertEquals("__body__.append(\"<div>start :  middle  end</div>\");", render("div start :\n\t+w  middle\n\t+w  end"));
		assertEquals("__body__.append(\"<div>start: e{n}d1 e{n}d2</div>\");", render("div start:\n\t+w e{n}d1\n\t+w e{n}d2"));
		assertEquals("__body__.append(\"<div>start:</div>\");", render("div start:\n\t+w \n\t+w "));
		assertEquals("__body__.append(\"<div>start: <code>src</code></div>\");", render("div start:\n\t+w <code>src</code>"));

		// prompt (HTML escaped literal w/ line endings)
		assertEquals("__body__.append(\"<div>start:</div>\");", render("div start:\n\t+>middle\n\t+>end")); // space required
		assertEquals("__body__.append(\"<div>start:middle\\nend\\n</div>\");", render("div start:\n\t+> middle\n\t+> end"));
		assertEquals("__body__.append(\"<div>start : middle\\n end\\n</div>\");", render("div start :\n\t+>  middle\n\t+>  end"));
		assertEquals("__body__.append(\"<div>start:e{n}d1\\ne{n}d2\\n</div>\");", render("div start:\n\t+> e{n}d1\n\t+> e{n}d2"));
		assertEquals("__body__.append(\"<div>start:\\n\\n</div>\");", render("div start:\n\t+> \n\t+> "));
		assertEquals("__body__.append(\"<div>start:&lt;code&gt;src&lt;/code&gt;\\n</div>\");", render("div start:\n\t+> <code>src</code>"));
	}
	
	@Test
	public void testCapture() throws Exception {
		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
					 "test$0.append(\"<div>hello</div>\");\n" +
					 "String test = test$0.toString();\n" +
					 "test$0 = null;",
				render("capture(test)\n\tdiv hello"));

		assertEquals("__body__.append(\"<div>1</div>\");\n" +
					 "StringBuilder test$6 = new StringBuilder();\n" +
					 "test$6.append(\"<div>2</div>\");\n" +
					 "String test = test$6.toString();\n" +
					 "test$6 = null;\n" +
					 "__body__.append(\"<div>3</div>\");",
				render("div 1\ncapture(test)\n\tdiv 2\ndiv 3"));

		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
				 "yield(new MyView(), test$0);\n" +
				 "String test = test$0.toString();\n" +
				 "test$0 = null;",
			render("capture(test) <- view<MyView>"));

	}
	
	@Test
	public void testContentFor() throws Exception {
		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
					 "test$0.append(\"<div>hello</div>\");\n" +
					 "putContent(\"test\", test$0.toString());\n" +
					 "test$0 = null;",
				render("contentFor(\"test\")\n\tdiv hello"));

		assertEquals("__body__.append(\"<div>1</div>\");\n" +
					 "StringBuilder test$6 = new StringBuilder();\n" +
					 "test$6.append(\"<div>2</div>\");\n" +
					 "putContent(\"test\", test$6.toString());\n" +
					 "test$6 = null;\n" +
					 "__body__.append(\"<div>3</div>\");",
				render("div 1\ncontentFor(\"test\")\n\tdiv 2\ndiv 3"));
	}
	
}
