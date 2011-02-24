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

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.http.constants.StatusCode;

public class AssetResponse extends Response {

	private final URL url;
	private final String length;
	private final String lastModified;

	public AssetResponse(RequestType requestType, ContentType type, URL url, String length, String lastModified) {
		super(requestType);
		setStatus(StatusCode.OK);
		setContentType(type);
		this.url = url;
		this.length = length;
		this.lastModified = lastModified;
	}

	@Override
	public ReadableByteChannel getDataChannel() {
		try {
			return Channels.newChannel(url.openStream());
		} catch(IOException e) {
			return null;
		}
	}
	
	@Override
	public boolean hasDataChannel() {
		return url != null;
	}
	
	@Override
	protected ByteBuffer completeBuffer(StringBuilder sb) {
		sb.append(Header.LAST_MODIFIED.key()).append(':').append(lastModified).append('\r').append('\n');
		sb.append(Header.CONTENT_LENGTH.key()).append(':').append(length).append('\r').append('\n');
		sb.append('\r').append('\n');

		if(url == null) { // HEAD request
			sb.append('\n');
		}
		
		return ByteBuffer.wrap(sb.toString().getBytes());
	}
	
}
