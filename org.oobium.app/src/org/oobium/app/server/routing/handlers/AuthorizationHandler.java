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

import static org.oobium.http.constants.ContentType.HTML;
import static org.oobium.http.constants.Header.WWW_AUTHENTICATE;
import static org.oobium.http.constants.StatusCode.NOT_AUTHORIZED;

import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.http.HttpRequest;

public class AuthorizationHandler extends RouteHandler {

	private final String realm;
	
	public AuthorizationHandler(String realm) {
		this.realm = realm;
	}

	@Override
	public Response routeRequest(HttpRequest request) throws Exception {
		Response response = new Response(request.getType());
		response.setStatus(NOT_AUTHORIZED);
		response.addHeader(WWW_AUTHENTICATE, String.format("Basic realm=\"%1$s\"", realm));
		response.setContentType(HTML);
		response.setBody(
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"" +
				" \"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">" +
				"<HTML>" +
				"  <HEAD>" +
				"    <TITLE>Error</TITLE>" +
				"    <META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=ISO-8859-1\">" +
				"  </HEAD>" +
				"  <BODY><H1>401 Unauthorized.</H1></BODY>" +
				"</HTML>"
			);
		return response;
	}
	
}
