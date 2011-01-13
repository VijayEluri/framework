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

import static org.oobium.utils.StringUtils.blank;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;


public class ByteArrayResponse extends Response {

	private byte[] data;

	public ByteArrayResponse(RequestType requestType) {
		super(requestType);
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	protected ByteBuffer getBuffer(StringBuilder sb) {
		if(blank(data)) {
			sb.append('\r').append('\n').append('\n');
			return ByteBuffer.wrap(sb.toString().getBytes());
		} else {
			sb.append(Header.CONTENT_LENGTH.key()).append(": ").append(data.length).append('\r').append('\n');
			sb.append('\r').append('\n');
			byte[] buffer = Arrays.copyOf(sb.toString().getBytes(), sb.length() + data.length);
			System.arraycopy(data, 0, buffer, sb.length(), data.length);
			return ByteBuffer.wrap(buffer);
		}
	}

}
