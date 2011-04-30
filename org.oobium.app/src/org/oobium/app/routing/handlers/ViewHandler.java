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

import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;
import org.oobium.app.views.View;

public class ViewHandler extends RouteHandler {

	public final Class<? extends View> viewClass;
	
	public ViewHandler(Router router, Class<? extends View> viewClass, String[][] params) {
		super(router, params);
		this.viewClass = viewClass;
	}

	@Override
	public Response routeRequest(Request request) throws Exception {
		View view = viewClass.newInstance();
		return View.render(router, view, request, getParamMap());
	}

	@Override
	public String toString() {
		return viewClass.getSimpleName();
	}

}
