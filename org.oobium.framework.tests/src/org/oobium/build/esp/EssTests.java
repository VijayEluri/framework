package org.oobium.build.esp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.oobium.app.views.StyleSheet;
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
	
	private String css(String src) throws Exception {
		EspDom dom = new EspDom("Test"+(count++)+".ess", src);
		EspCompiler ec = new EspCompiler(pkg, dom);
		ESourceFile sf = ec.compile();
		sf.getSource();

		Class<?> clazz = SimpleDynClass.getClass(sf.getCanonicalName(), sf.getSource());
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
				"#header .navigation{font-size:12px}" +
				"#header.logo{width:300px}" +
				"#header.logo[type=password]:hover{text-decoration:none}";

		String ess =
				("#header\n" +
				 "  color: black\n" +
				 "  .navigation\n" +
				 "    font-size: 12px\n" +
				 "  &.logo\n" +
				 "    width: 300px\n" +
				 "    &[type=password]\n" +
				 "      &:hover\n" +
				 "        text-decoration: none").replace("  ", "\t");

		assertEquals(css, css(ess));
	}
	
}
