package org.oobium.persist.db.internal;

import static org.junit.Assert.*;
import static org.oobium.utils.json.JsonUtils.*;
import static org.oobium.utils.StringUtils.*;


import org.junit.Test;
import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.PersistException;

public class ConversionTests {

	@Test
	public void testSingleNonString() throws Exception {
		Conversion conversion = new Conversion(toMap("id:1234"));
		conversion.run();
		assertEquals("WHERE id=?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleString() throws Exception {
		Conversion conversion = new Conversion(toMap("name:'bob'"));
		conversion.run();
		assertEquals("WHERE name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleValue() throws Exception {
		Conversion conversion = new Conversion(toMap("name:?"), "bob");
		conversion.run();
		assertEquals("WHERE name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwo() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,active:true"));
		conversion.run();
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithValues() throws Exception {
		Conversion conversion = new Conversion(toMap("name:?,active:?"), "bob", true);
		conversion.run();
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithFirstValue() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,active:?"), true);
		conversion.run();
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithSecondValue() throws Exception {
		Conversion conversion = new Conversion(toMap("name:?,active:true"), "bob");
		conversion.run();
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleNotEquals() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{not:1234}"));
		conversion.run();
		assertEquals("WHERE id!=?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoNotEquals() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{not:1234},name:{not:bob}"));
		conversion.run();
		assertEquals("WHERE id!=? AND name!=?", conversion.getSql());
		assertEquals("[1234, bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testLessThan() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{lt:1234}"));
		conversion.run();
		assertEquals("WHERE id<?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testRange() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{gt:1,lt:10}"));
		conversion.run();
		assertEquals("WHERE (id>? AND id<?)", conversion.getSql());
		assertEquals("[1, 10]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoAnded() throws Exception {
		Conversion conversion = new Conversion(toMap("and:{name:bob,active:true}", true));
		conversion.run();
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoOred() throws Exception {
		Conversion conversion = new Conversion(toMap("or:{name:bob,active:true}", true));
		conversion.run();
		assertEquals("WHERE name=? OR active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testOneAndedWithTwoOrs() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,or:{name:joe,active:true}", true));
		conversion.run();
		assertEquals("WHERE name=? AND (name=? OR active=?)", conversion.getSql());
		assertEquals("[bob, joe, true]", asString(conversion.getValues()));
	}

	@Test
	public void testSingleWithLimit() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,$limit:'1,2'"));
		conversion.run();
		assertEquals("WHERE name=? LIMIT 1,2", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}

	
	@ModelDescription(attrs={@Attribute(name="name",type=String.class)})
	public static class TestClass extends Model { }
	
	@Test
	public void testSingleWithModelClass() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob"));
		conversion.setModelType(TestClass.class);
		conversion.run();
		assertEquals("WHERE name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test(expected=PersistException.class)
	public void testInvalidSingleWithModelClass() throws Exception {
		Conversion conversion = new Conversion(toMap("active:true"));
		conversion.setModelType(TestClass.class);
		conversion.run();
	}
	

}
