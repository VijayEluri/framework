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
package org.oobium.http;

import java.util.Collection;
import java.util.Map;

import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;

public interface HttpRequest {

	public static final int UNKNOWN = -1;
	
	public static final String DELETE_METHOD = "_delete";
	public static final String PUT_METHOD = "_put";

	public static final int HTTP = 0;
	public static final int HTTPS = 1;
	
	public abstract ContentType[] getContentTypes();

	public abstract HttpCookie getCookie(String name);

	public abstract Collection<HttpCookie> getCookies();

	public abstract String getCookieValue(String name);

	public abstract String getFullPath();
	
	public abstract HttpRequestHandler getHandler();
	
	public abstract String getHeader(Header name);

	public abstract String[] getHeaders();
	
	public abstract Integer getInteger(Header name);

	public abstract String getIpAddress();

	public abstract Object getParameter(String name);

	public abstract Map<String, Object> getParameters();

	public abstract String getPath();

	public abstract int getPort();
	
	public abstract String getQuery();

	public abstract Map<String, Object> getQueryMap();

	public abstract RequestType getType();

	public abstract String getHost();

	public abstract boolean hasCookie(String name);

	public abstract boolean hasHeader(Header name);

	public abstract boolean hasParameter(String name);

	public abstract boolean hasParameters();

	public abstract boolean isDelete();

	public abstract boolean isGet();

	public abstract boolean isHome();

	public abstract boolean isLocalHost();

	public abstract boolean isMultipart();

	public abstract boolean isPath(String path);

	public abstract boolean isPost();

	public abstract boolean isPut();

}
