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

import static org.oobium.app.server.controller.Action.create;
import static org.oobium.app.server.controller.Action.destroy;
import static org.oobium.app.server.controller.Action.show;
import static org.oobium.app.server.controller.Action.showAll;
import static org.oobium.app.server.controller.Action.showEdit;
import static org.oobium.app.server.controller.Action.showNew;
import static org.oobium.app.server.controller.Action.update;
import static org.oobium.http.HttpRequest.Type.DELETE;
import static org.oobium.http.HttpRequest.Type.GET;
import static org.oobium.http.HttpRequest.Type.POST;
import static org.oobium.http.HttpRequest.Type.PUT;
import static org.oobium.persist.ModelAdapter.getAdapter;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findAny;
import static org.oobium.utils.CharStreamUtils.isEqual;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.controllerCanonicalName;
import static org.oobium.utils.StringUtils.encode;
import static org.oobium.utils.StringUtils.getResourceAsString;
import static org.oobium.utils.StringUtils.tableName;
import static org.oobium.utils.StringUtils.underscored;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.json.JsonUtils.toStringList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oobium.app.AssetProvider;
import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.handlers.AssetHandler;
import org.oobium.app.server.routing.handlers.AuthorizationHandler;
import org.oobium.app.server.routing.handlers.ControllerHandler;
import org.oobium.app.server.routing.handlers.DynamicAssetHandler;
import org.oobium.app.server.routing.handlers.ViewHandler;
import org.oobium.app.server.routing.routes.AssetRoute;
import org.oobium.app.server.routing.routes.ControllerRoute;
import org.oobium.app.server.routing.routes.DynamicAssetRoute;
import org.oobium.app.server.routing.routes.ViewRoute;
import org.oobium.app.server.view.DynamicAsset;
import org.oobium.app.server.view.View;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpRequest.Type;
import org.oobium.http.HttpResponse;
import org.oobium.http.constants.Header;
import org.oobium.logging.ILogger;
import org.oobium.persist.Model;
import org.oobium.utils.Base64;

public class Router implements IPathRouting, IUrlRouting {

	private static final String DISCOVERY_DEFAULT = "__discovery__";

	public static final String UNKNOWN_PATH = "/#";
	public static final String HOME = "Home";

	private final ILogger logger;

	private final int port;
	private final String[] hosts;
	
	private Map<String, Route[]> routes;
	private List<Route> patternRoutes;
	private Map<Type, Map<String, Route>> fixedRoutes;
	private Map<String, Class<?>> namedClasses;
	private Map<String, String> hasMany;

	private Map<String, Realm> realms;
	private Map<Type, Map<String, Realm>> authentications;
	
	private Map<String, ClassLoader> controllers;

	private String discoveryHeader;
	Set<Route> published;
	
	public Router(ILogger logger, String host, int port) {
		this.logger = logger;
		this.hosts = new String[] { host };
		this.port = port;
	}

	public Router(ILogger logger, String[] hosts, int port) {
		this.logger = logger;
		this.hosts = hosts;
		this.port = port;
	}

	/**
	 * Create a new named route.
	 * @param name the name of the new route
	 * @return a {@link NamedRoute} object, to which actual routes can be added
	 * @throws IllegalArgumentException if the given name is not valid for named routes
	 */
	public NamedRoute add(String name) {
		validateName(name);
		return new NamedRoute(this, name);
	}

	public Routed addAssetRoutes(AssetProvider provider) {
		List<String> paths = provider.getAssetList();
		List<Route> routes = new ArrayList<Route>();
		if(!blank(paths)) {
			for(String path : paths) {
				if(path.startsWith("/images/")) {
					path = path.substring(7);
				} else if(path.startsWith("/scripts/")) {
					path = path.substring(8);
				} else if(path.startsWith("/styles/")) {
					path = path.substring(7);
				}
				String[] sa = path.split("\\|", 4);
				String key = provider.getClass().getCanonicalName() + ":" + sa[0];
				Route route = new AssetRoute(checkRule(sa[0]), provider, sa[1], sa[2]);
				routes.add(route);
				addRoute(key, route);
				if(sa.length == 4) {
					addBasicAuthentication(Type.GET, sa[0], sa[3]);
				}
			}
			return new Routed(this, routes.toArray(new Route[routes.size()]));
		}
		return new Routed(this);
	}
	
	public void addBasicAuthentication(String path, String realm) {
		addBasicAuthentication(GET, path, realm);
	}
	
	public void addBasicAuthentication(Type requestType, String path, String realm) {
		if(realms == null) {
			realms = new HashMap<String, Realm>();
		}
		Realm r = realms.get(realm);
		if(r == null) {
			r = new Realm(realm);
			realms.put(realm, r);
		}
		if(authentications == null) {
			authentications = new HashMap<Type, Map<String,Realm>>();
		}
		Map<String, Realm> auth = authentications.get(requestType);
		if(auth == null) {
			auth = new HashMap<String, Realm>();
			authentications.put(requestType, auth);
		}
		auth.put(path, r);
	}
	
	public void addBasicAuthorization(String realm, String username, String password) {
		if(realms == null) {
			realms = new HashMap<String, Realm>();
		}
		Realm r = realms.get(realm);
		if(r == null) {
			r = new Realm(realm);
			realms.put(realm, r);
		}
		r.authorize(username, password);
	}

	public void addControllerNames(ClassLoader loader, Collection<String> controllerNames) {
		if(controllers == null) {
			controllers = new LinkedHashMap<String, ClassLoader>();
		}
		for(String name : controllerNames) {
			controllers.put(name, loader);
		}
	}

	void addHasMany(String parentKey, String key) {
		if(hasMany == null) {
			hasMany = new HashMap<String, String>();
		}
		hasMany.put(parentKey, key);
	}
	
	void addNamedClass(String name, Class<?> clazz) {
		if(namedClasses == null) {
			namedClasses = new HashMap<String, Class<?>>();
		}
		namedClasses.put(name, clazz);
	}

	public Routed addRoute(Class<? extends DynamicAsset> clazz) {
		validateNoArgsCtor(clazz);
		String name = "/" + underscored(clazz.getCanonicalName());
		name = name.replaceAll("\\.", "/") + "." + DynamicAsset.getFileExtension(clazz);
		Route route = new DynamicAssetRoute(GET, name, clazz);
		addRoute(name, route);
		return new Routed(this, route);
	}

	public Routed addRoute(Class<? extends Model> clazz, Action action) {
		return addRoute(null, clazz, action);
	}

	public Routed addRoute(String path, Class<? extends Model> clazz, Action action) {
		if(path != null && path.charAt(0) == '?') {
			path = "/{models}/{id}" + path;
		}
		Route route = null;
		switch(action) {
		case create:	route = addRoute(getKey(clazz, action), (path == null) ? "/{models}" 			: path, clazz, action); break;
		case update:	route = addRoute(getKey(clazz, action), (path == null) ? "/{models}/{id}" 	 	: path, clazz, action); break;
		case destroy:	route = addRoute(getKey(clazz, action), (path == null) ? "/{models}/{id}" 	 	: path, clazz, action); break;
		case show:		route = addRoute(getKey(clazz, action), (path == null) ? "/{models}/{id}" 	 	: path, clazz, action); break;
		case showAll:	route = addRoute(getKey(clazz, action), (path == null) ? "/{models}" 			: path, clazz, action); break;
		case showEdit:	route = addRoute(getKey(clazz, action), (path == null) ? "/{models}/{id}/edit"	: path, clazz, action); break;
		case showNew:	route = addRoute(getKey(clazz, action), (path == null) ? "/{models}/new" 		: path, clazz, action); break;
		default:
			throw new IllegalArgumentException("unknown action: " + action);
		}
		return new Routed(this, route);
	}
	
	private void addRoute(String key, Route route) {
		Route[] ra;
		if(routes == null) {
			routes = new HashMap<String, Route[]>();
			ra = new Route[1];
		} else {
			ra = routes.get(key);
			if(ra == null) {
				ra = new Route[1];
			} else {
				// don't add duplicates
				for(Route r : ra) {
					if(r.equals(route)) return;
				}
				Route[] tmp = new Route[ra.length + 1];
				System.arraycopy(ra, 0, tmp, 1, ra.length);
				ra = tmp;
			}
		}
		ra[0] = route; // add at 0 so that iterations will happen with the last added going first
		routes.put(key, ra);

		if(route.isFixed()) {
			if(fixedRoutes == null) {
				fixedRoutes = new HashMap<Type, Map<String,Route>>();
			}
			Map<String, Route> routes = fixedRoutes.get(route.requestType);
			if(routes == null) {
				routes = new HashMap<String, Route>();
				fixedRoutes.put(route.requestType, routes);
			}
			routes.put(route.path, route);
		} else {
			if(patternRoutes == null) {
				patternRoutes = new ArrayList<Route>();
			}
			patternRoutes.add(route);
		}
	}
	
	Route addRoute(String key, String rule, Class<? extends Controller> clazz, Type requestType) {
		rule = checkRule(rule);
		Route route = new ControllerRoute(requestType, rule, null, clazz, null);
		addRoute(key, route);
		return route;
	}
	
	Route addRoute(String key, String rule, Class<? extends View> clazz) {
		rule = checkRule(rule);
		Route route = new ViewRoute(GET, rule, clazz);
		addRoute(key, route);
		return route;
	}
	
	ControllerRoute addRoute(String key, String rule, Class<?> clazz, Action action) {
		rule = checkRule(rule);
		Type type;
		switch(action) {
		case create:	type = POST;	break;
		case update:	type = PUT;		break;
		case destroy:	type = DELETE;	break;
		case show:		type = GET;		break;
		case showAll:	type = GET;		break;
		case showEdit:	type = GET;		break;
		case showNew:	type = GET;		break;
		default:
			throw new IllegalArgumentException("unknown action: " + action);
		}
		Class<? extends Model> modelClass = Model.class.isAssignableFrom(clazz) ? clazz.asSubclass(Model.class) : null;
		Class<? extends Controller> controllerClass = getControllerClass(clazz);
		ControllerRoute route = new ControllerRoute(type, rule, modelClass, controllerClass, action);
		addRoute(key, route);
		return route;
	}
	
	public Routed addRoute(Type requestType, String path, Controller controller) {
		String name = (path.charAt(0) == '/') ? path.substring(1) : path;
		name = name.replaceAll("\\{([^\\}^\\:^\\=]+)[\\:\\=]?[^\\}]*\\}", "$1").replaceAll("/", "_");
		return add(name).asRoute(requestType, path, controller);
	}
	
	Route addRoute(Type requestType, String key, String rule, Controller controller) {
		rule = checkRule(rule);
		Route route = new ControllerRoute(requestType, rule, controller, null);
		addRoute(key, route);
		return route;
	}

	public Routes addRoutes(Class<? extends Model> clazz, Action...actions) {
		if(actions.length == 0) {
			actions = Action.values();
		}
		List<Routed> routed = new ArrayList<Routed>();
		for(Action action : actions) {
			routed.add(addRoute(clazz, action));
		}
		return new Routes(this, routed, clazz, "/{models}/{id}");
	}

	public Routes addRoutes(String path, Class<? extends Model> clazz, Action...actions) {
		String[] parsedRules = parseRules(path, clazz);
		
		if(actions.length == 0) {
			actions = Action.values();
		}

		List<Routed> routed = new ArrayList<Routed>();
		for(Action action : actions) {
			Routed r = null;
			switch(action) {
			case create:	r = addRoute(parsedRules[create.ordinal()],		clazz, action); break;
			case destroy:	r = addRoute(parsedRules[destroy.ordinal()],	clazz, action); break;
			case update:	r = addRoute(parsedRules[update.ordinal()], 	clazz, action); break;
			case show:		r = addRoute(parsedRules[show.ordinal()],		clazz, action); break;
			case showAll:	r = addRoute(parsedRules[showAll.ordinal()],	clazz, action); break;
			case showEdit:	r = addRoute(parsedRules[showEdit.ordinal()],	clazz, action); break;
			case showNew:	r = addRoute(parsedRules[showNew.ordinal()],	clazz, action); break;
			default:
				throw new IllegalArgumentException("unknown action: " + action);
			}
			routed.add(r);
		}
		return new Routes(this, routed, clazz, path);
	}

	public void applyHeaders(HttpRequest request, HttpResponse response) {
		if(discoveryHeader != null && request.isHome()) {
			if(response instanceof Response) {
				((Response) response).addHeader(Header.API_LOCATION, discoveryHeader);
			}
		}
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
	
	private String checkRule(String rule) {
		logger.debug(rule);
		if(rule.charAt(0) != '/') {
			rule = "/" + rule;
		}
		if(rule.length() > 1 && rule.charAt(rule.length()-1) == '/') {
			rule = new String(rule.substring(0, rule.length()-1));
		}
		return rule;
	}
	
	public void clear() {
		if(patternRoutes != null) {
			patternRoutes.clear();
			patternRoutes = null;
		}
		if(fixedRoutes != null) {
			for(Map<?,?> map : fixedRoutes.values()) {
				map.clear();
			}
			fixedRoutes.clear();
			fixedRoutes = null;
		}
		if(routes != null) {
			routes.clear();
			routes = null;
		}
		if(namedClasses != null) {
			namedClasses.clear();
			namedClasses = null;
		}
		if(realms != null) {
			realms.clear();
			realms = null;
		}
		if(authentications != null) {
			for(Map<?,?> map : authentications.values()) {
				map.clear();
			}
			authentications.clear();
			authentications = null;
		}
		if(controllers != null) {
			controllers.clear();
			controllers = null;
		}
		if(published != null) {
			published.clear();
			published = null;
		}
	}
	
	/**
	 * Get an existing named route.
	 * @param name the name of the named route
	 * @return a {@link NamedRoute} object if the given name exists; null otherwise
	 */
	public NamedRoute get(String name) {
		validateName(name);
		if(routes.containsKey(name)) {
			return new NamedRoute(this, name);
		}
		return null;
	}

	private Class<? extends Controller> getControllerClass(Class<?> clazz) {
		if(Model.class.isAssignableFrom(clazz)) {
			if(controllers != null) {
				Pattern p = Pattern.compile("^.*[\\.\\$]" + clazz.getSimpleName() + "Controller$");
				for(String className : controllers.keySet()) {
					Matcher m = p.matcher(className);
					if(m.matches()) {
						try {
							return Class.forName(className, true, controllers.get(className)).asSubclass(Controller.class);
						} catch(ClassNotFoundException e) {
							// discard and look for the next one
						}
					}
				}
			}
		} else if(Controller.class.isAssignableFrom(clazz)) {
			if((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
				throw new UnsupportedOperationException(clazz.getCanonicalName() + " cannot be routed because it is an abstract class");
			}
			return clazz.asSubclass(Controller.class);
		}

		throw new IllegalArgumentException("could not locate controller class for " + clazz.getSimpleName());
	}
	
	public RouteHandler getHandler(HttpRequest request) {
		if(port != request.getPort()) {
			return null;
		}
		
		if(!hasHost(request.getHost())) {
			return null;
		}
		
		if(authentications != null) {
			Map<String, Realm> auths = authentications.get(request.getType());
			if(auths != null) {
				Realm realm = auths.get(request.getFullPath());
				if(realm == null) {
					realm = auths.get(request.getPath());
				}
				if(realm != null) {
					if(!isAuthorized(request, realm)) {
						return new AuthorizationHandler(realm.name());
					}
				}
			}
		}
		
		if(fixedRoutes != null) {
			Map<String, Route> fixedRoutes = this.fixedRoutes.get(request.getType());
			if(fixedRoutes != null) {
				Route fixedRoute = fixedRoutes.get(request.getFullPath());
				if(fixedRoute == null) {
					fixedRoute = fixedRoutes.get(request.getPath());
				}
				if(fixedRoute != null) {
					switch(fixedRoute.type) {
					case Route.ASSET:
						AssetRoute ar = (AssetRoute) fixedRoute;
						return new AssetHandler(ar.loader(), ar.length, ar.lastModified);
					case Route.AUTHORIZATION:
						throw new UnsupportedOperationException();
					case Route.CONTROLLER:
						ControllerRoute cr = (ControllerRoute) fixedRoute;
						return new ControllerHandler(cr.controller, cr.controllerClass, cr.action, cr.params);
					case Route.DYNAMIC_ASSET:
						DynamicAssetRoute dar = (DynamicAssetRoute) fixedRoute;
						return new DynamicAssetHandler(dar.assetClass, dar.params);
					case Route.VIEW:
						ViewRoute vr = (ViewRoute) fixedRoute;
						return new ViewHandler(vr.viewClass, vr.params);
					default:
						throw new IllegalStateException();
					}
				}
			}
		}
		
		if(patternRoutes != null) {
			for(Route route : patternRoutes) {
				if(route.requestType == request.getType()) {
					Matcher matcher = route.matcher(route.matchOnFullPath ? request.getFullPath() : request.getPath());
					if(matcher.matches()) {
						switch(route.type) {
						case Route.ASSET:
						case Route.AUTHORIZATION:
							throw new UnsupportedOperationException();
						case Route.CONTROLLER:
							ControllerRoute cr = (ControllerRoute) route;
							String[][] params;
							if(cr.params == null) {
								params = null;
							} else {
								params = new String[cr.params.length][];
								for(int i = 0; i < params.length; i++) {
									params[i] = new String[] { cr.params[i][0], cr.params[i][1] };
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
							return new ControllerHandler(cr.controller, cr.controllerClass, cr.action, params);
						case Route.VIEW:
							ViewRoute vr = (ViewRoute) route;
							return new ViewHandler(vr.viewClass, vr.params);
						default:
							throw new IllegalStateException("unknown route type: " + route.type);
						}
					}
				}
			}
		}
		
		return null;
	}

	public String getHost() {
		return hosts[0];
	}
	
	public String[] getHosts() {
		return hosts;
	}
	
	String getKey(Class<? extends Model> parent, String field, Action action) {
		return parent.getName() + ":" + field + ":" + action;
	}

	String getKey(Class<?> clazz, Action action) {
		if(Model.class.isAssignableFrom(clazz)) {
			return controllerCanonicalName(clazz.getName()) + ":" + action;
		} else {
			return clazz.getName() + ":" + action;
		}
	}
	
	public List<String> getPaths() {
		List<String> paths = new ArrayList<String>();
		if(fixedRoutes != null) {
			for(Map<String, Route> map : fixedRoutes.values()) {
				paths.addAll(map.keySet());
			}
		}
		if(patternRoutes != null) {
			for(Route route : patternRoutes) {
				paths.add(route.rule);
			}
		}
		Collections.sort(paths);
		return paths;
	}
	
	public List<String> getPaths(Type type) {
		List<String> paths = new ArrayList<String>();
		if(fixedRoutes != null) {
			Map<String, Route> map = fixedRoutes.get(type);
			if(map != null) {
				paths.addAll(map.keySet());
			}
		}
		if(patternRoutes != null) {
			for(Route route : patternRoutes) {
				if(route.requestType == type) {
					paths.add(route.rule);
				}
			}
		}
		Collections.sort(paths);
		return paths;
	}

	public int getPort() {
		return port;
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
	
	public List<Route> getRoutes(Type type) {
		List<Route> routes = new ArrayList<Route>();
		if(fixedRoutes != null) {
			Map<String, Route> map = fixedRoutes.get(type);
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
				if(route.requestType == type) {
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
	
	private boolean hasHost(String host) {
		if(host != null && host.length() > 0) {
			for(String h : hosts) {
				if(h.equals(host)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAuthorized(HttpRequest request, Realm realm) {
		String header = request.getHeader(Header.AUTHORIZATION);
		if(header != null && header.startsWith("Basic ")) {
			String[] sa = new String(Base64.decode(header.substring(6).getBytes())).split(":");
			if(sa.length == 2) {
				return realm.isAuthorized(sa[0], sa[1]);
			}
		}
		return false;
	}

	public boolean isAuthorized(HttpRequest request, String username, String password) {
		if(username == null) {
			return false;
		}
		String header = request.getHeader(Header.AUTHORIZATION);
		if(header != null && header.startsWith("Basic ")) {
			String[] sa = new String(Base64.decode(header.substring(6).getBytes())).split(":");
			if(sa.length == 1 || sa[1] == null) {
				return username.equals(sa[0]) && password == null;
			}
			if(sa.length == 2) {
				return username.equals(sa[0]) && password.equals(sa[1]);
			}
		}
		return false;
	}

	private String[] parseRules(String path, Class<? extends Model> clazz) {
		char[] ca = path.toCharArray();

		boolean models = false;
		int[] id = null;
		String idRule = null;
		
		int pix = find(ca, '?');
		if(pix == 0) {
			path = "{models}/{id}" + path;
			models = true;
			id = new int[] { 9, 12 };
			pix = 13;
		} else {
			if(pix == -1) {
				pix = ca.length;
			}
			int s1 = find(ca, '{');
			while(s1 != -1 && s1 < pix) {
				int s2 = closer(ca, s1);
				if(s2 == -1) {
					throw new RoutingException("missing closer for variable starting at " + s1 + " in: " + path);
				}
				int ix = findAny(ca, s1, s2, '=', ':');
				if(ix == -1) ix = s2;
				if(isEqual(ca, s1+1, ix, 'm','o','d','e','l','s')) {
					models = true;
					if(id != null) break;
				} else if(isEqual(ca, s1+1, ix, 'i','d')) {
					id = new int[] { s1, s2 };
					if(ix < s2 && ca[ix] == '=') {
						idRule = new String(ca, ix+1, s2-ix-1).trim();
					}
					if(models) break;
				}
				s1 = find(ca, '{', s2 + 1);
			}
		}
		
		if(models && id != null) {
			String classRules = (path.substring(0, id[0]) + path.substring(id[1]+1)).replaceAll("//", "/").replaceAll("/\\?", "?");
			if(classRules.endsWith("/")) {
				classRules = classRules.substring(0, classRules.length()-1);
			}

			String modelRules = path;
			if(idRule != null) {
				modelRules = path.substring(0, id[0]+1) + idRule + path.substring(id[1]);
			}

			String[] sa = new String[7];
			sa[create.ordinal()] =   classRules;
			sa[destroy.ordinal()] =  modelRules;
			sa[update.ordinal()] =   modelRules;
			sa[show.ordinal()] =     modelRules;
			sa[showAll.ordinal()] =  classRules;
			sa[showEdit.ordinal()] = modelRules.contains("?") ? modelRules.replaceFirst("\\?", "/edit?") : modelRules + "/edit";
			sa[showNew.ordinal()] =  classRules.contains("?") ? classRules.replaceFirst("\\?", "/new?") : classRules + "/new";
			
			return sa;
		} else {
			if(!models && id == null) {
				throw new RoutingException("{model} and {id} fields missing in model path: " + path);
			}
			if(!models) {
				throw new RoutingException("{model} field missing in model path: " + path);
			} {
				throw new RoutingException("{id} field missing in model path: " + path);
			}
		}
	}

	private String pathError(Object obj1, String field) {
		if(obj1 == null) {
			logger.warn(new RoutingException("cannot find a path to a null object"));
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
		return pathTo(modelClass, Action.showAll);
	}
	
	@Override
	public String pathTo(Class<? extends Model> modelClass, Action action) {
		if(modelClass != null) {
			Route[] routes = this.routes.get(getKey(modelClass, action));
			if(routes != null) {
				IllegalArgumentException exception = null;
				for(Route route : routes) {
					if(routes != null) {
						try {
							return pathToClass(route.rule, modelClass);
						} catch(IllegalArgumentException e) {
							exception = e; // rule may have variables, which can't be handled without a model object
						}
					}
				}
				if(exception != null) {
					return pathError(exception, null);
				}
			}
		}
		return pathError(modelClass, null);
	}
	
	@Override
	public String pathTo(Model model) {
		return pathTo(model, Action.show);
	}
	
	@Override
	public String pathTo(Model model, Action action) {
		if(model != null) {
			Route[] routes = this.routes.get(getKey(model.getClass(), action));
			if(routes != null) {
				return pathToModel(routes[0].rule, model);
			}
		}
		return pathError(model, null);
	}

	@Override
	public String pathTo(Model parent, String field) {
		if(parent != null && field != null) {
			Action action = getAdapter(parent).hasMany(field) ? showAll : show;
			Route[] routes = this.routes.get(getKey(parent.getClass(), field, action));
			if(routes != null) {
				return pathToHasMany(routes[0].rule, parent, field);
			}
		}
		return pathError(parent, field);
	}
	
	@Override
	public String pathTo(Model parent, String field, Action action) {
		if(parent != null && field != null) {
			Route[] routes = this.routes.get(getKey(parent.getClass(), field, action));
			if(routes != null) {
				return pathToHasMany(routes[0].rule, parent, field);
			}
		}
		return pathError(parent, field);
	}
	
	@Override
	public String pathTo(String routeName) {
		return pathTo(routeName, new Object[0]);
	}
	
	public String pathTo(String routeName, Model model) {
		if(!blank(routeName)) {
			Route[] routes = this.routes.get(routeName);
			if(routes != null) {
				return pathToModel(routes[0].rule, model);
			}
		}
		return pathError(routeName, null);
	}

	@Override
	public String pathTo(String routeName, Object...params) {
		if(params.length == 1 && params[0] instanceof Model) {
			return pathTo(routeName, (Model) params[0]);
		}
		
		if(!blank(routeName)) {
			Route[] routes = this.routes.get(routeName);
			if(routes != null) {
				IllegalArgumentException exception = null;
				for(Route route : routes) {
					if(route.isFixed()) {
						return route.path;
					} else {
						Class<?> clazz = (namedClasses != null) ? namedClasses.get(routeName) : null;
						try {
							return pathToClass(route.rule, clazz, params);
						} catch(IllegalArgumentException e) {
							exception = e; // pathToNew may have variables, which are acceptable if the model is passed in
						}
					}
					if(exception != null) {
						return pathError(exception, null);
					}
				}
			}
		}
		return pathError(routeName, null);
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
	
	void publish(Route route) {
		if(published == null) {
			published = new LinkedHashSet<Route>();
		}
		published.add(route);
	}
	
	/**
	 * Remove a named route and all routes that had been added to it.
	 * @param name the named route to remove
	 * @throws IllegalArgumentException if the given name is not valid for named routes
	 * @see #add(String)
	 */
	public void remove(String name) {
		validateName(name);
		if(routes != null) {
			if(namedClasses != null) {
				namedClasses.remove(name);
			}
			Route[] ra = routes.remove(name);
			if(ra != null) {
				for(Route route : ra) {
					removeRoute(route);
				}
			}
		}
	}

	public void removeAssetRoutes(AssetProvider provider) {
		List<String> paths = toStringList(getResourceAsString(provider.getClass(), "assets.js"));
		if(!blank(paths)) {
			for(String path : paths) {
				String[] sa = path.split(":", 2);
				if(sa.length == 2) {
					removeBasicAuthentication(Type.GET, sa[0]);
				}
			}
			if(fixedRoutes != null) {
				Map<String, Route> map = fixedRoutes.get(GET);
				if(map != null) {
					for(Iterator<Route> rIter = map.values().iterator(); rIter.hasNext(); ) {
						Route route = rIter.next();
						if(route instanceof AssetRoute && ((AssetRoute) route).provider == provider) {
							rIter.remove();
						}
					}
				}
				if(map.isEmpty()) {
					fixedRoutes.remove(GET);
				}
				if(fixedRoutes.isEmpty()) {
					fixedRoutes = null;
				}
			}
			if(patternRoutes != null) {
				for(Iterator<Route> iter = patternRoutes.iterator(); iter.hasNext(); ) {
					Route route = iter.next();
					if(route instanceof AssetRoute && ((AssetRoute) route).provider == provider) {
						iter.remove();
					}
				}
				if(patternRoutes.isEmpty()) {
					patternRoutes = null;
				}
			}
		}
	}

	public void removeBasicAuthentication(Type requestType, String path) {
		if(authentications != null) {
			Map<String, Realm> auth = authentications.get(requestType);
			if(auth != null) {
				auth.remove(path);
				if(auth.isEmpty()) {
					authentications.remove(requestType);
				}
			}
			if(authentications.isEmpty()) {
				authentications = null;
			}
		}
	}

	public void removeBasicAuthorization(String realm, String username) {
		if(realms != null) {
			Realm r = realms.get(realm);
			if(r != null) {
				r.remove(username);
				if(r.isEmpty()) {
					realms.remove(realm);
				}
			}
			if(realms.isEmpty()) {
				realms = null;
			}
		}
	}
	
	public void removeControllerNames(Collection<String> controllerNames) {
		if(controllers != null) {
			for(String name : controllerNames) {
				controllers.remove(name);
			}
		}
	}
	
	private void removeFromHasMany(String parentKey) {
		if(hasMany != null) {
			String key = hasMany.remove(parentKey);
			if(key != null) {
				Route[] ra = routes.remove(key);
				if(ra != null) {
					for(Route route : ra) {
						removeRoute(route);
					}
				}
			}
			if(hasMany.isEmpty()) {
				hasMany = null;
			}
		}
	}

	private boolean removeFromRoutes(String key, Route route) {
		if(routes != null) {
			Route[] ra = routes.get(key);
			if(ra != null) {
				if(ra.length == 1) {
					if(ra[0].equals(route)) {
						routes.remove(key);
						return true;
					}
				} else { // duplicates should have been filter during the addRule method
					for(int i = 0; i < ra.length; i++) {
						if(ra[i].equals(route)) {
							Route[] tmp = new Route[ra.length-1];
							System.arraycopy(ra, 0, tmp, 0, i);
							System.arraycopy(ra, i+1, tmp, i, tmp.length-i);
							routes.put(key, tmp);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public void removeRoute(Class<? extends Model> clazz, Action action) {
		removeRoute(null, clazz, action);
	}

	private void removeRoute(Route route) {
		if(route.isFixed()) {
			if(fixedRoutes != null) {
				Map<String, Route> routes = fixedRoutes.get(route.requestType);
				if(routes != null) {
					routes.remove(route.path);
					if(routes.isEmpty()) {
						fixedRoutes.remove(routes);
					}
				}
				if(fixedRoutes.isEmpty()) {
					fixedRoutes = null;
				}
			}
		} else {
			if(patternRoutes != null) {
				patternRoutes.remove(route);
				if(patternRoutes.isEmpty()) {
					patternRoutes = null;
				}
			}
		}
		if(published != null) {
			published.remove(route);
			if(published.isEmpty()) {
				published = null;
			}
		}
	}
	
	public void removeRoute(String rule, Class<? extends Model> clazz, Action action) {
		if(rule != null && rule.charAt(0) == '?') {
			rule = "/{models}/{id}" + rule;
		}
		switch(action) {
		case create:	removeRoute(getKey(clazz, action), (rule == null) ? "/{models}"			: rule, clazz, action); break;
		case update:	removeRoute(getKey(clazz, action), (rule == null) ? "/{models}/{id}"		: rule, clazz, action); break;
		case destroy:	removeRoute(getKey(clazz, action), (rule == null) ? "/{models}/{id}"		: rule, clazz, action); break;
		case show:		removeRoute(getKey(clazz, action), (rule == null) ? "/{models}/{id}"		: rule, clazz, action); break;
		case showAll:	removeRoute(getKey(clazz, action), (rule == null) ? "/{models}" 			: rule, clazz, action); break;
		case showEdit:	removeRoute(getKey(clazz, action), (rule == null) ? "/{models}/{id}/edit" 	: rule, clazz, action); break;
		case showNew:	removeRoute(getKey(clazz, action), (rule == null) ? "/{models}/new" 		: rule, clazz, action); break;
		default:
			throw new IllegalArgumentException("unknown action: " + action);
		}
	}
	
	private void removeRoute(String key, Route route) {
		if(removeFromRoutes(key, route)) {
			if(namedClasses != null) {
				namedClasses.remove(key);
			}

			removeRoute(route);
			removeFromHasMany(key);
		}
	}
	
	ControllerRoute removeRoute(String key, String path, Class<?> clazz, Action action) {
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		Type type;
		switch(action) {
		case create:	type = POST;	break;
		case update:	type = PUT;		break;
		case destroy:	type = DELETE;	break;
		case show:		type = GET;		break;
		case showAll:	type = GET;		break;
		case showEdit:	type = GET;		break;
		case showNew:	type = GET;		break;
		default:
			throw new IllegalArgumentException("unknown action: " + action);
		}
		Class<? extends Model> modelClass = Model.class.isAssignableFrom(clazz) ? clazz.asSubclass(Model.class) : null;
		Class<? extends Controller> controllerClass = getControllerClass(clazz);
		ControllerRoute route = new ControllerRoute(type, path, modelClass, controllerClass, action);
		removeRoute(key, route);
		return route;
	}

	public void removeRoutes(Class<? extends Model> clazz, Action...actions) {
		if(actions.length == 0) {
			actions = Action.values();
		}
		for(Action action : actions) {
			removeRoute(clazz, action);
		}
	}
	
	public void removeRoutes(String path, Class<? extends Model> clazz, Action...actions) {
		String[] parsedRules = parseRules(path, clazz);
		
		if(actions.length == 0) {
			actions = Action.values();
		}

		for(Action action : actions) {
			switch(action) {
			case create:	removeRoute(parsedRules[create.ordinal()],	clazz, action); break;
			case destroy:	removeRoute(parsedRules[destroy.ordinal()],	clazz, action); break;
			case update:	removeRoute(parsedRules[update.ordinal()], 	clazz, action); break;
			case show:		removeRoute(parsedRules[show.ordinal()],	clazz, action); break;
			case showAll:	removeRoute(parsedRules[showAll.ordinal()],	clazz, action); break;
			case showEdit:	removeRoute(parsedRules[showEdit.ordinal()],clazz, action); break;
			case showNew:	removeRoute(parsedRules[showNew.ordinal()],	clazz, action); break;
			default:
				throw new IllegalArgumentException("unknown action: " + action);
			}
		}
	}
	
	public void setDiscovery(String path) {
		setDiscovery(path, false);
	}
	
	public void setDiscovery(String path, boolean addDiscoveryHeader) {
		remove(DISCOVERY_DEFAULT);
		if(path != null) {
			add(DISCOVERY_DEFAULT).asRoute(path, new DiscoveryController(this));
		}
		this.discoveryHeader = addDiscoveryHeader ? pathTo(DISCOVERY_DEFAULT) : null;
	}
	
	public void setDiscovery(String path, String realm) {
		setDiscovery(path, false);
	}
	
	public void setDiscovery(String path, String realm, boolean addDiscoverHeader) {
		setDiscovery(path, discoveryHeader);
		addBasicAuthentication(path, realm);
	}
	
	public Routed setHome(Class<? extends View> clazz) {
		return setHome(clazz, (String) null);
	}
	
	public Routed setHome(Class<? extends View> clazz, String parameters) {
		String path = (parameters == null) ? "/" : ("/?" + parameters);
		Route route = addRoute(HOME, path, clazz);
		return new Routed(this, route);
	}

	public Routed setHome(Class<?> clazz, Action action) {
		return setHome(clazz, action, null);
	}

	public Routed setHome(Class<?> clazz, Action action, String parameters) {
		if(Model.class.isAssignableFrom(clazz) || Controller.class.isAssignableFrom(clazz)) {
			String path = (parameters == null) ? "/" : ("/?" + parameters);
			Route route = addRoute(HOME, path, clazz, action);
			return new Routed(this, route);
		} else {
			throw new IllegalArgumentException("invalid type: " + clazz.getSimpleName() + " (valid types are Model and Controller)");
		}
	}

	public Routed setHome(Controller controller) {
		Route route = addRoute(Type.GET, HOME, "/", controller);
		return new Routed(this, route);
	}

	@Override
	public String urlTo(Class<? extends Model> modelClass) {
		return urlTo(modelClass, Action.showAll);
	}
	
	@Override
	public String urlTo(Class<? extends Model> modelClass, Action action) {
		return asUrl(pathTo(modelClass, action));
	}

	@Override
	public String urlTo(Model model) {
		return urlTo(model, Action.show);
	}

	@Override
	public String urlTo(Model model, Action action) {
		return asUrl(pathTo(model, action));
	}

	@Override
	public String urlTo(Model parent, String field) {
		return asUrl(pathTo(parent, field));
	}

	@Override
	public String urlTo(Model parent, String field, Action action) {
		return asUrl(pathTo(parent, field, action));
	}

	@Override
	public String urlTo(String routeName) {
		return asUrl(pathTo(routeName));
	}

	@Override
	public String urlTo(String routeName, Model model) {
		return asUrl(pathTo(routeName, model));
	}

	@Override
	public String urlTo(String routeName, Object... params) {
		return asUrl(pathTo(routeName, params));
	}
	
	private void validateName(String name) {
		for(int i = 0; i < name.length(); i++) {
			if(!Character.isLetterOrDigit(name.charAt(i)) && name.charAt(i) != '_') {
				throw new IllegalArgumentException("name can consist only of letters, digits, and underscores");
			}
		}
	}

	private void validateNoArgsCtor(Class<?> clazz) {
		for(Constructor<?> ctor : clazz.getConstructors()) {
			if(ctor.getParameterTypes().length == 0) {
				return;
			}
		}
		throw new IllegalArgumentException("\"" + clazz.getSimpleName() + "\" must implement a no-args constructor to be routing directly");
	}

}
