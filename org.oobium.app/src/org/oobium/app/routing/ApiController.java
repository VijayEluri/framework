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

import static org.oobium.utils.json.JsonUtils.format;
import static org.oobium.utils.json.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.http.Action;
import org.oobium.app.routing.routes.HttpRoute;

public class ApiController extends HttpController {

	private Map<String, String> build(Route route) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("method", route.httpMethod.getName());
		if(route.isFixed()) {
			map.put("fixed", "true");
			map.put("path", route.path);
		} else {
			map.put("path", route.rule);
		}
		if(route instanceof HttpRoute) {
			HttpRoute cr = (HttpRoute) route;
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
	
	private List<Map<String, String>> createRouteList(Set<Route> routes) {
		HttpMethod type = hasParam("method") ? HttpMethod.valueOf(param("method").toUpperCase()) : null;

		List<Map<String, String>> results = new ArrayList<Map<String,String>>();
		for(Route route : routes) {
			if(type != null) {
				if(route.httpMethod == type) {
					results.add(build(route));
				}
			} else {
				results.add(build(route));
			}
		}
		
		return results;
	}
	
	@Override
	public void handleRequest() throws Exception {
		Router router = getRouter();
		Set<Route> routes = router.published;
		if(routes == null) {
			renderJson("[]");
			return;
		}
		
		if("models".equals(param("type"))) {
			Map<String, Map<String, Map<String, String>>> results = router.getModelRouteMap(routes);
			render(format(toJson(results)));
		} else {
			List<Map<String, String>> results = createRouteList(routes);
			render(format(toJson(results)));
		}
	}
	
}
