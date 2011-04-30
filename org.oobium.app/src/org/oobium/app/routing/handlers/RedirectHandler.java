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
package org.oobium.app.routing.handlers;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static org.oobium.app.http.MimeType.HTML;
import static org.oobium.utils.DateUtils.httpDate;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.response.StaticResponse;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;

public class RedirectHandler extends RouteHandler {

	private static final ChannelBuffer content = ChannelBuffers.wrappedBuffer((
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"" +
			" \"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">" +
			"<HTML>" +
			"  <HEAD>" +
			"    <TITLE>Redirect</TITLE>" +
			"    <META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=ISO-8859-1\">" +
			"  </HEAD>" +
			"  <BODY><H1>302 Redirect</H1></BODY>" +
			"</HTML>"
		).getBytes());

	private static final String lastModified = httpDate(System.currentTimeMillis());
	
	private static final String length = Long.toString(content.readableBytes());

	
	public final String to;
	
	public RedirectHandler(Router router, String to) {
		super(router);
		this.to = to;
	}

	@Override
	public Response routeRequest(Request request) throws Exception {
		Response response = new StaticResponse(FOUND, HTML, content, length, lastModified);
		response.addHeader(HttpHeaders.Names.LOCATION, to);
		return response;
	}
	
	@Override
	public String toString() {
		return "Redirect to " + to;
	}
	
}
