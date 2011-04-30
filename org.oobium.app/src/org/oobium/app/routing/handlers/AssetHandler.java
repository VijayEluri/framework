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

import java.net.URL;

import org.oobium.app.http.MimeType;
import org.oobium.app.request.Request;
import org.oobium.app.response.StaticResponse;
import org.oobium.app.response.Response;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;

public class AssetHandler extends RouteHandler {

	private final String assetPath;
	private final MimeType type;
	private final String length;
	private final String lastModified;
	
	public AssetHandler(Router router, String assetPath, MimeType type, String length, String lastModified) {
		super(router);
		this.assetPath = assetPath;
		this.type = type;
		this.length = length;
		this.lastModified = lastModified;
	}

	@Override
	public Response routeRequest(Request request) throws Exception {
		ClassLoader loader = router.getService().getClass().getClassLoader();
		URL url = loader.getResource(assetPath);
		if(url != null) {
			return new StaticResponse(type, url, length, lastModified);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return assetPath;
	}
	
}
