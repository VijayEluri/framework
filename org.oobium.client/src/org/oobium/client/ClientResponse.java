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
package org.oobium.client;

import static org.oobium.http.constants.ContentType.HTML;
import static org.oobium.http.constants.ContentType.JS;
import static org.oobium.http.constants.ContentType.JSON;
import static org.oobium.http.constants.StatusCode.NOT_FOUND;
import static org.oobium.http.constants.StatusCode.OK;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.StatusCode;

public class ClientResponse {

	public static ClientResponse ok() {
		return ok("system", null);
	}
	
	public static ClientResponse ok(String server, ContentType type) {
		return create(OK, server, type);
	}
	
	public static ClientResponse create(StatusCode status) {
		return create(status, "system", HTML);
	}
	
	private static ClientResponse create(StatusCode status, String server, ContentType type) {
		ClientResponse response = new ClientResponse();
		response.status = status;
		response.headers = new LinkedHashMap<String, List<String>>();
		response.headers.put(null, Collections.singletonList(status.getStatusHeader()));
		response.headers.put(Header.SERVER.key(), Collections.singletonList(server));
		if(JS == type || JSON == type) {
			response.type = type;
			response.content = ("[\"[" + status.getCode() + "] " + status.getDescription() + "\"]").getBytes();
			return response;
		} else { // default to HTML
			response.type = type;
			response.content = (status.getCode() + "\n" + status.getDescription()).getBytes();
			return response;
		}
	}

	public static ClientResponse notFound() {
		return create(NOT_FOUND, "system", null);
	}
	
	
	private StatusCode status;
	private ContentType type;
	private Map<String, List<String>> headers;
	private byte[] content;

	private Exception exception;

	private ClientResponse() {
		// no args constructor
	}
	
	ClientResponse(Exception e) {
		exception = e;
	}

	ClientResponse(HttpURLConnection conn) {
		if(conn.getDoOutput()) {
			try {
				conn.getOutputStream().close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		try {
			status = StatusCode.get(conn.getResponseCode());
			type = ContentType.get(conn.getContentType());
			headers = new LinkedHashMap<String, List<String>>(conn.getHeaderFields());
			int contentLength = conn.getContentLength();
			if(contentLength > 0) {
				content = new byte[contentLength];
				int read, offset = 0;
				BufferedInputStream in;
				int responseCode = conn.getResponseCode();
				if(responseCode >= 200 && responseCode < 300) {
					in = new BufferedInputStream(conn.getInputStream());
				} else {
					in = new BufferedInputStream(conn.getErrorStream());
				}
				while(offset < contentLength && (read = in.read(content, offset, contentLength-offset)) != -1) {
					offset += read;
				}
				in.close();
			}
		} catch(Exception e) {
			exception = e;
		}
	}

	public boolean exceptionThrown() {
		return exception != null;
	}

	public byte[] getContent() {
		return content;
	}
	
	public String getBody() {
		return (content != null) ? new String(content) : "";
	}

	public Exception getException() {
		return exception;
	}

	public String getHeader(String key) {
		if(headers != null) {
			List<String> list = headers.get(key);
			if(list != null && !list.isEmpty()) {
				return list.get(0);
			}
		}
		return null;
	}

	public List<String> getHeaderNames() {
		return (headers != null) ? new ArrayList<String>(headers.keySet()) : new ArrayList<String>(0);
	}
	
	public List<String> getHeaders() {
		if(headers != null) {
			List<String> list = new ArrayList<String>();
			for(Entry<String, List<String>> entry : headers.entrySet()) {
				String key = entry.getKey();
				if(key == null) {
					for(String value : entry.getValue()) {
						list.add(value);
					}
				} else {
					for(String value : entry.getValue()) {
						list.add(entry.getKey() + ":" + value);
					}
				}
			}
			return list;
		} else {
			return new ArrayList<String>(0);
		}
	}

	public List<String> getHeaders(String key) {
		return (headers != null) ? headers.get(key) : null;
	}

	public StatusCode getStatus() {
		return status;
	}

	public ContentType getType() {
		return type;
	}
	
	public boolean hasBody() {
		return content != null && content.length > 0;
	}

	public boolean isSuccess() {
		return exception == null && getStatus().isSuccess();
	}

	@Override
	public String toString() {
		return super.toString() + " {" + getStatus() + "}";
	}

}
