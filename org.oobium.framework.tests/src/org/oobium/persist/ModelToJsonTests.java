package org.oobium.persist;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.utils.json.JsonUtils;

public class ModelToJsonTests {

	@Before
	public void setup() {
		DynClasses.reset();
	}
	
	@Test
	public void testEmpty() throws Exception {
		Model m = DynClasses.getModel("AModel").newInstance();
		assertEquals("{}", m.toJson());
	}

	@Test
	public void testSimpleField() throws Exception {
		Model m = DynClasses.getModel("AModel").newInstance();
		m.set("name", "bob");
		assertEquals("{\"name\":\"bob\"}", m.toJson());
	}
	
	@Test
	public void testId() throws Exception {
		Model m = DynClasses.getModel("AModel").newInstance();
		m.setId(10);
		assertEquals("{\"id\":10}", m.toJson());
	}

	@Test
	public void testModelField_New() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		a.set("b", b);
		b.set("name", "bob");
		assertEquals("{\"b\":null}", a.toJson());
	}
	
	@Test
	public void testModelField_Saved() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		a.set("b", b);
		b.setId(15);
		assertEquals("{\"b\":15}", a.toJson());
	}
	
	@Test
	public void testArray() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		Model c = DynClasses.getModel("CModel").newInstance();
		c.setId(10);
		a.set("array", new Object[] { 1, "hello", b, c });
		assertEquals("{}", a.toJson());
		assertEquals(a.toJson(), JsonUtils.toJson(a));
	}
	
	@Test
	public void testArray_Included() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		Model c = DynClasses.getModel("CModel").newInstance();
		c.setId(10);
		a.set("array", new Object[] { 1, "hello", b, c });
		assertEquals("{\"array\":[1,\"hello\",{},{\"id\":10}]}", a.toJson("include:array"));
		assertEquals(a.toJson(), JsonUtils.toJson(a));
	}
	
	@Test
	public void testIterable() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		Model c = DynClasses.getModel("CModel").newInstance();
		c.setId(10);
		List<Object> iterable = new ArrayList<Object>();
		iterable.add(1);
		iterable.add("hello");
		iterable.add(b);
		iterable.add(c);
		a.set("iterable", iterable);
		assertEquals("{}", a.toJson());
		assertEquals(a.toJson(), JsonUtils.toJson(a));
	}
	
	@Test
	public void testIterable_Included() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		Model c = DynClasses.getModel("CModel").newInstance();
		c.setId(10);
		List<Object> iterable = new ArrayList<Object>();
		iterable.add(1);
		iterable.add("hello");
		iterable.add(b);
		iterable.add(c);
		a.set("iterable", iterable);
		assertEquals("{\"iterable\":[1,\"hello\",{},{\"id\":10}]}", a.toJson("include:iterable"));
		assertEquals(a.toJson(), JsonUtils.toJson(a));
	}
	
	@Test
	public void testMap() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		Model c = DynClasses.getModel("CModel").newInstance();
		c.setId(10);
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("a", 1);
		map.put("b", "hello");
		map.put("c", b);
		map.put("d", c);
		a.set("map", map);
		assertEquals("{}", a.toJson());
		assertEquals(a.toJson(), JsonUtils.toJson(a));
	}
	
	@Test
	public void testMap_Included() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		Model c = DynClasses.getModel("CModel").newInstance();
		c.setId(10);
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("a", 1);
		map.put("b", "hello");
		map.put("c", b);
		map.put("d", c);
		a.set("map", map);
		assertEquals("{\"map\":{\"a\":1,\"b\":\"hello\",\"c\":null,\"d\":10}}", a.toJson("include:map"));
		assertEquals(a.toJson(), JsonUtils.toJson(a));
	}
	
	@Test
	public void testExcludeAttr() throws Exception {
		Model m = DynClasses.getModel("AModel").addAttr("name", "String.class").addAttr("password", "String.class", "json=false").newInstance();
		m.set("name", "bob");
		m.set("password", "secret");
		assertEquals("{\"name\":\"bob\"}", m.toJson());
	}

	@Test
	public void testIncludeModelField_New() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		a.set("b", b);
		b.set("name", "bob");
		assertEquals("{\"b\":{\"name\":\"bob\"}}", a.toJson("include:b"));
	}
	
	@Test
	public void testIncludeModelField_Saved() throws Exception {
		Model a = DynClasses.getModel("AModel").newInstance();
		Model b = DynClasses.getModel("BModel").newInstance();
		a.set("b", b);
		b.setId(15);
		b.set("name", "bob");
		assertEquals("{\"b\":{\"id\":15,\"name\":\"bob\"}}", a.toJson("include:b"));
	}

	@Test
	public void testHasMany_New() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addHasMany("bmodels", "BModel.class");
		DynModel bm = DynClasses.getModel("BModel");

		Model a = am.newInstance();
		Model b1 = bm.newInstance();
		Model b2 = bm.newInstance();
		
		a.set("bmodels", new Object[] { b1, b2 });
		b1.set("name", "bob");
		b2.set("name", "joe");

		assertEquals("{\"bmodels\":[null,null]}", a.toJson("include:b"));
	}

	@Test
	public void testIncludeHasMany_New() throws Exception {
		DynModel am = DynClasses.getModel("AModel").addHasMany("bmodels", "BModel.class", "include=true");
		DynModel bm = DynClasses.getModel("BModel");

		Model a = am.newInstance();
		Model b1 = bm.newInstance();
		Model b2 = bm.newInstance();
		
		a.set("bmodels", new Object[] { b1, b2 });
		b1.set("name", "bob");
		b2.set("name", "joe");

		assertEquals("{\"bmodels\":[{\"name\":\"bob\"},{\"name\":\"joe\"}]}", a.toJson("include:b"));
	}

}
