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
import static org.oobium.app.http.Action.show;
import static org.oobium.app.http.Action.showAll;
import static org.oobium.persist.ModelAdapter.getAdapter;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findAny;
import static org.oobium.utils.CharStreamUtils.isEqual;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.encode;
import static org.oobium.utils.StringUtils.tableName;
import static org.oobium.utils.StringUtils.varName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.AppService;
import org.oobium.app.http.Action;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.handlers.AssetHandler;
import org.oobium.app.routing.handlers.AuthorizationHandler;
import org.oobium.app.routing.handlers.DynamicAssetHandler;
import org.oobium.app.routing.handlers.HttpHandler;
import org.oobium.app.routing.handlers.RedirectHandler;
import org.oobium.app.routing.handlers.RtspHandler;
import org.oobium.app.routing.handlers.ViewHandler;
import org.oobium.app.routing.handlers.WebsocketHandler;
import org.oobium.app.routing.routes.DynamicAssetRoute;
import org.oobium.app.routing.routes.HttpRoute;
import org.oobium.app.routing.routes.RedirectRoute;
import org.oobium.app.routing.routes.RtspRoute;
import org.oobium.app.routing.routes.StaticRoute;
import org.oobium.app.routing.routes.ViewRoute;
import org.oobium.app.routing.routes.WebsocketRoute;
import org.oobium.persist.Model;

public class AppRouter extends Router implements IPathRouting, IUrlRouting {

	private static final String API_NAME = "__api__";

	
	private final int port;
	private final String[] hosts;
	private List<Router> moduleRouters;
	private String apiHeader;
	

	public AppRouter(AppService service, String host, int port) {
		this(service, new String[] { host }, port);
	}

	public AppRouter(AppService service, String[] hosts, int port) {
		super(service);
		this.hosts = hosts;
		this.port = port;
		setApi(API_NAME, true);
	}

	public synchronized void add(Router moduleRouter) {
		if(moduleRouters == null) {
			moduleRouters = new ArrayList<Router>();
		}
		moduleRouters.add(moduleRouter);
	}

	public void addApiHeader(boolean addApiHeader) {
		String path = getRoute(this, API_NAME).path;
		this.apiHeader = addApiHeader ? path : null;
	}
	
	public void applyHeaders(Request request, Response response) {
		if(request.isHome()) {
			if(apiHeader != null) {
				response.setApiLocation(apiHeader);
			}
			String path = getModelNotificationsPath();
			if(path != null) {
				response.setHeader("API-WS-Location", path);
			}
		}
	}
	
	private String getModelNotificationsPath() {
		if(modelNotificationPath != null) {
			return modelNotificationPath;
		}
		if(moduleRouters != null) {
			for(Router router : moduleRouters) {
				if(router.modelNotificationPath != null) {
					return router.modelNotificationPath;
				}
			}
		}
		return null;
	}
	
	private String asUrl(String path) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://").append(hosts[0]);
		if(port != 80) {
			sb.append(':').append(port);
		}
		if(!blank(path)) {
			if(path.charAt(0) != '/') {
				sb.append('/');
			}
			sb.append(path);
		}
		return sb.toString();
	}
	
	/**
	 * substitute GET for HEAD when resolving routes;
	 * other methods are untouched
	 */
	private HttpMethod getMethod(Request request) {
		HttpMethod method = request.getMethod();
		if(HttpMethod.HEAD == method) {
			return HttpMethod.GET;
		}
		return method;
	}
	
	private RouteHandler checkAuthorization(Request request, Router router) {
		if(router.authentications != null) {
			Map<String, Realm> auths = router.authentications.get(getMethod(request));
			if(auths != null) {
				Realm realm = auths.get(request.getUri());
				if(realm == null) {
					realm = auths.get(request.getPath());
				}
				if(realm != null) {
					if(!router.isAuthorized(request, realm)) {
						return new AuthorizationHandler(router, realm.name());
					}
				}
			}
		}
		return null;
	}
	
	private RouteHandler checkAuthorization(Request request, Router router, HttpRoute route) {
		if(route.realm != null) {
			Realm realm = router.realms.get(route.realm);
			if(realm != null) {
				if(!router.isAuthorized(request, realm)) {
					return new AuthorizationHandler(router, realm.name());
				}
			}
		}
		return null;
	}
	
	private RouteHandler checkAuthorizations(Request request) {
		RouteHandler handler = checkAuthorization(request, this);
		if(handler == null && moduleRouters != null) {
			for(int i = 0; handler == null && i < moduleRouters.size(); i++) {
				handler = checkAuthorization(request, moduleRouters.get(i));
			}
		}
		return handler;
	}

	private RouteHandler getFixedRouteHandler(Request request) {
		RouteHandler handler = getFixedRouteHandler(request, this);
		if(handler == null && moduleRouters != null) {
			for(int i = 0; handler == null && i < moduleRouters.size(); i++) {
				handler = getFixedRouteHandler(request, moduleRouters.get(i));
			}
		}
		return handler;
	}
	
	private RouteHandler getFixedRouteHandler(Request request, Router router) {
		if(router.fixedRoutes != null) {
			Map<String, Route> fixedRoutes = router.fixedRoutes.get(getMethod(request));
			if(fixedRoutes != null) {
				Route fixedRoute = fixedRoutes.get(request.getUri());
				if(fixedRoute == null) {
					fixedRoute = fixedRoutes.get(request.getPath());
				}
				if(fixedRoute != null) {
					switch(fixedRoute.type) {
					case Route.ASSET:
						StaticRoute ar = (StaticRoute) fixedRoute;
						return new AssetHandler(router, ar.assetPath, ar.contentType, ar.length, ar.lastModified);
					case Route.AUTHORIZATION:
						throw new UnsupportedOperationException();
					case Route.HTTP_CONTROLLER:
						HttpRoute cr = (HttpRoute) fixedRoute;
						return new HttpHandler(router, cr.controllerClass, cr.action, cr.params);
					case Route.DYNAMIC_ASSET:
						DynamicAssetRoute dar = (DynamicAssetRoute) fixedRoute;
						return new DynamicAssetHandler(router, dar.assetClass, dar.params);
					case Route.REDIRECT:
						RedirectRoute rr = (RedirectRoute) fixedRoute;
						return new RedirectHandler(router, rr.to);
					case Route.RTSP_CONTROLLER:
						RtspRoute rc = (RtspRoute) fixedRoute;
						return new RtspHandler(router, rc.controllerClass, rc.params);
					case Route.VIEW:
						ViewRoute vr = (ViewRoute) fixedRoute;
						return new ViewHandler(router, vr.viewClass, vr.params);
					case Route.WEBSOCKET:
						WebsocketRoute wr = (WebsocketRoute) fixedRoute;
						return new WebsocketHandler(router, wr.controllerClass, wr.group, wr.params);
					default:
						throw new IllegalStateException();
					}
				}
			}
		}
		return null;
	}

	public RouteHandler getHandler(Request request) {
		if(port != request.getPort()) {
			return null;
		}
		
		if(!hasHost(request.getHost())) {
			return null;
		}

		RouteHandler handler;
		
		handler = checkAuthorizations(request);
		if(handler != null) {
			return handler;
		}

		handler = getFixedRouteHandler(request);
		if(handler != null) {
			return handler;
		}
		
		handler = getPatternRouteHandler(request);
		if(handler != null) {
			return handler;
		}
		
		return null;
	}

	public String getHost() {
		return hosts[0];
	}

	public String[] getHosts() {
		return hosts;
	}
	
	private String[][] getParams(Route cr, Matcher matcher) {
		String[][] params;
		String[][] cparams = cr.params();
		if(cparams == null) {
			params = null;
		} else {
			params = new String[cparams.length][];
			for(int i = 0; i < params.length; i++) {
				params[i] = new String[] { cparams[i][0], cparams[i][1] };
			}
			if(matcher.groupCount() > 0) {
				int group = 1;
				for(int i = 0; i < params.length; i++) {
					if(params[i][1] == null) {
						params[i][1] = matcher.group(group++);
					}
				}
			}
		}
		return params;
	}

	public List<String> getPaths() {
		List<String> paths = new ArrayList<String>();
		if(fixedRoutes != null) {
			for(Map<String, Route> map : fixedRoutes.values()) {
				paths.addAll(map.keySet());
			}
			if(moduleRouters != null) {
				for(Router router : moduleRouters) {
					if(router.fixedRoutes != null) {
						for(Map<String, Route> map : router.fixedRoutes.values()) {
							paths.addAll(map.keySet());
						}
					}
				}
			}
		}
		if(patternRoutes != null) {
			for(Route route : patternRoutes) {
				paths.add(route.rule);
			}
			if(moduleRouters != null) {
				for(Router router : moduleRouters) {
					if(router.patternRoutes != null) {
						for(Route route : router.patternRoutes) {
							paths.add(route.rule);
						}
					}
				}
			}
		}
		Collections.sort(paths);
		return paths;
	}
	
	public List<String> getPaths(HttpMethod method) {
		List<String> paths = new ArrayList<String>();
		if(fixedRoutes != null) {
			Map<String, Route> map = fixedRoutes.get(method);
			if(map != null) {
				paths.addAll(map.keySet());
			}
			if(moduleRouters != null) {
				for(Router router : moduleRouters) {
					if(router.fixedRoutes != null) {
						map = router.fixedRoutes.get(method);
						if(map != null) {
							paths.addAll(map.keySet());
						}
					}
				}
			}
		}
		if(patternRoutes != null) {
			for(Route route : patternRoutes) {
				if(route.httpMethod == method) {
					paths.add(route.rule);
				}
			}
			if(moduleRouters != null) {
				for(Router router : moduleRouters) {
					if(router.patternRoutes != null) {
						for(Route route : router.patternRoutes) {
							if(route.httpMethod == method) {
								paths.add(route.rule);
							}
						}
					}
				}
			}
		}
		Collections.sort(paths);
		return paths;
	}
	
	private String getPathTo(Router router, Class<? extends Model> modelClass, Action action) {
		if(router != null) {
			Route[] routes = router.routes.get(getKey(modelClass, action));
			if(routes != null) {
				IllegalArgumentException exception = null;
				for(Route route : routes) {
					try {
						return pathToClass(route.rule, modelClass);
					} catch(IllegalArgumentException e) {
						exception = e; // rule may have variables, which can't be handled without a model object
					}
				}
				if(exception != null) {
					throw exception;
				}
			}
		}
		return null;
	}
	
	public String getPathTo(Router router, String routeName, Object...params) {
		if(router != null) {
			Route[] routes = router.routes.get(routeName);
			if(routes != null) {
				for(Route route : routes) {
					if(route.isFixed()) {
						return route.path;
					} else {
						Class<?> clazz = (router.namedClasses != null) ? router.namedClasses.get(routeName) : null;
						return pathToClass(route.rule, clazz, params);
					}
				}
			}
		}
		return null;
	}
	
	private RouteHandler getPatternRouteHandler(Request request) {
		RouteHandler handler = getPatternRouteHandler(request, this);
		if(handler == null && moduleRouters != null) {
			for(int i = 0; handler == null && i < moduleRouters.size(); i++) {
				handler = getPatternRouteHandler(request, moduleRouters.get(i));
			}
		}
		return handler;
	}
	
	private RouteHandler getPatternRouteHandler(Request request, Router router) {
		if(router.patternRoutes != null) {
			HttpMethod method = getMethod(request);
			for(Route route : router.patternRoutes) {
				if(route.httpMethod == method) {
					Matcher matcher = route.matcher(route.matchOnFullPath ? request.getUri() : request.getPath());
					if(matcher.matches()) {
						switch(route.type) {
						case Route.ASSET:
						case Route.AUTHORIZATION:
							throw new UnsupportedOperationException();
						case Route.HTTP_CONTROLLER:
							HttpRoute cr = (HttpRoute) route;
							RouteHandler unauth = checkAuthorization(request, router, cr);
							return (unauth != null) ? unauth : new HttpHandler(router, cr.controllerClass, cr.action, getParams(cr, matcher));
						case Route.REDIRECT:
							RedirectRoute rr = (RedirectRoute) route;
							return new RedirectHandler(router, rr.to);
						case Route.VIEW:
							ViewRoute vr = (ViewRoute) route;
							return new ViewHandler(router, vr.viewClass, vr.params);
						default:
							throw new IllegalStateException("unknown route type: " + route.type);
						}
					}
				}
			}
		}
		return null;
	}

	public int getPort() {
		return port;
	}
	
	private Route getRoute(Router router, String key) {
		Route[] routes = (router != null) ? router.routes.get(key) : null;
		if(routes != null && routes.length > 0) {
			return routes[0];
		}
		routes = this.routes.get(key);
		if(routes != null && routes.length > 0) {
			return routes[0];
		}
		if(moduleRouters != null) {
			for(int i = 0; i < moduleRouters.size(); i++) {
				Router modRouter = moduleRouters.get(i);
				if(modRouter != router) {
					Route[] modRoutes = modRouter.routes.get(key);
					if(modRoutes != null && modRoutes.length > 0) {
						return modRoutes[0];
					}
				}
			}
		}
		return null;
	}
	
	public List<Route> getRoutes() {
		List<Route> routes = new ArrayList<Route>();
		if(fixedRoutes != null) {
			for(Map<String, Route> map : fixedRoutes.values()) {
				routes.addAll(map.values());
			}
			Collections.sort(routes, new Comparator<Route>() {
				@Override
				public int compare(Route r1, Route r2) {
					return r1.toString().compareTo(r2.toString());
				}
			});
		}
		if(patternRoutes != null) {
			routes.addAll(patternRoutes);
		}
		return routes;
	}

	public List<Route> getRoutes(HttpMethod method) {
		List<Route> routes = new ArrayList<Route>();
		if(fixedRoutes != null) {
			Map<String, Route> map = fixedRoutes.get(method);
			if(map != null) {
				routes.addAll(map.values());
			}
			Collections.sort(routes, new Comparator<Route>() {
				@Override
				public int compare(Route r1, Route r2) {
					return r1.toString().compareTo(r2.toString());
				}
			});
		}
		if(patternRoutes != null) {
			for(Route route : patternRoutes) {
				if(route.httpMethod == method) {
					routes.add(route);
				}
			}
		}
		return routes;
	}

	private String getValue(Model model, char[] ca, int s1, int s2) {
		int ix = findAny(ca, s1, s2, ':', '=');
		if(ix == -1) ix = s2;
		if(ix < s2 && ca[ix] == '=') {
			String param = new String(ca, ix+1, s2-ix-1).trim();
			return param;
		} else if(isEqual(ca, s1, ix, 'm','o','d','e','l','s')) {
			return tableName(model);
		} else {
			String param = new String(ca, s1, ix-s1).trim();
			Object value = model.get(param);
			if(value instanceof Model) {
				return String.valueOf(((Model) value).getId());
			} else {
				return String.valueOf(model.get(param));
			}
		}
	}
	
	private String getValue(Model parent, String field, char[] ca, int s1, int s2) {
		int ix = findAny(ca, s1, s2, ':', '=');
		if(ix == -1) ix = s2;
		if(ix < s2 && ca[ix] == '=') {
			String param = new String(ca, ix+1, s2-ix-1).trim();
			return param;
		} else if(isEqual(ca, s1, ix, 'm','o','d','e','l','s')) {
			return tableName(getAdapter(parent.getClass()).getRelationClass(field));
		} else {
			String param = new String(ca, s1, ix-s1).trim();
			Object value;
			int ix2 = find(ca, '[', s1, ix);
			if(ix2 == -1) {
				throw new IllegalArgumentException("not a valid variable: " + param);
			} else {
				String cname = new String(ca, s1, ix2-s1);
				if(cname.equals(varName(parent.getClass()))) {
					String f = new String(ca, ix2+1, ix-ix2-2);
					value = parent.get(f);
				} else {
					throw new IllegalArgumentException("not a valid variable: " + param);
				}
			}
			if(value instanceof Model) {
				return String.valueOf(((Model) value).getId());
			} else {
				return String.valueOf(value);
			}
		}
	}

	public boolean hasHost(String host) {
		if(host != null && host.length() > 0) {
			for(String h : hosts) {
				if(h.equals(host)) {
					return true;
				}
			}
		}
		return false;
	}

	private String pathError(Object obj1, String field) {
		if(obj1 == null) {
			logger.warn(new Exception("cannot find a path to a null object"));
		} else if(obj1 instanceof Model) {
			if(field == null) {
				logger.warn(new Exception("could not find path for model: " + obj1.getClass().getSimpleName()));
			} else {
				if(obj1 instanceof Class<?>) {
					logger.warn(new Exception("could not find path for hasMany: " + obj1.getClass().getSimpleName() + " -> " + field));
				} else {
					logger.warn(new Exception("could not find path for hasMany: " + obj1.getClass().getSimpleName() + " -> " + field));
				}
			}
		} else if(obj1 instanceof Class<?>) {
			logger.warn(new Exception("could not find path for class: " + ((Class<?>) obj1).getSimpleName()));
		} else if(obj1 instanceof String) {
			logger.warn(new Exception("could not find path named: " + obj1));
		} else if(obj1 instanceof Exception) {
			logger.warn("could not resolve path due an exception: " + ((Exception) obj1).getMessage(), (Exception) obj1);
		} else {
			logger.warn(new Exception("could not find path for: " + obj1));
		}
		return UNKNOWN_PATH;
	}
	
	@Override
	public String pathTo(Class<? extends Model> modelClass) {
		return pathTo((Router) null, modelClass);
	}
	
	@Override
	public String pathTo(Class<? extends Model> modelClass, Action action) {
		return pathTo((Router) null, modelClass, action);
	}

	@Override
	public String pathTo(Model model) {
		return pathTo((Router) null, model);
	}
	
	@Override
	public String pathTo(Model model, Action action) {
		return pathTo((Router) null, model, action);
	}
	
	@Override
	public String pathTo(Model parent, String field) {
		return pathTo((Router) null, parent, field);
	}

	@Override
	public String pathTo(Model parent, String field, Action action) {
		return pathTo((Router) null, parent, field, action);
	}
	
	public String pathTo(Router router, Class<? extends Model> modelClass) {
		return pathTo(router, modelClass, Action.showAll);
	}
	
	public String pathTo(Router router, Class<? extends Model> modelClass, Action action) {
		if(modelClass != null) {
			IllegalArgumentException exception = null;
			String path = null;
			// try the initial router
			try {
				path = getPathTo(router, modelClass, action);
			} catch(IllegalArgumentException e) {
				exception = e;
			}
			if(path == null) {
				// try the application router
				try {
					path = getPathTo(this, modelClass, action);
				} catch(IllegalArgumentException e) {
					exception = e;
				}
				if(path == null && moduleRouters != null) {
					// try the others...
					for(int i = 0; path == null && i < moduleRouters.size(); i++) {
						Router modRouter = moduleRouters.get(i);
						if(modRouter != router) {
							try {
								path = getPathTo(moduleRouters.get(i), modelClass, action);
							} catch(IllegalArgumentException e) {
								exception = e;
							}
						}
					}
				}
			}
			if(path != null) {
				return path;
			}
			if(exception != null) {
				return pathError(exception, null);
			}
		}
		return pathError(modelClass, null);
	}
	
	public String pathTo(Router router, Model model) {
		return pathTo(router, model, Action.show);
	}

	public String pathTo(Router router, Model model, Action action) {
		if(model != null) {
			Route route = getRoute(router, getKey(model.getClass(), action));
			if(route != null) {
				return pathToModel(route.rule, model);
			}
		}
		return pathError(model, null);
	}

	public String pathTo(Router router, Model parent, String field) {
		if(parent != null && field != null) {
			Action action = getAdapter(parent).hasMany(field) ? showAll : show;
			Route route = getRoute(router, getKey(parent.getClass(), field, action));
			if(route != null) {
				return pathToHasMany(route.rule, parent, field);
			}
		}
		return pathError(parent, field);
	}

	public String pathTo(Router router, Model parent, String field, Action action) {
		if(parent != null && field != null) {
			Route route = getRoute(router, getKey(parent.getClass(), field, action));
			if(route != null) {
				return pathToHasMany(route.rule, parent, field);
			}
		}
		return pathError(parent, field);
	}

	public String pathTo(Router router, String routeName) {
		return pathTo(router, routeName, new Object[0]);
	}
	
	public String pathTo(Router router, String routeName, Model model) {
		if(!blank(routeName)) {
			Route route = getRoute(router, routeName);
			if(route != null) {
				return pathToModel(route.rule, model);
			}
		}
		return pathError(routeName, null);
	}
	
	public String pathTo(Router router, String routeName, Object...params) {
		if(params.length == 1 && params[0] instanceof Model) {
			return pathTo(router, routeName, (Model) params[0]);
		}
		
		if(!blank(routeName)) {
			IllegalArgumentException exception = null;
			String path = null;
			// try initial router
			try {
				path = getPathTo(router, routeName, params);
			} catch(IllegalArgumentException e) {
				exception = e;
			}
			if(path == null) {
				// try application router
				try {
					path = getPathTo(this, routeName, params);
				} catch(IllegalArgumentException e) {
					exception = e;
				}
				if(path == null && moduleRouters != null) {
					// try the rest...
					for(int i = 0; path == null && i < moduleRouters.size(); i++) {
						try {
							path = getPathTo(moduleRouters.get(i), routeName, params);
						} catch(IllegalArgumentException e) {
							exception = e;
						}
					}
				}
			}
			if(path != null) {
				return path;
			}
			if(exception != null) {
				return pathError(exception, null);
			}
		}
		return pathError(routeName, null);
	}

	@Override
	public String pathTo(String routeName) {
		return pathTo((Router) null, routeName);
	}
	
	@Override
	public String pathTo(String routeName, Model model) {
		return pathTo((Router) null, routeName, model);
	}

	@Override
	public String pathTo(String routeName, Object... params) {
		return pathTo((Router) null, routeName, params);
	}
	
	/**
	 * TODO switch to using PathBuilder
	 */
	@Deprecated
	private String pathToClass(String path, Class<?> clazz, Object...params) {
		StringBuilder sb = new StringBuilder(path.length() + 20);
		char[] ca = path.toCharArray();
		int pix = find(ca, '?');
		if(pix == -1) pix = ca.length;
		int s0 = 0;
		int s1 = find(ca, '{');
		int i = 0;
		while(s1 != -1) {
			sb.append(ca, s0, s1-s0);
			int s2 = closer(ca, s1);

			int ix = find(ca, '=', s1, s2);
			if(ix != -1) {
				if(s1 < pix) {
					sb.append(new String(ca, ix+1, s2-ix-1).trim());
				} // else skip - it is handled by the routing and does not need to be in the path
			} else {
				ix = find(ca, ':', s1, s2);
				if(ix != -1) {
					if(i < params.length) {
						String value = String.valueOf(params[i]);
						String regex = new String(ca, ix+1, s2-ix-1).trim();
						if(Pattern.matches(regex, value)) {
							sb.append(params[i]);
						} else {
							throw new IllegalArgumentException("invalid value for " + new String(ca, s1, s2-s1+1) + ": " + value);
						}
					} else {
						throw new IllegalArgumentException("cannot evaluate " + new String(ca, s1, s2-s1+1) + ": no parameter given");
					}
					i++;
				} else {
					String s = new String(ca, s1+1, s2-s1-1).trim();
					if("models".equals(s)) {
						if(clazz != null) {
							sb.append(tableName(clazz));
						} else if(i < params.length && params[i] instanceof Class<?>) {
							sb.append(tableName((Class<?>) params[i]));
						} else {
							throw new IllegalArgumentException("cannot evaluate {models}: no class given");
						}
					} else if("id".equals(s)) {
						if(i < params.length) {
							if(params[i] instanceof Number) {
								sb.append(((Number) params[i]).longValue());
								i++;
							} else {
								throw new IllegalArgumentException("cannot evaluate {id}: " + params[i] + " is not a number");
							}
						} else {
							throw new IllegalArgumentException("cannot evaluate {id}: no parameter given");
						}
					} else {
						throw new IllegalArgumentException("class path contains an unknown variable: " + new String(ca));
					}
				}
			}
			
			s0 = s2 + 1;
			s1 = find(ca, '{', s0);
		}
		if(s0 < ca.length) {
			sb.append(ca, s0, ca.length-s0);
		}
		if(sb.charAt(sb.length()-1) == '?') {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	private String pathToHasMany(String path, Model parent, String field) {
		if(path == null) {
			return UNKNOWN_PATH;
		}
		
		StringBuilder sb = new StringBuilder(path.length() + 20);
		char[] ca = path.toCharArray();
		int pix = find(ca, '?');
		if(pix == -1) pix = ca.length;
		int s0 = 0;
		int s1 = find(ca, '{');
		while(s1 != -1) {
			sb.append(ca, s0, s1-s0);
			int s2 = closer(ca, s1);
			s1++;
			if(s1 < pix) {
				sb.append(getValue(parent, field, ca, s1, s2));
			} else { // in parameter section
				int ix = find(ca, ':', s1, s2);
				if(ix != -1) {
					String f = new String(ca, s1, ix-s1).trim();
					Object value = parent.get(f);
					sb.append(f).append('=');
					if(!blank(value)) sb.append(encode(value.toString()));
				} else {
					if(find(ca, '=', s1, s2) == -1) {
						sb.append(getValue(parent, field, ca, s1, s2));
					}
				}
			}
			s0 = s2 + 1;
			s1 = find(ca, '{', s0);
		}
		if(s0 < ca.length) {
			sb.append(ca, s0, ca.length-s0);
		}
		if(sb.charAt(sb.length()-1) == '?') {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	/**
	 * TODO switch to using PathBuilder
	 */
	@Deprecated
	private String pathToModel(String path, Model model) {
		StringBuilder sb = new StringBuilder(path.length() + 20);
		char[] ca = path.toCharArray();
		int pix = find(ca, '?');
		if(pix == -1) pix = ca.length;
		int s0 = 0;
		int s1 = find(ca, '{');
		while(s1 != -1) {
			sb.append(ca, s0, s1-s0);
			int s2 = closer(ca, s1);
			s1++;
			if(s1 < pix) {
				sb.append(getValue(model, ca, s1, s2));
			} else { // in parameter section
				int ix = find(ca, ':', s1, s2);
				if(ix != -1) {
					String field = new String(ca, s1, ix-s1).trim();
					Object value = model.get(field);
					sb.append(field).append('=');
					if(!blank(value)) sb.append(encode(value.toString()));
				} else {
					if(find(ca, '=', s1, s2) == -1) {
						sb.append(getValue(model, ca, s1, s2));
					}
				}
			}
			s0 = s2 + 1;
			s1 = find(ca, '{', s0);
		}
		if(s0 < ca.length) {
			sb.append(ca, s0, ca.length-s0);
		}
		if(sb.charAt(sb.length()-1) == '?') {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	public synchronized void remove(Router moduleRouter) {
		if(moduleRouters != null && moduleRouters.remove(moduleRouter)) {
			if(moduleRouters.isEmpty()) {
				moduleRouters = null;
			}
		}
	}

	public void removeApi() {
		remove(API_NAME);
	}
	
	public void setApi(String path) {
		setApi(path, false);
	}

	public void setApi(String path, boolean addApiHeader) {
		remove(API_NAME);
		if(path != null) {
			add(API_NAME).asRoute(GET, path, ApiController.class);
		}
		this.apiHeader = addApiHeader ? path : null;
	}

	public void setApi(String path, String realm) {
		setApi(path, false);
	}

	public void setApi(String path, String realm, boolean addApiHeader) {
		setApi(path, apiHeader);
		addBasicAuthentication(path, realm);
	}

	@Override
	public String urlTo(Class<? extends Model> modelClass) {
		return urlTo((Router) null, modelClass);
	}

	@Override
	public String urlTo(Class<? extends Model> modelClass, Action action) {
		return urlTo((Router) null, modelClass, action);
	}

	@Override
	public String urlTo(Model model) {
		return urlTo((Router) null, model);
	}

	@Override
	public String urlTo(Model model, Action action) {
		return urlTo((Router) null, model, action);
	}

	@Override
	public String urlTo(Model parent, String field) {
		return urlTo((Router) null, parent, field);
	}

	@Override
	public String urlTo(Model parent, String field, Action action) {
		return urlTo((Router) null, parent, field, action);
	}

	public String urlTo(Router router, Class<? extends Model> modelClass) {
		return urlTo(router, modelClass, Action.showAll);
	}

	public String urlTo(Router router, Class<? extends Model> modelClass, Action action) {
		return asUrl(pathTo(router, modelClass, action));
	}

	public String urlTo(Router router, Model model) {
		return urlTo(router, model, Action.show);
	}

	public String urlTo(Router router, Model model, Action action) {
		return asUrl(pathTo(router, model, action));
	}

	public String urlTo(Router router, Model parent, String field) {
		return asUrl(pathTo(router, parent, field));
	}

	public String urlTo(Router router, Model parent, String field, Action action) {
		return asUrl(pathTo(router, parent, field, action));
	}

	public String urlTo(Router router, String routeName) {
		return asUrl(pathTo(router, routeName));
	}

	public String urlTo(Router router, String routeName, Model model) {
		return asUrl(pathTo(router, routeName, model));
	}

	public String urlTo(Router router, String routeName, Object... params) {
		return asUrl(pathTo(router, routeName, params));
	}

	@Override
	public String urlTo(String routeName) {
		return urlTo((Router) null, routeName);
	}

	@Override
	public String urlTo(String routeName, Model model) {
		return urlTo((Router) null, routeName, model);
	}

	@Override
	public String urlTo(String routeName, Object... params) {
		return urlTo((Router) null, routeName, params);
	}

}
