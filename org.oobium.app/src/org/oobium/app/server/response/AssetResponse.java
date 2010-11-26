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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.oobium.http.HttpRequest.Type;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.StatusCode;

public class AssetResponse extends Response {

	private final URL url;
	private final String length;
	private final String lastModified;

	public AssetResponse(Type requestType, ContentType type, URL url, String length, String lastModified) {
		super(requestType);
		setStatus(StatusCode.OK);
		setContentType(type);
		this.url = url;
		this.length = length;
		this.lastModified = lastModified;
	}

	@Override
	protected ByteBuffer getBuffer(StringBuilder sb) {
		sb.append(Header.LAST_MODIFIED.key()).append(':').append(lastModified).append('\r').append('\n');
		sb.append(Header.CONTENT_LENGTH.key()).append(':').append(length).append('\r').append('\n');
		sb.append('\r').append('\n');

		if(url == null) { // HEAD request
			sb.append('\n');
			return ByteBuffer.wrap(sb.toString().getBytes());
		} else {
			int n;
			byte[] buf = new byte[2048];
			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(url.openStream());
				while((n = bis.read(buf)) > 0) {
					out.write(buf, 0, n);
				}
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				if(bis != null) {
					try {
						bis.close();
					} catch(IOException e) {
						
					}
				}
			}
			byte[] data = out.toByteArray();

			byte[] buffer = Arrays.copyOf(sb.toString().getBytes(), sb.length() + data.length);
			System.arraycopy(data, 0, buffer, sb.length(), data.length);
			return ByteBuffer.wrap(buffer);
		}
	}
	
}
