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
import org.oobium.app.views.StyleSheet;

public class StyleSheetHandler extends RouteHandler {

	public final Class<? extends StyleSheet> assetClass;
	
	public StyleSheetHandler(Router router, Class<? extends StyleSheet> assetClass, String[][] params) {
		super(router, params);
		this.assetClass = assetClass;
	}

	@Override
	public Response routeRequest(Request request) throws Exception {
		StyleSheet asset = assetClass.newInstance();
		return StyleSheet.render(router, asset, request, getParamMap());
	}
	
	@Override
	public String toString() {
		return assetClass.getSimpleName();
	}

}
