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
package org.oobium.app.dev;

import static org.jboss.netty.handler.codec.http.HttpMethod.DELETE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.oobium.app.http.Action.show;
import static org.oobium.app.http.Action.showAll;
import static org.oobium.app.http.MimeType.HTML;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.oobium.app.ModuleService;
import org.oobium.app.dev.controllers.EventController;
import org.oobium.app.dev.controllers.PathsController;
import org.oobium.app.dev.controllers.PersistServicesController;
import org.oobium.app.dev.controllers.ShutdownController;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.Router;
import org.oobium.utils.Config;

public class AppDevActivator extends ModuleService implements HttpRequest500Handler {

	@Override
	public void addRoutes(Config config, Router router) {
		router.addAssetRoutes();

		router.add("events").asRoute(POST, "/events/{eventType:\\w+}", EventController.class);
		router.add("paths").asRoute(GET, "/{app:[\\w\\.]+}/paths", PathsController.class);
		router.add("shutdown").asRoute(DELETE, "/", ShutdownController.class);
		
		router.add("persist_service").asRoute("/persist_services/{id}", PersistServicesController.class, show);
		router.add("persist_services").asRoute("/persist_services", PersistServicesController.class, showAll);
	}

	@Override
	public Response handle500(Request request, Throwable cause) {
		try {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				cause.printStackTrace(pw);
				pw.close();

				String html = 
					"<html>\n" +
					"<head>\n" +
					" <title>500 - Server Error</title>\n" +
					" <style>\n" +
					"  .indent { margin-left:15px }\n" +
					"  .trace  { color: red }\n" +
					" </style>\n" +
					" <script src='/jquery-1.4.2.dev.min.js'></script>\n" +
					"</head>\n" +
					"<body>\n" +
					" <h2 class=\"trace\">" + cause.getLocalizedMessage() + "</h2>\n" +
					" <div class=\"trace\">" +
					"{trace}\n" +
					" </div>\n" +
					"</body>\n" +
					"</html>";
				
				String s = sw.toString();
				StringBuilder sb = new StringBuilder(s.length() * 2);
				BufferedReader reader = new BufferedReader(new StringReader(s));
				String line;
				while((line = reader.readLine()) != null) {
					if(line.charAt(0) == '\t') {
						sb.append("<div class=\"indent\">");
					} else {
						sb.append("<div>");
					}
					sb.append(line).append("</div>\n");
				}

				String trace = sb.toString().replaceAll("at (([\\.\\w\\$]+)\\.\\w+)\\(([\\.\\w]+:(\\d+))\\)",
						"at $1(<a href=\"/#\" onclick=\"\\$.post('/events/open_type', {type:'$2', line:$4});return false;\">$3</a>)");

				html = html.replace("{trace}", trace);
				
				Response response = new Response(INTERNAL_SERVER_ERROR);
				response.setContentType(HTML);
				response.setContent(html);
				return response;
			} catch(Exception e) {
				logger.warn(e);
				// fall through
			}
		} catch(Exception e) {
			logger.warn(e);
			// fall through
		}
		return null;
	}

}
