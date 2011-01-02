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
package org.oobium.app.server.response;

import static org.oobium.http.HttpRequest.Type.HEAD;
import static org.oobium.http.constants.ContentType.HTML;
import static org.oobium.http.constants.ContentType.JS;
import static org.oobium.http.constants.ContentType.JSON;
import static org.oobium.http.constants.StatusCode.NOT_FOUND;
import static org.oobium.http.constants.StatusCode.NOT_MODIFIED;
import static org.oobium.http.constants.StatusCode.OK;
import static org.oobium.http.constants.StatusCode.SERVER_ERROR;
import static org.oobium.utils.DateUtils.httpDate;
import static org.oobium.utils.StringUtils.blank;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.http.HttpRequest.Type;
import org.oobium.http.HttpResponse;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.StatusCode;
import org.oobium.http.impl.Cookie;
import org.oobium.http.impl.Headers;

public class Response implements HttpResponse {

	private static Response newInstance(StatusCode status, Type requestType, ContentType[] types) {
		Response response = new Response(requestType);
		response.setStatus(status);
		if(status != NOT_MODIFIED) {
			ContentType type = !blank(types) ? types[0] : null;
			if(JS == type || JSON == type) {
				response.setContentType(type);
				response.setBody("[\"[" + status.getCode() + "] " + status.getDescription() + "\"]");
			} else { // default to HTML
				response.setContentType(HTML);
				response.setBody(status.getCode() + "\n" + status.getDescription());
			}
		}
		return response;
	}
	
	public static Response notFound(Type requestType) {
		return newInstance(NOT_FOUND, requestType, null);
	}
	
	public static Response notFound(Type requestType, ContentType[] types) {
		return newInstance(NOT_FOUND, requestType, types);
	}
	
	public static Response notModified(Type requestType) {
		return newInstance(NOT_MODIFIED, requestType, null);
	}
	
	public static Response notModified(Type requestType, ContentType[] types) {
		return newInstance(NOT_MODIFIED, requestType, types);
	}
	
	public static Response serverError(Type requestType) {
		return newInstance(SERVER_ERROR, requestType, null);
	}
	
	public static Response serverError(Type requestType, ContentType[] types) {
		return newInstance(SERVER_ERROR, requestType, types);
	}
	
	private Headers headers;
	private Map<String, Cookie> cookies;
	private Type requestType;
	private StatusCode status;
	private ContentType contentType;
	private String body;
	
	public Response(Type requestType) {
		this.requestType = requestType;
		headers = new Headers();
		headers.add(Header.DATE, httpDate(new Date()));
	}
	
	public void addHeader(Header key, String value) {
		headers.add(key, value);
	}
	
	public void expireCookie(String name) {
		setCookie(name, Cookie.create(name, "", 0));
	}
	
	public String getBody() {
		return ((contentType == JS || contentType == JSON) && blank(body)) ? "null" : body;
	}
	
	@Override
	public ByteBuffer getBuffer() {
		StringBuilder sb = new StringBuilder(2048);
		if(cookies != null) {
			for(Cookie cookie : cookies.values()) {
				headers.add(Header.SET_COOKIE, cookie.toString());
			}
		}
		
		headers.set(Header.SERVER, "oobium");
		
		if(!headers.hasStatusLine()) {
			sb.append(getStatusHeader()).append('\r').append('\n');
		}

		for(String header : headers) {
			sb.append(header).append('\r').append('\n');
		}
		
		if(!headers.hasContentType()) {
			if(contentType != null && !contentType.isUnkown()) {
				sb.append(contentType.getHeader()).append('\r').append('\n');
			} else if(body != null) {
				sb.append(ContentType.PLAIN.getHeader()).append('\r').append('\n');
			}
		}
		
		if(requestType == HEAD) {
			sb.append('\r').append('\n').append('\n');
			return ByteBuffer.wrap(sb.toString().getBytes());
		} else {
			return getBuffer(sb);
		}
	}
	
	protected ByteBuffer getBuffer(StringBuilder sb) {
		if(hasBody()) {
			sb.append('\r').append('\n');
			sb.append(getBody());
		} else {
			sb.append('\r').append('\n').append('\n');
		}
		return ByteBuffer.wrap(sb.toString().getBytes());
	}

	public ContentType getContentType() {
		return contentType;
	}
	
	public Headers getHeaders() {
		return headers;
	}

	public StatusCode getStatus() {
		return status;
	}
	
	public String getStatusHeader() {
		return status.getStatusHeader();
	}
	
	public boolean hasBody() {
		return body != null && body.length() > 0;
	}
	
	public boolean hasHeaders() {
		return !headers.isEmpty();
	}

	public boolean isOK() {
		return OK == status;
	}
	
	public void setBody(String body) {
		setHeader(Header.CONTENT_LENGTH, Integer.toString(body.length()));
		this.body = body;
	}
	
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	
	private void setCookie(String name, Cookie cookie) {
		if(cookies == null) {
			cookies = new HashMap<String, Cookie>();
		}
		cookies.put(name, cookie);
	}
	
	public void setCookie(String name, String value) {
		setCookie(name, Cookie.create(name, value));
	}
	
	public void setCookie(String name, String value, Date expiration) {
		setCookie(name, Cookie.create(name, value, expiration));
	}
	
	public void setCookie(String name, String value, int seconds) {
		setCookie(name, Cookie.create(name, value, seconds));
	}
	
	public void setHeader(Header name, String value) {
		headers.set(name, value);
	}
	
	public void setHeaders(List<String> headers) {
		this.headers.setAll(headers);
	}
	
	public void setStatus(StatusCode code) {
		setHeader(Header.STATUS, code.getStatusHeaderValue());
		this.status = code;
	}
	
}
