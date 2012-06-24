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
import static org.oobium.utils.DateUtils.httpDate;

import java.io.File;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.oobium.app.http.MimeType;

public class StaticResponse extends Response {

	private static MimeType getMimeType(File file) {
		String name = file.getName();
		if(name.endsWith(".gz")) {
			name = name.substring(0, name.length()-3);
		}
		return MimeType.getFromExtension(name);
	}
	
	private static boolean isGzipped(File file) {
		return file.getName().endsWith(".gz");
	}
	
	
	private final Object payload;

	public StaticResponse(File file) {
		this(OK, getMimeType(file), file, Long.toString(file.length()), httpDate(file.lastModified()), isGzipped(file));
	}
	
	public StaticResponse(MimeType type, String payload) {
		this(OK, type, payload, Integer.toString(payload.length()), "0");
	}
	
	public StaticResponse(MimeType type, String payload, long lastModified) {
		this(OK, type, payload, Integer.toString(payload.length()), httpDate(lastModified));
	}
	
	public StaticResponse(MimeType type, Object payload, long length, long lastModified) {
		this(OK, type, payload, Long.toString(length), httpDate(lastModified));
	}
	
	public StaticResponse(MimeType type, Object payload, String length, String lastModified) {
		this(OK, type, payload, length, lastModified);
	}
	
	public StaticResponse(HttpResponseStatus status, MimeType type, Object payload, String length, String lastModified) {
		this(status, type, payload, length, lastModified, false);
	}
	
	public StaticResponse(HttpResponseStatus status, MimeType type, Object payload, String length, String lastModified, boolean gzipped) {
		super(status);
		setContentType(type);
		setHeader(HttpHeaders.Names.CONTENT_LENGTH, length);
		setHeader(HttpHeaders.Names.LAST_MODIFIED, lastModified);
		if(gzipped) {
			setHeader(HttpHeaders.Names.CONTENT_ENCODING, HttpHeaders.Values.GZIP);
		}
		this.payload = payload;
	}

	public Object getPayload() {
		return payload;
	}

}
