/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.server;

import static org.oobium.http.constants.RequestType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.oobium.server.Data;

public class NioDataTests {

	private Data data(String str) {
		return new Data((str != null) ? str.getBytes() : null);
	}
	
	@Test
	public void testData() throws Exception {
		Data data;
		data = data("GET /home HTTP/1.1\r\nHost: www.oobium.com\r\n\r\n");
		assertEquals(Data.HAS_HEADERS, data.state);
		assertEquals(GET, data.type);
		assertEquals(Data.HTTP_1_1, data.protocol);

		data = data("GET /home HTTP/1.0\r\nHost: www.oobium.com\r\n\r\n");
		assertEquals(Data.HAS_HEADERS, data.state);
		assertEquals(GET, data.type);
		assertEquals(Data.HTTP_1_0, data.protocol);

		data = data("GET /home HTTP/1.1\nHost: www.oobium.com\n\n");
		assertEquals(Data.HAS_HEADERS, data.state);
		assertEquals(GET, data.type);
		assertEquals(Data.HTTP_1_1, data.protocol);
		assertEquals("/home", data.getPath());
		assertEquals("HTTP/1.1", data.getProtocol());
		assertEquals("www.oobium.com", data.getHost());

		data = data("GET /home HTTP/1.0\nHost: www.oobium.com\n\n");
		assertEquals(Data.HAS_HEADERS, data.state);
		assertEquals(GET, data.type);
		assertEquals(Data.HTTP_1_0, data.protocol);
		assertEquals("/home", data.getPath());
		assertEquals("HTTP/1.0", data.getProtocol());
		assertEquals(null, data.getHost());

	}
	
	@Test
	public void testFindType() throws Exception {
		Data data;
		
		data = data("GETasdf");
		
		assertEquals(Data.INVALID_TYPE, data.state);
		assertEquals(0, data.marks[0]);
		
		data = data("GET asdf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(3, data.marks[0]);
		
		data = data("get asdf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(3, data.marks[0]);
		
		data = data("Get asdf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(3, data.marks[0]);
		
		data = data("PUTasdf");
		assertEquals(Data.INVALID_TYPE, data.state);
		assertEquals(0, data.marks[0]);
		
		data = data("PUT asdf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(3, data.marks[0]);
		
		data = data("put adsf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(3, data.marks[0]);
		
		data = data("Put asdf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(3, data.marks[0]);
		
		data = data("POSTasdf");
		assertEquals(Data.INVALID_TYPE, data.state);
		assertEquals(0, data.marks[0]);
		
		data = data("POST asdf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(4, data.marks[0]);
		
		data = data("post asdf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(4, data.marks[0]);
		
		data = data("Post adsf");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(4, data.marks[0]);
		
		data = data("DELETEa");
		assertEquals(Data.INVALID_TYPE, data.state);
		assertEquals(0, data.marks[0]);

		data = data("DELETE ");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(6, data.marks[0]);
		
		data = data("delete ");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(6, data.marks[0]);
		
		data = data("Delete ");
		assertEquals(Data.HAS_TYPE, data.state);
		assertEquals(6, data.marks[0]);

		data = data("");
		assertEquals(Data.UNKNOWN, data.state);
		assertEquals(0, data.marks[0]);

		data = data("DELETEME ");
		assertEquals(Data.INVALID_TYPE, data.state);
		assertEquals(0, data.marks[0]);
	}
	
	@Test
	public void testReadLine() throws Exception {
		assertNull(data("").readLine());
		assertNull(data("test").readLine());
		assertEquals("test", data("test\r\n").readLine());
		assertEquals("test", data("test\n").readLine());
		assertEquals("test", data("test\r\nasdf").readLine());
		assertEquals("test", data("test\nasdf").readLine());
		
		Data data = data("test\nasdf\n");
		assertEquals("test", data.readLine());
		assertEquals("asdf", data.readLine());
		assertNull(data.readLine());

		data = data("test\n\n");
		assertEquals("test", data.readLine());
		assertEquals("", data.readLine());
		assertNull(data.readLine());
	}
	
}
