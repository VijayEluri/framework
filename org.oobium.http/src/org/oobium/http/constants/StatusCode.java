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

import java.net.URLConnection;

/**
 * HTTP Status Codes.
 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">link http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html</a>
 */
public enum StatusCode {

	/**
	 * 200: The request sent by the client was successful.
	 */
	OK(200, "OK", "The request sent by the client was successful."),
	/**
	 * 201: The request was successful and a new resource was created.
	 */
	CREATED(201, "Created", "The request was successful and a new resource was created."),
	/**
	 * 202: The request has been accepted for processing, but has not yet been processed.
	 */
	ACCEPTED(202, "Accepted", "The request has been accepted for processing, but has not yet been processed."),
	/**
	 * 204: The request was successful but does not require the return of an entity-body.
	 */
	NO_CONTENT(204, "No Content", "The request was successful but does not require the return of an entity-body."),
	/**
	 * 301: The resource has permanently moved to a different URI.
	 */
	MOVED_PERM(301, "Moved Permanently", "The resource has permanently moved to a different URI."),
	/**
	 * 302: The resource has been found but the result is shown via a different URI.
	 */
	REDIRECT(302, "Found", "The resource has been found but the result is shown via a different URI."),
	/**
	 * 304: The resource has not been modified since the date requested.
	 */
	NOT_MODIFIED(304, "Not Modified", "The resource has not been modified since the date requested."),
	/**
	 * 307: The resource has temporarily been moved to a different URI. 
	 * The client should use the original URI to access the resource in future as the URI may change.
	 */
	MOVED_TEMP(307, "Temporary Redirect", "The resource has temporarily been moved to a different URI. " +
			"The client should use the original URI to access the resource in future as the URI may change."),
	/**
	 * 400: The syntax of the request was not understood by the server.
	 */
	BAD_REQUEST(400, "Bad Request", "The syntax of the request was not understood by the server."),
	/**
	 * 401: The request needs user authentication.
	 */
	NOT_AUTHORIZED(401, "Not Authorized", "The request needs user authentication."),
	/**
	 * 402: Reserved for future use.
	 */
	PAYMENT_REQUIRED(402, "Payment Required", "Reserved for future use."),
	/**
	 * 403: The server has refused to fulfill the request.
	 */
	FORBIDDEN(403, "Forbidden", "The server has refused to fulfill the request."),
	/**
	 * 404: The document/file requested by the client was not found.
	 */
	NOT_FOUND(404, "Not Found", "The document/file requested by the client was not found."),
	/**
	 * 408: The client failed to sent a request in the time allowed by the server.
	 */
	TIMEOUT(408, "Request Timeout", "The client failed to sent a request in the time allowed by the server."),
	/**
	 * 409: The request could not be completed due to a conflict with the current state of the resource.
	 */
	CONFLICT(409, "Conflict", "The request could not be completed due to a conflict with the current state of the resource."),
	/**
	 * 500: The request was unsuccessful due to an unexpected condition encountered by the server.
	 */
	SERVER_ERROR(500, "Internal Server Error", "The request was unsuccessful due to an unexpected condition encountered by the server."),
	/**
	 * 503: The request was unsuccessful to the server being down or overloaded.
	 */
	SERVER_UNAVAILABLE(503, "Service Unavailable", "The request was unsuccessful to the server being down or overloaded."),
	/**
	 * The response status code is not known
	 */
	UNKNOWN(-1, "Unknown", "The response status code is not known")
	;
	
	public static StatusCode get(int code) {
		for(StatusCode value : StatusCode.values()) {
			if(value.code == code) {
				return value;
			}
		}
		return StatusCode.UNKNOWN;
	}

	private final int code;
	private final String message;
	private final String description;
	
	private StatusCode(int code, String message, String description) {
		this.code = code;
		this.message = message;
		this.description = description;
	}

	public int getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}

	public String getMessage() {
		return message;
	}
	
	public String getStatusHeader() {
		return "HTTP/1.0 " + code + " - " + message;
	}
	
	public String getStatusHeaderValue() {
		return code + " - " + message;
	}
	
	public boolean isStatusOf(URLConnection conn) {
		return getStatusHeader().equals(conn.getHeaderField(0));
	}
	
	public boolean isSuccess() {
		return code >= 200 && code < 300;
	}
	
}
