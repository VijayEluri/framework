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

import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.oobium.app.routing.Router.DEFAULT_MODEL_NOTIFY_PATH;
import static org.oobium.app.routing.Router.MODEL_NOTIFY_GROUP;
import static org.oobium.app.routing.Router.checkClass;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.controllers.RtspController;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.app.http.Action;
import org.oobium.app.routing.routes.WebsocketRoute;
import org.oobium.app.views.View;
import org.oobium.persist.Model;

public class NamedRoute {

	private final Router router;
	private final String name;
	
	NamedRoute(Router router, String name) {
		this.router = router;
		this.name = name;
	}
	
	public Routed asModelNotifier() {
		return asModelNotifier(DEFAULT_MODEL_NOTIFY_PATH);
	}
	
	public Routed asModelNotifier(String path) {
		if(path.charAt(0) == '?') {
			path = name + path;
		}
		WebsocketRoute route = router.addWebsocketRoute(name, path, null, MODEL_NOTIFY_GROUP);
		return new Routed(router, route);
	}
	
	public RoutedWebsocket asNotifier(String path) {
		if(path.charAt(0) == '?') {
			path = name + path;
		}
		WebsocketRoute route = router.addWebsocketRoute(name, path, null, name);
		return new RoutedWebsocket(router, route);
	}
	
	/**
	 * Add a single named resource route to this router for the given model class and action.
	 * <p>Refer to the {@link Action} class for details on how the given action denotes the path and request type.</p>
	 * @param clazz the model class to use as a resource route
	 * @param action the action that this resource route is for
	 * @return a Routed object
	 * @see Action
	 */
	public Routed asResource(Class<? extends Model> clazz, Action action) {
		Route route = null;
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
		return new Routed(router, route);
	}
	
	/**
	 * Add a single named resource route to this router for the given model class and action.
	 * Unlike {@link #addResource(Class, Action)}, this method allows for overriding the conventional path.
	 * <p>Refer to the {@link Action} class for details on how the given action denotes the path and request type.</p>
	 * @param clazz the model class to use as a resource route
	 * @param action the action that this resource route is for
	 * @return a Routed object
	 * @see Action
	 */
	public Routed asResource(String path, Class<? extends Model> clazz, Action action) {
		Route route = null;
		if(Model.class.isAssignableFrom(clazz)) {
			if(path.charAt(0) == '?') {
				path = "/{models}" + path;
			}
			route = router.addRoute(name, path, clazz, action);
			router.addNamedClass(name, clazz);
		} else if(HttpController.class.isAssignableFrom(clazz)) {
			if(path.charAt(0) == '?') {
				path = name + path;
			}
			route = router.addRoute(name, path, clazz, action);
		} else {
			throw new IllegalArgumentException("invalid type: " + clazz.getSimpleName() + " (valid types are Model and Controller)");
		}
		return new Routed(router, route);
	}
	
	/**
	 * Add a route to be handled by the given controller's handleRequest method. The request type
	 * to be handled is GET and the path is equal to the name of this NamedRoute.
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed asRoute(Class<? extends HttpController> clazz) {
		return asRoute(GET, name, clazz);
	}
	
	/**
	 * Add a route to be handled by the given controller. The request path handled is the equal to
	 * the name of this NamedRoute. The type of
	 * request handled, and the controller method called to handle it, are determined by the given action.
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 * @see Action
	 */
	public Routed asRoute(Class<? extends HttpController> clazz, Action action) {
		return asRoute(name, clazz, action);
	}
	
	/**
	 * Add a route to be handled by the given controller's handleRequest method. The request path
	 * to be handled is equal to the name of this {@link NamedRoute}.
	 * @param requestType the type of the request this route will handle
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed asRoute(HttpMethod method, Class<? extends HttpController> clazz) {
		return asRoute(method, name, clazz);
	}
	
	/**
	 * Add a route to be handled by the given controller's handleRequest method.
	 * @param requestType the type of the request this route will handle
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed asRoute(HttpMethod method, String path, Class<? extends HttpController> clazz) {
		checkClass(clazz);
		if(path.charAt(path.length() - 1) == '*') {
			if(path.length() == 1 || path.charAt(path.length() - 2) == '/') {
				List<Route> routes = new ArrayList<Route>();
				String base = (path.length() == 1) ? "" : path.substring(0, path.length()-2);
				routes.add(router.addRoute(name, base + "/(.*)", clazz, method));
				routes.add(router.addRoute(name, base + "/(.*)?(.*)", clazz, method));
				routes.add(router.addRoute(name, base, clazz, method));
				return new Routed(router, routes.toArray(new Route[routes.size()]));
			} else {
				throw new IllegalArgumentException("wildcard can only be the last full segment (" + path + ")");
			}
		} else {
			Route route = router.addRoute(name, path, clazz, method);
			return new Routed(router, route);
		}
	}
	
	/**
	 * Add a route to be handled by the given controller's handleRequest method. The request type
	 * to be handled is GET.
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed asRoute(String path, Class<? extends HttpController> clazz) {
		return asRoute(GET, path, clazz);
	}

	/**
	 * Add a route to be handled by the given controller. The type of
	 * request handled, and the controller method called to handle it, are determined by the given action.
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 * @see Action
	 */
	public Routed asRoute(String path, Class<? extends HttpController> clazz, Action action) {
		checkClass(clazz);
		Route route = router.addRoute(name, path, clazz, action);
		return new Routed(router, route);
	}
	
	public Routed asRtsp(Class<? extends RtspController> clazz) {
		checkClass(clazz);
		Route route = router.addRtsp(name, name, clazz);
		return new Routed(router, route);
	}
	
	public Routed asRtsp(String path, Class<? extends RtspController> clazz) {
		checkClass(clazz);
		if(path.charAt(0) == '?') {
			path = name + path;
		}
		Route route = router.addRtsp(name, path, clazz);
		return new Routed(router, route);
	}
	
	/**
	 * Add a named view route for the given view class. The new route will render the view in response to
	 * a GET request with a path of <code>varName(clazz)</code>.<br>
	 * For example: addView(Home.class) will render the Home view for a "GET /home" request.
	 * @param clazz the view to be rendered
	 * @return a Routed object
	 */
	public Routed asView(Class<? extends View> clazz) {
		checkClass(clazz);
		Route route = router.addRoute(name, name, clazz);
		return new Routed(router, route);
	}
	
	/**
	 * Add a named view route to render the given view class in response to a GET request with the given path.<br>
	 * @param path the path that this request will handle (may contain constants, but not variables)
	 * @param clazz the view to be rendered
	 * @return a Routed object
	 */
	public Routed asView(String path, Class<? extends View> clazz) {
		checkClass(clazz);
		if(path.charAt(0) == '?') {
			path = name + path;
		}
		Route route = router.addRoute(name, path, clazz);
		return new Routed(router, route);
	}
	
	public RoutedWebsocket asWebsocket(String path, Class<? extends WebsocketController> controller) {
		if(path.charAt(0) == '?') {
			path = name + path;
		}
		WebsocketRoute route = router.addWebsocketRoute(name, path, controller, controller.getClass().getName());
		return new RoutedWebsocket(router, route);
	}
	
}
