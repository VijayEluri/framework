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

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class ClientResponse {

	public static ClientResponse ok() {
		return ok("system");
	}
	
	public static ClientResponse ok(String server) {
		return create(OK, server, false);
	}
	
	public static ClientResponse create(HttpResponseStatus status) {
		return create(status, "system", false);
	}
	
	public static ClientResponse create(HttpResponseStatus status, boolean isJS) {
		return create(status, "system", isJS);
	}
	
	private static ClientResponse create(HttpResponseStatus status, String server, boolean isJS) {
		ClientResponse response = new ClientResponse();
		response.status = status;
		response.headers = new LinkedHashMap<String, List<String>>();
		response.headers.put(null, Collections.singletonList("HTTP/1.1 " + status.getCode() + " - " + status.getReasonPhrase()));
		response.headers.put("Server", Collections.singletonList(server));
		if(isJS) {
			response.content = ("[\"[" + status.getCode() + "] " + status.getReasonPhrase() + "\"]").getBytes();
			return response;
		} else { // default to HTML
			response.content = (status.getCode() + "\n" + status.getReasonPhrase()).getBytes();
			return response;
		}
	}

	public static ClientResponse notFound() {
		return create(NOT_FOUND);
	}
	
	
	private HttpResponseStatus status;
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
			status = HttpResponseStatus.valueOf(conn.getResponseCode());
			headers = new LinkedHashMap<String, List<String>>();
			for(Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
				String key = entry.getKey();
				headers.put((key != null) ? key.toLowerCase() : null, entry.getValue());
			}
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
			List<String> list = headers.get(key.toLowerCase());
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

	public HttpResponseStatus getStatus() {
		return status;
	}

	public boolean hasBody() {
		return content != null && content.length > 0;
	}

	/**
	 * 409 - Conflict is return by an Oobium server when using
	 * renderErrors(models). The body is filled with the errors
	 * causing the conflict.
	 * @return true if this status code is equal to 409; false otherwise
	 */
	public boolean isConflict() {
		HttpResponseStatus status = getStatus();
		if(status != null) {
			return status.getCode() == 409;
		}
		return false;
	}

	public boolean isNotFound() {
		HttpResponseStatus status = getStatus();
		if(status != null) {
			return status.getCode() == 404;
		}
		return false;
	}

	public boolean isSuccess() {
		HttpResponseStatus status = getStatus();
		if(status != null) {
			int code = status.getCode();
			return exception == null && code >= 200 && code < 300;
		}
		return false;
	}

	@Override
	public String toString() {
		if(exception != null) return exception.getLocalizedMessage();
		return super.toString() + " {" + getStatus() + "}";
	}

}
