package org.oobium.build.esp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.oobium.app.views.StyleSheet;
import org.oobium.build.esp.compiler.ESourceFile;
import org.oobium.build.esp.compiler.EspCompiler;
import org.oobium.build.esp.dom.EspDom;
import org.oobium.build.esp.dom.EspResolver;
import org.oobium.build.esp.parser.EspBuilder;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.SimpleDynClass;

public class EssTests {

	protected static String pkg;
	private static int count;

	@Before
	public void setup() throws Exception {
		DynClasses.reset();
		pkg = "com.test" + count++;
	}
	
	private ESourceFile esf(String src) throws Exception {
		EspDom dom = EspBuilder.newEspBuilder("Test"+(count++)+".ess").parse(src);
		EspResolver resolver = new EspResolver();
		resolver.add(dom);
		
		EspCompiler ec = EspCompiler.newEspCompiler(pkg);
		ec.setResolver(resolver);
		ESourceFile esf = ec.compile(dom);
		
		String java = esf.getSource();
		System.out.println(java);

		return esf;
	}

	private String css(String src) throws Exception {
		ESourceFile esf = esf(src);
		
		String java = esf.getSource();
		System.out.println(java);
		
		Class<?> clazz = SimpleDynClass.getClass(esf.getCanonicalName(), java);
		StyleSheet ss = (StyleSheet) clazz.newInstance();
		
		String css = ss.getContent();
		System.out.println(css);
		return css;
	}
	
	@Test
	public void testX() throws Exception {
		assertEquals("#header{color:red}", css("#header { color: red; }"));
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
		assertEquals(css, css(ess));
	}
	
	@Test
	public void testStyleFormats() throws Exception {
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
		assertEquals(css(ess1), css(ess2));
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
		assertEquals(css(ess1), css(ess2));
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

		assertEquals(css, css(ess));
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

		assertEquals(css, css(ess));
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

		assertEquals(css, css(ess));
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

		assertEquals(css, css(ess));
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

		assertEquals(css, css(ess));
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

		assertEquals(css, css(ess));
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

		assertEquals(css, css(ess));
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

		assertEquals(css, css(ess));
	}

}
