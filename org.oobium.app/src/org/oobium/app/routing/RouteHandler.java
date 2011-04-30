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
package org.oobium.app.routing;

import java.util.HashMap;
import java.util.Map;

import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.logging.Logger;

public abstract class RouteHandler {

	public final Router router;
	protected Logger logger;
	public final String[][] params;
	
	public RouteHandler(Router router) {
		this.router = router;
		params = null;
	}
	
	public RouteHandler(Router router, String[][] params) {
		this.router = router;
		this.params = params;
	}
	
	public abstract Response routeRequest(Request request) throws Exception; 

	protected Map<String, Object> getParamMap() {
		if(params != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			for(String[] param : params) {
				map.put(param[0], param[1]);
			}
			return map;
		}
		return null;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
}
