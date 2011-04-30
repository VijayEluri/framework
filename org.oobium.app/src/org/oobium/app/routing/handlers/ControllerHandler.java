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

import org.oobium.app.controllers.Controller;
import org.oobium.app.http.Action;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;

public class ControllerHandler extends RouteHandler {

	public final Class<? extends Controller> controllerClass;
	public final Action action;
	
	public ControllerHandler(Router router, Class<? extends Controller> controllerClass, Action action, String[][] params) {
		super(router, params);
		this.controllerClass = controllerClass;
		this.action = action;
	}

	public Response routeRequest(Request request) throws Exception {
		Controller controller = controllerClass.newInstance();
		try {
			controller.initialize(router, request, getParamMap());
			controller.execute(action);
			return controller.getResponse();
		} finally {
			controller.clear();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(controllerClass.getSimpleName());
		sb.append('#');
		if(action != null) {
			sb.append(action);
		} else {
			sb.append("handleRequest");
		}
		if(params != null) {
			sb.append('(');
			for(int i = 0; i < params.length; i++) {
				if(i != 0) sb.append(',');
				sb.append(params[i][0]);
				if(params[i][1] != null) {
					sb.append('=').append(params[i][1]);
				}
			}
			sb.append(')');
		}
		return sb.toString();
	}
	
}
