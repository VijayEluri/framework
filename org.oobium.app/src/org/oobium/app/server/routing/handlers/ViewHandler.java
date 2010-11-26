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
import org.oobium.app.server.view.View;
import org.oobium.http.HttpRequest;

public class ViewHandler extends RouteHandler {

	public final Class<? extends View> viewClass;
	
	public ViewHandler(Class<? extends View> viewClass, String[][] params) {
		super(params);
		this.viewClass = viewClass;
	}

	@Override
	public Response routeRequest(HttpRequest request) throws Exception {
		return View.render(viewClass, request, getParamMap());
	}

}
