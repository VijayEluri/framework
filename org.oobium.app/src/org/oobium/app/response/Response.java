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
package org.oobium.app.response;

import static org.oobium.utils.DateUtils.httpDate;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.oobium.app.http.MimeType;

public class Response extends DefaultHttpResponse {

	private Map<String, Cookie> cookies;
	
	public Response() {
		this(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	}
	
	public Response(HttpResponseStatus status) {
		this(HttpVersion.HTTP_1_1, status);
	}
	
	public Response(HttpVersion version, HttpResponseStatus status) {
		super(version, status);
		setHeader(HttpHeaders.Names.SERVER, "oobium");
		setHeader(HttpHeaders.Names.DATE, httpDate());
	}

	private void addCookie(Cookie cookie) {
		if(cookies == null) {
			cookies = new HashMap<String, Cookie>();
		}
		cookies.put(cookie.getName(), cookie);
		removeHeader(HttpHeaders.Names.SET_COOKIE);
		for(Cookie c : cookies.values()) {
			CookieEncoder encoder = new CookieEncoder(true);
			encoder.addCookie(c);
			addHeader(HttpHeaders.Names.SET_COOKIE, encoder.encode());
		}
	}
	
	public void expireCookie(String name) {
		setCookie(name, "", 0);
	}

	public String getApiLocation() {
		return getHeader("API-Location");
	}

	public byte[] getContentAsBytes() {
		ChannelBuffer content = getContent();
		if(content == null) {
			return null;
		}
		byte[] bytes = new byte[content.readableBytes()];
		content.getBytes(0, bytes);
		return bytes;
	}
	
	public String getContentAsString() {
		ChannelBuffer content = getContent();
		if(content == null) {
			return null;
		}
		return content.toString(CharsetUtil.UTF_8);
	}
	
	public MimeType getContentType() {
		String header = getHeader(HttpHeaders.Names.CONTENT_TYPE);
		if(header != null) {
			int ix = header.indexOf(';');
			if(ix != -1) {
				header = header.substring(0, ix);
			}
			return MimeType.valueOf(header);
		}
		return null;
	}
	
	public boolean isSuccess() {
		int code = getStatus().getCode();
		return code >= 200 && code < 300;
	}
	
	public void setApiLocation(String location) {
		setHeader("API-Location", location);
	}
	
	public void setContent(String content) {
		setContent(content, CharsetUtil.UTF_8);
	}
	
	public void setContent(String content, Charset charset) {
		if(content == null) {
			setContent(ChannelBuffers.EMPTY_BUFFER);
		} else {
			setContent(ChannelBuffers.copiedBuffer(content, charset));
		}
	}

	public void setContentType(MimeType type) {
		setHeader(HttpHeaders.Names.CONTENT_TYPE, type.contentType);
	}
	
	public void setCookie(String name, Object value) {
		setCookie(name, String.valueOf(value));
	}

	public void setCookie(String name, String value) {
		setCookie("/", name, value);
	}
	
	public void setCookie(String path, String name, String value) {
		Cookie cookie = new DefaultCookie(name, value);
		cookie.setPath(path);
		addCookie(cookie);
	}
	
	public void setCookie(String name, Object value, int maxAge) {
		setCookie(name, String.valueOf(value), maxAge);
	}
	
	public void setCookie(String name, String value, int maxAge) {
		setCookie("/", name, value, maxAge);
	}
	
	public void setCookie(String path, String name, String value, int maxAge) {
		Cookie cookie = new DefaultCookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		addCookie(cookie);
	}
	
}
