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

import static org.oobium.http.constants.ContentType.MULTIPART;
import static org.oobium.http.constants.ContentType.parse;
import static org.oobium.http.constants.Header.ACCEPT;
import static org.oobium.http.constants.Header.CONTENT_DISPOSITION;
import static org.oobium.http.constants.Header.CONTENT_LENGTH;
import static org.oobium.http.constants.Header.CONTENT_TYPE;
import static org.oobium.http.constants.Header.COOKIE;
import static org.oobium.http.constants.Header.HOST;
import static org.oobium.http.constants.Header.METHOD;
import static org.oobium.http.constants.RequestType.DELETE;
import static org.oobium.http.constants.RequestType.GET;
import static org.oobium.http.constants.RequestType.POST;
import static org.oobium.http.constants.RequestType.PUT;
import static org.oobium.http.impl.Attrs.attrDecode;
import static org.oobium.http.impl.Attrs.attrsDecode;
import static org.oobium.utils.json.JsonUtils.toMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.oobium.http.HttpCookie;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpRequestHandler;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.http.impl.Cookie;
import org.oobium.http.impl.Headers;
import org.oobium.logging.Logger;

public class Request implements HttpRequest {

	public static int findAll(byte[] ca, int from, int to, byte...cs) {
		for(int i = from; i >= 0 && i <= to && i < ca.length; i++) {
			if(ca[i] == cs[0]) {
				boolean found = true;
				for(int j = 1; j < cs.length; j++) {
					if((i+j) == ca.length || ca[i+j] != cs[j]) {
						found = false;
						break;
					}
				}
				if(found) {
					return i;
				}
			}
		}
		return -1;
	}

	private static final byte[] CRLN = "\r\n".getBytes();
	
	private final Logger logger;

	private RequestType type;
	private String path;
	private String fullPath;
	private HttpRequestHandler handler;
	private Headers headers;
	private Map<String, Object> parameters;
	private Map<String, HttpCookie> cookies;
	private String host;
	private int port;
	private String ipAddress;
	
	public Request(RequestType type, String path, String fullPath, Headers headers, Map<String, Object> parameters) {
		logger = Logger.getLogger(Server.class);
		
		this.type = type;
		this.path = path;
		this.fullPath = fullPath;
		this.headers = headers;
		this.parameters = parameters;

		parseCookies(headers);
	}

	public void addHeader(Header name, String value) {
		headers.add(name, value);
	}

	public ContentType[] getContentTypes() {
		String str = headers.get(ACCEPT);
		if(str != null) {
			return ContentType.getAll(str.split(",")[0].trim());
		}
		return new ContentType[0];
	}
	
	public HttpCookie getCookie(String name) {
		if(cookies != null) {
			return cookies.get(name);
		}
		return null;
	}

	public Collection<HttpCookie> getCookies() {
		if(cookies != null) {
			return cookies.values();
		}
		return Collections.emptyList();
	}
	
	public String getCookieValue(String name) {
		HttpCookie cookie = getCookie(name);
		if(cookie != null) {
			return cookie.getValue();
		}
		return null;
	}
	
	public String getFullPath() {
		return fullPath;
	}
	
	public HttpRequestHandler getHandler() {
		return handler;
	}
	
	public String getHeader(Header name) {
		return headers.get(name);
	}

	public String[] getHeaders() {
		return headers.toArray();
	}
	
	public String getHost() {
		if(host == null) {
			host = headers.get(HOST);
			int ix = host.indexOf(':');
			if(ix != -1) {
				host = host.substring(0, ix);
			}
		}
		return host;
	}
	
	public Integer getInteger(Header name) {
		try {
			return Integer.valueOf(headers.get(name));
		} catch(Exception e) {
			return null;
		}
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public Object getParameter(String name) {
		return parameters.get(name);
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getPort() {
		return port;
	}

	public String getQuery() {
		Object q = parameters.get("q");
		return (q instanceof String) ? (String) q : null;
	}
	
	public Map<String, Object> getQueryMap() {
		String query = getQuery();
		if(query != null) {
			return toMap(query);
		}
		return null;
	}

	public RequestType getType() {
		return type;
	}

	public boolean hasCookie(String name) {
		return cookies.containsKey(name);
	}
	
//	public boolean hasData(String key) {
//		return data != null && data.containsKey(key);
//	}
	
	public boolean hasHeader(Header name) {
		if(headers == null) {
			return false;
		}
		return headers.has(name);
	}
	
	public boolean hasParameter(String name) {
		if(parameters == null) {
			return false;
		}
		return parameters.containsKey(name);
	}
	
	public boolean hasParameters() {
		return parameters != null && !parameters.isEmpty();
	}
	
	public boolean isDelete() {
		return getType() == DELETE;
	}
	
	public boolean isGet() {
		return getType() == GET;
	}
	
	public boolean isHome() {
		return isGet() && "/".equals(getPath());
	}
	
	public boolean isLocalHost() {
		return ipAddress.equals("127.0.0.1");
	}
	
	public boolean isMultipart() {
		String contentType = headers.get(CONTENT_TYPE);
		return contentType != null && contentType.startsWith(MULTIPART.getType());
	}
	
	public boolean isPath(String path) {
		if(path != null) {
			return path.equals(getPath());
		}
		return false;
	}
	
	public boolean isPost() {
		return getType() == POST;
	}
	
	public boolean isPut() {
		return getType() == PUT;
	}

	private void loadContent(Data data) {
		if(headers.has(CONTENT_LENGTH)) {
			int contentLength;
			try {
				contentLength = Integer.parseInt(headers.get(CONTENT_LENGTH));
			} catch(NumberFormatException nfe) {
				contentLength = -1;
			}
			if(contentLength > 0) {
				if(parameters == null) {
					parameters = new HashMap<String, Object>();
				}
				byte[] content = data.getContent(contentLength);
				if(isMultipart()) {
					parseMultipart(content);
				} else {
					Map<String, String> map = attrsDecode(new String(content));
					if(map.containsKey(METHOD.key())) {
						headers.add(METHOD, map.remove(METHOD.key()));
					}
					parameters.putAll(map);
				}
			}
		}
	}
	
	private void parseCookies(Headers headers) {
		for(String header : headers.getAll(COOKIE)) {
			String[] sa = header.split(";\\s");
			for(String s : sa) {
				Cookie cookie = Cookie.parse(s.trim());
				if(cookie != null) {
					if(cookies == null) {
						cookies = new HashMap<String, HttpCookie>();
					}
					cookies.put(cookie.getName(), cookie);
				}
			}
		}
	}
	
	private void parseMultipart(byte[] content) {
		logger.info("parsing multipart");
		String[] sa = headers.get(CONTENT_TYPE).split("\\s*;\\s*");
		byte[] boundary = ("--" + sa[1].substring(9)).getBytes();
		if(logger.isLoggingInfo()) {
			logger.info(" boundary: " + new String(boundary));
		}
		int b1 = findAll(content, 0, content.length-1, boundary) + boundary.length;
		b1 = findAll(content, b1, content.length-1, CRLN) + 2;
		int b2 = findAll(content, b1, content.length-1, boundary);
		while(b1 != -1 && b2 != -1) {
			String name = null;
			ContentType type = null;
			int l1 = b1;
			int l2 = findAll(content, l1, b2, CRLN);
			while(l1 != -1 && l2 != -1) {
				if(l2 > l1) { // not an empty line
					String[] sa1 = new String(content, l1, l2-l1).split("\\s*;\\s*");
					String[] sa2 = sa1[0].split("\\s*:\\s*");
					if(CONTENT_DISPOSITION.key().equalsIgnoreCase(sa2[0])) {
						for(int i = 1; i < sa1.length; i++) {
							String[] nvp = attrDecode(sa1[i]);
							if("name".equalsIgnoreCase(nvp[0])) {
								name = nvp[1].substring(1, nvp[1].length()-1);
							} else {
								parameters.put(nvp[0], nvp[1]);
							}
						}
					} else if(CONTENT_TYPE.key().equalsIgnoreCase(sa2[0])) {
						type = parse(sa2[1]);
						parameters.put("data_type", sa2[1]);
					}
					l1 = l2 + 2;
					l2 = findAll(content, l1, b2, CRLN);
				} else { // an empty line - move into the value section
					l1 += 2; // get passed the empty line
					byte[] data = new byte[b2-l1-2];
					System.arraycopy(content, l1, data, 0, data.length);
					if(type != null && type.isBinary()) {
						if(logger.isLoggingInfo()) {
							logger.info(name + ": adding as byte[]");
						}
						parameters.put(name, data);
					} else {
						if(logger.isLoggingInfo()) {
							logger.info(name + ": adding as String");
						}
						parameters.put(name, new String(data));
					}
					parameters.put("data_length", Integer.toString(data.length));
					l1 = l2 = -1;
				}
			}
			b1 = b2 + boundary.length;
			b1 = findAll(content, b1, content.length-1, CRLN) + 2;
			b2 = findAll(content, b1, content.length-1, boundary);
		}
	}
	
	public void putParameter(String name, String value) {
		parameters.put(name, value);
	}
	
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
	
	public void setHandler(HttpRequestHandler handler) {
		this.handler = handler;
	}
	
	public void setInput(Data data) {
		loadContent(data);
		if(headers.has(METHOD)) {
			String newType = headers.get(METHOD);
			if("PUT".equalsIgnoreCase(newType)) {
				type = PUT;
			}
			if("DELETE".equalsIgnoreCase(newType)) {
				type = DELETE;
			}
		}
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
}
