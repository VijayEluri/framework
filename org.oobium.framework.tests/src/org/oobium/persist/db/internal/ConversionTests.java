package org.oobium.persist.db.internal;

import static org.junit.Assert.*;
import static org.oobium.utils.json.JsonUtils.*;
import static org.oobium.utils.SqlUtils.*;
import static org.oobium.utils.StringUtils.*;


import org.junit.Test;
import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;

public class ConversionTests {

	@Test
	public void testSingleNonString() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("id:1234"));
		conversion.run();
		assertEquals("where id=?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleString() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:'bob'"));
		conversion.run();
		assertEquals("where name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleValue() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:?"), "bob");
		conversion.run();
		assertEquals("where name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleValue_ReservedWord() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("user:?"), "bob");
		conversion.run();
		assertEquals("where " + safeSqlWord(DERBY, "user") + "=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwo() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:bob,active:true"));
		conversion.run();
		assertEquals("where name=? and active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithValues() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:?,active:?"), "bob", true);
		conversion.run();
		assertEquals("where name=? and active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithFirstValue() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:bob,active:?"), true);
		conversion.run();
		assertEquals("where name=? and active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoWithSecondValue() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:?,active:true"), "bob");
		conversion.run();
		assertEquals("where name=? and active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testSingleNotEquals() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("id:{not:1234}"));
		conversion.run();
		assertEquals("where id!=?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoNotEquals() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("id:{not:1234},name:{not:bob}"));
		conversion.run();
		assertEquals("where id!=? and name!=?", conversion.getSql());
		assertEquals("[1234, bob]", asString(conversion.getValues()));
	}
	
	@Test
	public void testIsNull() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("id:{$is:null}"));
		conversion.run();
		assertEquals("where id is null", conversion.getSql());
		assertEquals(0, conversion.getValues().length);
	}
	
	@Test
	public void testIsNotNull() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("id:{$not:null}"));
		conversion.run();
		assertEquals("where id is not null", conversion.getSql());
		assertEquals(0, conversion.getValues().length);
	}
	
	@Test
	public void testLessThan() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("id:{lt:1234}"));
		conversion.run();
		assertEquals("where id<?", conversion.getSql());
		assertEquals("[1234]", asString(conversion.getValues()));
	}
	
	@Test
	public void testRange() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("id:{gt:1,lt:10}"));
		conversion.run();
		assertEquals("where (id>? and id<?)", conversion.getSql());
		assertEquals("[1, 10]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoAnded() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("and:{name:bob,active:true}", true));
		conversion.run();
		assertEquals("where name=? and active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testTwoOred() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("or:{name:bob,active:true}", true));
		conversion.run();
		assertEquals("where name=? or active=?", conversion.getSql());
		assertEquals("[bob, true]", asString(conversion.getValues()));
	}
	
	@Test
	public void testOneAndedWithTwoOrs() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:bob,or:{name:joe,active:true}", true));
		conversion.run();
		assertEquals("where name=? and (name=? or active=?)", conversion.getSql());
		assertEquals("[bob, joe, true]", asString(conversion.getValues()));
	}

	@Test
	public void testSingleWithLimit() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:bob,$limit:'1,2'"));
		conversion.run();
		assertEquals("where name=? limit 1,2", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}

	@Test
	public void testLimitOnly() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:1"));
		conversion.run();
		assertEquals("limit 1", conversion.getSql());
	}

	@Test
	public void testLimitAndOffset() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:'1,2'"));
		conversion.run();
		assertEquals("limit 1,2", conversion.getSql());
	}
	
	@Test
	public void testLimitOnly_AsParameter() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:?", 1));
		conversion.run();
		assertEquals("limit 1", conversion.getSql());
	}

	@Test
	public void testLimitAndOffset_AsParameter() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:?", "1,2"));
		conversion.run();
		assertEquals("limit 1,2", conversion.getSql());
	}
	
	@Test(expected=Exception.class)
	public void testIllegalLimit() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:'bob'"));
		conversion.run();
	}
	
	@Test(expected=Exception.class)
	public void testIllegalOffset() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:'bob, 2'"));
		conversion.run();
	}
	
	@Test(expected=Exception.class)
	public void testIllegalLimit_AsParameter() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:?", "'bob'"));
		conversion.run();
	}
	
	@Test(expected=Exception.class)
	public void testIllegalOffset_AsParameter() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("$limit:?", "'bob, 2'"));
		conversion.run();
	}
	
	@ModelDescription(attrs={@Attribute(name="name",type=String.class)})
	public static class TestClass extends Model { }
	
	@Test
	public void testSingleWithModelClass() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("name:bob"));
		conversion.setModelType(TestClass.class);
		conversion.run();
		assertEquals("where name=?", conversion.getSql());
		assertEquals("[bob]", asString(conversion.getValues()));
	}
	
	@Test(expected=Exception.class)
	public void testInvalidSingleWithModelClass() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("active:true"));
		conversion.setModelType(TestClass.class);
		conversion.run();
	}

	@Test
	public void testInvalidJsonMap() throws Exception {
		Conversion conversion = new Conversion(DERBY, toMap("blah limit 1"));
		conversion.setModelType(TestClass.class);
		try {
			conversion.run();
		} catch(Exception e) {
			fail("currently not designed to throw an except... should it be?");
		}
	}

}
