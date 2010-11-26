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
package org.oobium.app.server.routing;

import static org.oobium.utils.json.JsonUtils.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.routing.routes.ControllerRoute;
import org.oobium.http.HttpRequest.Type;

class DiscoveryController extends Controller {

	private final Router router;
	
	DiscoveryController(Router router) {
		this.router = router;
	}

	private Map<String, String> build(Route route) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("method", route.requestType.name());
		if(route.isFixed()) {
			map.put("fixed", "true");
			map.put("path", route.path);
		} else {
			map.put("path", route.rule);
		}
		if(route instanceof ControllerRoute) {
			ControllerRoute cr = (ControllerRoute) route;
			Class<?> c = cr.modelClass;
			if(c != null) {
				map.put("model", c.getName());
			}
			Action a = cr.action;
			if(a != null) {
				map.put("action", a.name());
			}
		}
		return map;
	}
	
	@Override
	public void handleRequest() throws SQLException {
		Set<Route> routes = router.published;
		if(routes == null) {
			renderJson("[]");
			return;
		}
		
		Type type = null;
		boolean models = "models".equals(param("q"));
		if(!models) {
			type = Type.valueOf(param("q").toUpperCase());
		}

		List<Map<String, String>> results = new ArrayList<Map<String,String>>();
		for(Route route : routes) {
			if(models) {
				if(route instanceof ControllerRoute) {
					if(((ControllerRoute) route).modelClass != null) {
						results.add(build(route));
					}
				}
			} else if(type != null) {
				if(route.requestType == type) {
					results.add(build(route));
				}
			} else {
				results.add(build(route));
			}
		}
		
		render(format(toJson(results)));
	}

}
