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
package org.oobium.app.server.routing.handlers;

import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.app.server.routing.Router;
import org.oobium.http.HttpRequest;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.StatusCode;

public class RedirectHandler extends RouteHandler {

	public final String to;
	
	public RedirectHandler(Router router, String to) {
		super(router);
		this.to = to;
	}

	@Override
	public Response routeRequest(HttpRequest request) throws Exception {
		Response response = new Response(request.getType());
		response = new Response(request.getType());
		response.setStatus(StatusCode.REDIRECT);
		response.addHeader(Header.LOCATION, to);
		response.setBody(
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"" +
				" \"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">" +
				"<HTML>" +
				"  <HEAD>" +
				"    <TITLE>Redirect</TITLE>" +
				"    <META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=ISO-8859-1\">" +
				"  </HEAD>" +
				"  <BODY><H1>302 Redirect to " + to + "</H1></BODY>" +
				"</HTML>"
			);
		return response;
	}
	
	@Override
	public String toString() {
		return "Redirect to " + to;
	}
	
}
