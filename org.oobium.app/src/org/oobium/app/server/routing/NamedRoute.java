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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.view.View;
import org.oobium.http.HttpRequest.Type;
import org.oobium.persist.Model;

public class NamedRoute {

	private final Router router;
	private final String name;
	
	NamedRoute(Router router, String name) {
		this.router = router;
		this.name = name;
	}
	
	public Routed asRoute(Class<? extends Controller> clazz, Type requestType) {
		Route route = router.addRoute(name, name, clazz, requestType);
		return new Routed(router, route);
	}
	
	public Routed asRoute(Class<? extends View> clazz) {
		validateView(clazz);
		Route route = router.addRoute(name, name, clazz);
		return new Routed(router, route);
	}
	
	public Routed asRoute(Controller controller) {
		Route route = router.addRoute(Type.GET, name, name, controller);
		return new Routed(router, route);
	}
	
	public Routed asRoute(String path, Controller controller) {
		Route route = router.addRoute(Type.GET, name, path, controller);
		return new Routed(router, route);
	}
	
	public Routed asRoute(Type requestType, Controller controller) {
		Route route = router.addRoute(requestType, name, name, controller);
		return new Routed(router, route);
	}
	
	public Routed asRoute(Type requestType, String path, Controller controller) {
		Route route = router.addRoute(requestType, name, path, controller);
		return new Routed(router, route);
	}
	
	public Routed asRoute(Class<?> clazz, Action action) {
		Route route = null;
		if(Model.class.isAssignableFrom(clazz)) {
			switch(action) {
			case create:	route = router.addRoute(name, name, clazz, action);					break;
			case update:	route = router.addRoute(name, name + "/{id}", clazz, action);		break;
			case destroy:	route = router.addRoute(name, name + "/{id}", clazz, action);		break;
			case show:		route = router.addRoute(name, name + "/{id}", clazz, action);		break;
			case showAll:	route = router.addRoute(name, name, clazz, action);					break;
			case showEdit:	route = router.addRoute(name, name + "/{id}/edit", clazz, action);	break;
			case showNew:	route = router.addRoute(name, name + "/new", clazz, action);		break;
			default:
				throw new IllegalArgumentException("unknown action: " + action);
			}
		} else if(Controller.class.isAssignableFrom(clazz)) {
			route = router.addRoute(name, name, clazz, action);
		} else {
			throw new IllegalArgumentException("invalid type: " + clazz.getSimpleName() + " (valid types are Model and Controller)");
		}
		return new Routed(router, route);
	}
	
	public Routed asRoute(String path, Class<? extends Controller> clazz, Type requestType) {
		Route route = router.addRoute(name, path, clazz, requestType);
		return new Routed(router, route);
	}
	
	public Routed asRoute(String path, Class<? extends View> clazz) {
		validateView(clazz);
		if(path.charAt(0) == '?') {
			path = name + path;
		}
		Route route = router.addRoute(name, path, clazz);
		return new Routed(router, route);
	}
	
	public Routed asRoute(String path, Class<?> clazz, Action action) {
		Route route = null;
		if(Model.class.isAssignableFrom(clazz)) {
			if(path.charAt(0) == '?') {
				path = "/{models}" + path;
			}
			route = router.addRoute(name, path, clazz, action);
			router.addNamedClass(name, clazz);
		} else if(Controller.class.isAssignableFrom(clazz)) {
			if(path.charAt(0) == '?') {
				path = name + path;
			}
			route = router.addRoute(name, path, clazz, action);
		} else {
			throw new IllegalArgumentException("invalid type: " + clazz.getSimpleName() + " (valid types are Model and Controller)");
		}
		return new Routed(router, route);
	}
	
	public Routed asRoutes(Class<? extends Controller> clazz, Type...requestTypes) {
		return asRoutes(name, clazz, requestTypes);
	}
	
	public Routed asRoutes(String path, Class<? extends Controller> clazz, Type...requestTypes) {
		if(requestTypes.length == 0) {
			requestTypes = Type.values();
		}
		List<Route> routes = new ArrayList<Route>();
		for(Type type : requestTypes) {
			if(path.charAt(path.length() - 1) == '*') {
				if(path.length() == 1 || path.charAt(path.length() - 2) == '/') {
					String base = (path.length() == 1) ? "" : path.substring(0, path.length()-2);
					routes.add(router.addRoute(name, base + "/(.*)", clazz, type));
					routes.add(router.addRoute(name, base + "/(.*)?(.*)", clazz, type));
					routes.add(router.addRoute(name, base, clazz, type));
				} else {
					throw new IllegalArgumentException("wildcard can only be the last full segment (" + path + ")");
				}
			} else {
				routes.add(router.addRoute(name, path, clazz, type));
			}
		}
		return new Routed(router, routes.toArray(new Route[routes.size()]));
	}

	private void validateView(Class<? extends View> clazz) {
		for(Constructor<?> ctor : clazz.getConstructors()) {
			if(ctor.getParameterTypes().length == 0) {
				return;
			}
		}
		throw new IllegalArgumentException("\"" + clazz.getSimpleName() + "\" must implement a no-args constructor to be routing directly");
	}

}
