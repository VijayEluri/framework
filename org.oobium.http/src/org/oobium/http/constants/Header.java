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
package org.oobium.http.constants;

public enum Header {

	ACCEPT("Accept"),
	API_LOCATION("API-Location"),
	AUTHORIZATION("Authorization"),
	COOKIE("Cookie"),
	CONNECTION("Connection"),
	CONTENT_DISPOSITION("Content-Disposition"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_TYPE("Content-Type"),
	DATE("Date"),
	ERROR("Error"),
	HOST("Host"),
	ID("id"),
	IF_MODIFIED_SINCE("If-Modified-Since"),
	LAST_MODIFIED("Last-Modified"),
	LOCATION("Location"),
	METHOD("_method"),
	SERVER("Server"),
	SET_COOKIE("Set-Cookie"),
	STATUS("Status"),
	SYSTEM_CMD("Oobium"),
	USER_AGENT("User-Agent"),
	WWW_AUTHENTICATE("WWW-Authenticate"),
	X_REQUESTED_WITH("X-Requested-With");
	
	private String key;
	private String lowerKey;
	
	private Header(String key) {
		this.key = key;
		this.lowerKey = key.toLowerCase();
	}

	/**
	 * Returns the key, or name, for this Header as it would be used in the HTTP Header section.
	 * <p>For example: the STATUS header key is Status.
	 * @return the header's key
	 */
	public String key() {
		return key;
	}
	
	public String lowerKey() {
		return lowerKey;
	}

	public String value(Object value) {
		return key + "=" + value;
	}
	
}
