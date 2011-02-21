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

import static org.oobium.http.constants.Action.show;
import static org.oobium.http.constants.Action.showAll;
import static org.oobium.http.constants.RequestType.DELETE;
import static org.oobium.http.constants.RequestType.GET;
import static org.oobium.http.constants.RequestType.POST;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.oobium.app.ModuleService;
import org.oobium.app.dev.controllers.NotifyController;
import org.oobium.app.dev.controllers.PathsController;
import org.oobium.app.dev.controllers.PersistServicesController;
import org.oobium.app.dev.controllers.ShutdownController;
import org.oobium.app.dev.views.application.Error500;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.Router;
import org.oobium.app.server.view.View;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpRequest500Handler;
import org.oobium.http.HttpResponse;
import org.oobium.http.constants.StatusCode;
import org.oobium.utils.Config;

public class AppDevActivator extends ModuleService implements HttpRequest500Handler {

	public static final String ID = AppDevActivator.class.getPackage().getName();
	
	
	@Override
	public void addRoutes(Config config, Router router) {
		router.addAssetRoutes();

		router.add("notify").asRoute(POST, ID, NotifyController.class);
		router.add("paths").asRoute(GET, "{app:[\\w\\.]+}/paths", PathsController.class);
		router.add("shutdown").asRoute(DELETE, "/", ShutdownController.class);
		
		router.add("persist_service").asRoute("/persist_services/{id}", PersistServicesController.class, show);
		router.add("persist_services").asRoute("/persist_services", PersistServicesController.class, showAll);
	}

	@Override
	public HttpResponse handle500(HttpRequest request, Exception exception) {
		try {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				exception.printStackTrace(pw);
				pw.close();

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
					sb.append(line).append("</div>");
				}

				String trace = sb.toString().replaceAll("at (([\\.\\w\\$]+)\\.\\w+)\\(([\\.\\w]+:(\\d+))\\)",
						"at $1(<a href=\"/#\" onclick=\"\\$.post('" + ID + "', {type:'$2', line:$4});return false;\">$3</a>)");
				Response response = View.render(new Error500(exception, trace), request);
				response.setStatus(StatusCode.SERVER_ERROR);
				return response;
			} catch(Exception e) {
				logger.warn(e);
				// fall through
			}
		} catch(Exception e) {
			logger.warn(e);
			// fall through
		}
		return Response.serverError(request.getType());
	}

}
