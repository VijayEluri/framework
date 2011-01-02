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
import org.oobium.app.server.view.DynamicAsset;
import org.oobium.http.HttpRequest;

public class DynamicAssetHandler extends RouteHandler {

	public final Router router;
	public final Class<? extends DynamicAsset> assetClass;
	
	public DynamicAssetHandler(Router router, Class<? extends DynamicAsset> assetClass, String[][] params) {
		super(params);
		this.router = router;
		this.assetClass = assetClass;
	}

	@Override
	public Response routeRequest(HttpRequest request) throws Exception {
		DynamicAsset asset = assetClass.newInstance();
		return DynamicAsset.render(router, asset, request, getParamMap());
	}

}
