package org.oobium.app.controllers;

import static org.oobium.app.controllers.HttpController.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ControllerTests {

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
