package org.oobium.app.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.oobium.utils.literal.*;
import static org.oobium.app.controllers.HttpController.mapParams;
import static org.oobium.app.controllers.HttpController.splitParam;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.oobium.app.AppService;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.request.Request;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;

public class ControllerTests {

	@ModelDescription(hasMany={@Relation(name="phones",type=Phone.class)})
	public static class Member extends Model {
		public Member() { setId(0); set("type", "admin"); }
	}
	public static class MemberController extends HttpController { }

	@ModelDescription(hasOne={@Relation(name="member",type=Member.class)})
	public static class Phone extends Model { }
	public static class PhoneController extends HttpController { }

	
	@Test
	public void testGetQuery() throws Exception {
		HttpRequestHandler handler = mock(AppService.class);
		Request request = mock(Request.class);
		when(request.getHandler()).thenReturn(handler);
		
		HttpController c = new HttpController();
		c.initialize(null, request, Map(e("id", (Object) 15), e("query", Map("number", "303-555-1212"))));
		
		Map<String, Object> query = c.getQuery();
		Object number = query.remove("number");
		assertNotNull(number);
		assertEquals("303-555-1212", number);
		assertTrue(query.isEmpty());
	}
	
	@Test
	public void testGetQueryForHasManyRoute() throws Exception {
		HttpRequestHandler handler = mock(AppService.class);
		Request request = mock(Request.class);
		when(request.getHandler()).thenReturn(handler);
		
		HttpController c = new HttpController();
		c.initialize(null, request, Map("member[id]", (Object) 15));
		c.setParentClass(Member.class);
		c.setHasManyField("phones");
		
		Map<String, Object> query = c.getQuery();
		Object from = query.get("$from");
		assertNotNull(from);
		assertTrue(from instanceof Map<?,?>);
		Object type = ((Map<?,?>) from).get("$type");
		Object id = ((Map<?,?>) from).get("$id");
		Object field = ((Map<?,?>) from).get("$field");
		assertNotNull(type);
		assertNotNull(id);
		assertNotNull(field);
		assertEquals(Member.class, type);
		assertEquals("15", id);
		assertEquals("phones", field);
	}
	
	@Test
	public void testGetQueryForHasManyRoute_WithExistingQuery() throws Exception {
		HttpRequestHandler handler = mock(AppService.class);
		Request request = mock(Request.class);
		when(request.getHandler()).thenReturn(handler);
		
		HttpController c = new HttpController();
		c.initialize(null, request, Map(e("member[id]", (Object) 15), e("query", Map("number", "303-555-1212"))));
		c.setParentClass(Member.class);
		c.setHasManyField("phones");
		
		Map<String, Object> query = c.getQuery();
		Object from = query.get("$from");
		assertNotNull(from);
		assertTrue(from instanceof Map<?,?>);
		Object type = ((Map<?,?>) from).get("$type");
		Object id = ((Map<?,?>) from).get("$id");
		Object field = ((Map<?,?>) from).get("$field");
		assertNotNull(type);
		assertNotNull(id);
		assertNotNull(field);
		assertEquals(Member.class, type);
		assertEquals("15", id);
		assertEquals("phones", field);
		
		Object number = query.get("number");
		assertNotNull(number);
		assertEquals("303-555-1212", number);
	}
	
	@Test
	public void testGetQueryForHasManyRoute_WithConflictingQuery() throws Exception {
		HttpRequestHandler handler = mock(AppService.class);
		Request request = mock(Request.class);
		when(request.getHandler()).thenReturn(handler);
		
		HttpController c = new HttpController();
		c.initialize(null, request, Map(e("member[id]", (Object) 15), e("query", Map("$from", "something"))));
		c.setParentClass(Member.class);
		c.setHasManyField("phones");
		
		Map<String, Object> query = c.getQuery();
		Object from = query.remove("$from");
		assertNotNull(from);
		assertTrue(from instanceof Map<?,?>);
		assertTrue(query.isEmpty());
		Object type = ((Map<?,?>) from).get("$type");
		Object id = ((Map<?,?>) from).get("$id");
		Object field = ((Map<?,?>) from).get("$field");
		assertNotNull(type);
		assertNotNull(id);
		assertNotNull(field);
		assertEquals(Member.class, type);
		assertEquals("15", id);
		assertEquals("phones", field);
	}
	
	@Test
	public void testSplitParam() throws Exception {
		assertArrayEquals(new String[] {"client"}, splitParam("client"));
		assertArrayEquals(new String[] {"client", "name"}, splitParam("client[name]"));
		assertArrayEquals(new String[] {"client", "address", "city"}, splitParam("client[address][city]"));
	}
	
	@Test
	public void testMapParamsNull() throws Exception {
		assertNotNull(mapParams(null));
		assertTrue(mapParams(null).isEmpty());
	}
	
	@Test
	public void testMapParams0() throws Exception {
		assertEquals(new HashMap<String, Object>(), mapParams(new HashMap<String, Object>()));
	}
	
	@Test
	public void testMapParams1() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("client", "joe");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("client", "joe");
		
		assertEquals(expected, mapParams(params));
	}
	
	@Test
	public void testMapParams2() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("client[name]", "joe");
		params.put("client[phone]", "12345");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		Map<String, Object> client = new HashMap<String, Object>();
		client.put("name", "joe");
		client.put("phone", "12345");
		expected.put("client", client);
		
		assertEquals("{client={phone=12345, name=joe}, client[phone]=12345, client[name]=joe}", mapParams(params).toString());
	}
	
	@Test
	public void testMapParams3() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("client[name]", "joe");
		params.put("client[phone]", "12345");
		params.put("client[address][city]", "Carrot City");
		params.put("client[address][zipcode]", "12345");
		
		Map<String, Object> expected = new HashMap<String, Object>();
		Map<String, Object> client = new HashMap<String, Object>();
		client.put("name", "joe");
		client.put("phone", "12345");
		Map<String, Object> address = new HashMap<String, Object>();
		address.put("city", "Carrot City");
		address.put("zipcode", "12345");
		client.put("address", address);
		expected.put("client", client);
		
		assertEquals("{client[address][city]=Carrot City, " +
					  "client[phone]=12345, " +
					  "client={phone=12345, " +
					  		  "address={zipcode=12345, city=Carrot City}, " +
					  		  "name=joe}, " +
					  "client[name]=joe, " +
					  "client[address][zipcode]=12345}", mapParams(params).toString());
	}

}
