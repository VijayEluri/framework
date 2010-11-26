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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.http.HttpRequest;

public class ControllerHandler extends RouteHandler {

	public final Controller controller;
	public final Class<? extends Controller> controllerClass;
	public final Action action;
	
	public ControllerHandler(Controller controller, Class<? extends Controller> controllerClass, Action action, String[][] params) {
		super(params);
		this.controller = controller;
		this.controllerClass = controllerClass;
		this.action = action;
	}

	public Response routeRequest(HttpRequest request) throws Exception {
		Controller controller;
		if(this.controller == null) {
			Constructor<? extends Controller> ctor = controllerClass.getConstructor(HttpRequest.class, Map.class);
			controller = ctor.newInstance(request, getParamMap());
		} else {
			controller = this.controller;
			controller.initialize(request, getParamMap());
		}
		controller.execute(action);
		return controller.getResponse();
	}

}
