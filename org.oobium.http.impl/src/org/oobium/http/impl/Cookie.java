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
package org.oobium.http.impl;

import java.util.Date;

import org.oobium.http.HttpCookie;

public class Cookie implements HttpCookie {

	private static void checkName(String name) throws IllegalArgumentException {
		if(name == null || name.length() == 0 || name.charAt(0) == '$' || name.contains(",") || name.contains(";")
				|| name.equalsIgnoreCase("Comment")
                || name.equalsIgnoreCase("CommentURL")      // rfc2965 only
                || name.equalsIgnoreCase("Discard")	        // rfc2965 only
                || name.equalsIgnoreCase("Domain")
//              || name.equalsIgnoreCase("Expires")	        // netscape draft only
                || name.equalsIgnoreCase("Max-Age")
                || name.equalsIgnoreCase("Path")
                || name.equalsIgnoreCase("Port")            // rfc2965 only
                || name.equalsIgnoreCase("Secure")
                || name.equalsIgnoreCase("Version") ) {
			throw new IllegalArgumentException("Illegal cookie name: " + name);
		}
	}
	
	public static Cookie parse(String str) throws IllegalArgumentException {
		if(str != null) {
			String[] sa = str.split("=", 2);
			checkName(sa[0]);
			Cookie cookie = new Cookie();
			cookie.name = sa[0];
			cookie.value = sa[1];
			return cookie;
		}
		throw new IllegalArgumentException("Illegal cookie format");
	}
	
	public static Cookie create(String name, String value) {
		checkName(name);
		Cookie cookie = new Cookie();
		cookie.name = name;
		cookie.value = value;
		return cookie;
	}
	
	public static Cookie create(String name, String value, int seconds) {
		checkName(name);
		Cookie cookie = new Cookie();
		cookie.name = name;
		cookie.value = value;
		cookie.maxAge = Math.max(0, seconds);
		return cookie;
	}
	
	public static Cookie create(String name, String value, Date expiration) {
		checkName(name);
		Cookie cookie = new Cookie();
		cookie.name = name;
		cookie.value = value;
		if(expiration != null) {
			cookie.maxAge = (int) (Math.max(0, expiration.getTime() - System.currentTimeMillis()) / 1000);
		}
		return cookie;
	}
	
	private String domain;
	private String path;
	private String name;
	private String value;
	private Integer maxAge;
	private boolean encrypted;
	private String comment;

	private Cookie() {
		// use create methods
	}

	@Override
	public String getComment() {
		return comment;
	}
	
	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public boolean isEncrypted() {
		return encrypted;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('=').append(value);
		if(maxAge != null) {
			sb.append("; Max-Age=").append(maxAge);
		}
		// TODO cookie path
//		if(path != null) {
//			sb.append("; path=").append(path);
			sb.append("; Path=/");
//		}
		if(domain != null) {
			sb.append("; Domain=").append(domain);
		}
		sb.append("; Version=1");
		return sb.toString();
	}

}
