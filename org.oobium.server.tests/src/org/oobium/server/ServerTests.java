package org.oobium.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpRequestHandler;
import org.oobium.http.HttpResponse;
import org.oobium.logging.Logger;
import org.oobium.logging.LogProvider;

public class ServerTests {

	@Ignore
	@Test
	public void testServer() throws Exception {
		LogProvider.getLogger(Server.class).setConsoleLevel(Logger.NEVER);

		HttpResponse response = mock(HttpResponse.class);
		when(response.getBuffer()).thenAnswer(new Answer<ByteBuffer>() {
			@Override
			public ByteBuffer answer(InvocationOnMock invocation) throws Throwable {
				return ByteBuffer.wrap("HTTP/1.0 200 - OK\r\n\n".getBytes());
			}
		});
		
		HttpRequestHandler handler = mock(HttpRequestHandler.class);
		when(handler.getName()).thenReturn("test.handler");
		when(handler.getPort()).thenReturn(5000);
		when(handler.handleRequest(any(HttpRequest.class))).thenReturn(response);
		
		Server server = new Server();
		server.addHandler(handler);
		
		long start = System.currentTimeMillis();
		int iterations = 10000;
		for(int i = 0; i < iterations; i++) {
			System.out.println(i);
			URL url = new URL("http", "localhost", 5000, "/");
	        URLConnection conn = url.openConnection();
	        String status = conn.getHeaderField(0);
	        conn.getInputStream().close();
	        assertEquals("HTTP/1.0 200 - OK", status);
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("elapsed time: " + (duration) + " millis");
		System.out.println("average time: " + ((double) duration / iterations) + " millis");
	}
	
	@Test
	public void testBase64() throws Exception {
		LogProvider.getLogger(Server.class).setConsoleLevel(Logger.NEVER);

		HttpResponse response = mock(HttpResponse.class);
		when(response.getBuffer()).thenAnswer(new Answer<ByteBuffer>() {
			@Override
			public ByteBuffer answer(InvocationOnMock invocation) throws Throwable {
				return ByteBuffer.wrap("HTTP/1.0 200 - OK\r\n\n".getBytes());
			}
		});
		
		HttpRequestHandler handler = mock(HttpRequestHandler.class);
		when(handler.getName()).thenReturn("test.handler");
		when(handler.getPort()).thenReturn(5000);
		when(handler.handleRequest(any(HttpRequest.class))).thenReturn(response);
		
		Server server = new Server();
		server.addHandler(handler);

		Client client = new Client("localhost", 5000);

		
		
		Map<String, Object> params = new HashMap<String, Object>();
		
//		params
//		
//		client.get("/", );
		
//		long start = System.currentTimeMillis();
//		int iterations = 10000;
//		for(int i = 0; i < iterations; i++) {
//			System.out.println(i);
//			URL url = new URL("http", "localhost", 5000, "/");
//	        URLConnection conn = url.openConnection();
//	        String status = conn.getHeaderField(0);
//	        conn.getInputStream().close();
//	        assertEquals("HTTP/1.0 200 - OK", status);
//		}
//		long duration = System.currentTimeMillis() - start;
//		System.out.println("elapsed time: " + (duration) + " millis");
//		System.out.println("average time: " + ((double) duration / iterations) + " millis");
	}
	
}
