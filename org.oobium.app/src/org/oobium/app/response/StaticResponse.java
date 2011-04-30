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

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

import java.io.File;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.oobium.app.http.MimeType;

public class StaticResponse extends Response {

	private final Object payload;

	public StaticResponse(File file) {
		this(MimeType.getFromExtension(file.getName()), file, file.length(), file.lastModified());
	}
	
	public StaticResponse(MimeType type, Object payload, long length, long lastModified) {
		this(OK, type, payload, Long.toString(length), Long.toString(lastModified));
	}
	
	public StaticResponse(MimeType type, Object payload, String length, String lastModified) {
		this(OK, type, payload, length, lastModified);
	}
	
	public StaticResponse(HttpResponseStatus status, MimeType type, Object payload, String length, String lastModified) {
		super(status);
		setContentType(type);
		setHeader(HttpHeaders.Names.CONTENT_LENGTH, length);
		setHeader(HttpHeaders.Names.LAST_MODIFIED, lastModified);
		this.payload = payload;
	}

	public Object getPayload() {
		return payload;
	}

}
