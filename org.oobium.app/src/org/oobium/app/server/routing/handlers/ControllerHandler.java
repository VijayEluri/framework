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

import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.app.server.routing.Router;
import org.oobium.http.HttpRequest;

public class ControllerHandler extends RouteHandler {

	public final Router router;
	public final Class<? extends Controller> controllerClass;
	public final Action action;
	
	public ControllerHandler(Router router, Class<? extends Controller> controllerClass, Action action, String[][] params) {
		super(params);
		this.router = router;
		this.controllerClass = controllerClass;
		this.action = action;
	}

	public Response routeRequest(HttpRequest request) throws Exception {
		Controller controller = controllerClass.newInstance();
		try {
			controller.initialize(router, request, getParamMap());
			controller.execute(action);
			return controller.getResponse();
		} finally {
			controller.clear();
		}
	}

}
