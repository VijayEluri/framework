package org.oobium.server;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpRequestHandler;
import org.oobium.http.HttpResponse;
import org.oobium.logging.Logger;

public class ServerTests {

	@Test
	public void testServer() throws Exception {
		Logger.getLogger(Server.class).setConsoleLevel(Logger.NEVER);

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
	
}
