package org.oobium.persist.db.internal;

import static org.junit.Assert.*;
import static org.oobium.utils.json.JsonUtils.*;
import static org.oobium.utils.StringUtils.*;


import org.junit.Test;

public class ConversionTests {

	@Test
	public void testSingleNonString() throws Exception {
		Conversion conversion = new Conversion(toMap("id:1234"));
		assertEquals("WHERE id=?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleString() throws Exception {
		Conversion conversion = new Conversion(toMap("name:'bob'"));
		assertEquals("WHERE name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleValue() throws Exception {
		Conversion conversion = new Conversion(toMap("name:?"), "bob");
		assertEquals("WHERE name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwo() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,active:true"));
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithValues() throws Exception {
		Conversion conversion = new Conversion(toMap("name:?,active:?"), "bob", true);
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithFirstValue() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,active:?"), true);
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithSecondValue() throws Exception {
		Conversion conversion = new Conversion(toMap("name:?,active:true"), "bob");
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleNotEquals() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{not:1234}"));
		assertEquals("WHERE id!=?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoNotEquals() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{not:1234},name:{not:bob}"));
		assertEquals("WHERE id!=? AND name!=?", conversion.getSql());
		assertEquals("[1234, bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testLessThan() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{lt:1234}"));
		assertEquals("WHERE id<?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testRange() throws Exception {
		Conversion conversion = new Conversion(toMap("id:{gt:1,lt:10}"));
		assertEquals("WHERE (id>? AND id<?)", conversion.getSql());
		assertEquals("[1, 10]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoAnded() throws Exception {
		Conversion conversion = new Conversion(toMap("and:{name:bob,active:true}", true));
		assertEquals("WHERE name=? AND active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoOred() throws Exception {
		Conversion conversion = new Conversion(toMap("or:{name:bob,active:true}", true));
		assertEquals("WHERE name=? OR active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testOneAndedWithTwoOrs() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,or:{name:joe,active:true}", true));
		assertEquals("WHERE name=? AND (name=? OR active=?)", conversion.getSql());
		assertEquals("[bob, joe, true]", asString(conversion.getValues()));
	}

	@Test
	public void testSingleWithLimit() throws Exception {
		Conversion conversion = new Conversion(toMap("name:bob,$limit:'1,2'"));
		assertEquals("WHERE name=? LIMIT 1,2", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}

}
