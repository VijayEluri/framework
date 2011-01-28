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
import static org.oobium.http.constants.RequestType.DELETE;
import static org.oobium.http.constants.RequestType.GET;
import static org.oobium.http.constants.RequestType.POST;
import static org.oobium.http.constants.RequestType.PUT;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findAny;
import static org.oobium.utils.CharStreamUtils.isEqual;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.camelCase;
import static org.oobium.utils.StringUtils.controllerCanonicalName;
import static org.oobium.utils.StringUtils.getResourceAsString;
import static org.oobium.utils.StringUtils.underscored;
import static org.oobium.utils.json.JsonUtils.toStringList;

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
import java.util.TreeMap;

import org.oobium.app.AssetProvider;
import org.oobium.app.ModuleService;
import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.routing.routes.AssetRoute;
import org.oobium.app.server.routing.routes.ControllerRoute;
import org.oobium.app.server.routing.routes.DynamicAssetRoute;
import org.oobium.app.server.routing.routes.RedirectRoute;
import org.oobium.app.server.routing.routes.ViewRoute;
import org.oobium.app.server.view.DynamicAsset;
import org.oobium.app.server.view.View;
import org.oobium.http.HttpRequest;
import org.oobium.http.constants.ContentType;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.logging.ILogger;
import org.oobium.persist.Model;
import org.oobium.utils.Base64;

public class Router {

	public static final String UNKNOWN_PATH = "/#";
	public static final String HOME = "Home";

	static void checkClass(Class<?> clazz) {
		int modifiers = clazz.getModifiers();
		if(Modifier.isAbstract(modifiers)) {
			throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed because it is an abstract class");
		}
		if(Modifier.isPrivate(modifiers)) {
			throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed because it is a private class");
		}
		if(clazz != DiscoveryController.class) {
			try {
				clazz.getConstructor();
			} catch(SecurityException e) {
				throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed", e);
			} catch(NoSuchMethodException e) {
				throw new IllegalArgumentException(clazz.getCanonicalName() + " cannot be routed because it does not have a no-args constructor");
			}
		}
	}

	private static void checkName(String name) {
		for(int i = 0; i < name.length(); i++) {
			if(!Character.isLetterOrDigit(name.charAt(i)) && name.charAt(i) != '_') {
				throw new IllegalArgumentException("name can consist only of letters, digits, and underscores");
			}
		}
	}

	
	protected final ModuleService service;
	protected final ILogger logger;

	protected Map<String, Route[]> routes;
	protected List<Route> patternRoutes;
	protected Map<RequestType, Map<String, Route>> fixedRoutes;
	protected Map<String, Class<?>> namedClasses;
	protected Map<String, String> hasMany;

	protected Map<RequestType, Map<String, Realm>> authentications;
	private Map<String, Realm> realms;
	
	Set<Route> published;
	
	public Router(ModuleService service) {
		this.service = service;
		this.logger = service.getLogger();
	}

	/**
	 * Create a new named route.
	 * @param name the name of the new route
	 * @return a {@link NamedRoute} object, to which actual routes can be added
	 * @throws IllegalArgumentException if the given name is not valid for named routes
	 */
	public NamedRoute add(String name) {
		checkName(name);
		return new NamedRoute(this, name);
	}

	public Routed addAsset(Class<? extends DynamicAsset> clazz) {
		checkClass(clazz);
		String name = getAssetName(clazz);
		Route route = new DynamicAssetRoute(GET, name, clazz);
		addRoute(name, route);
		return new Routed(this, route);
	}
	
	public Routed addAssetRoutes() {
		List<String> paths = service.getAssetList();
		List<Route> routes = new ArrayList<Route>();
		if(!blank(paths)) {
			for(String path : paths) {
				String[] sa = path.split("\\|", 4);
				String key = service.getClass().getCanonicalName() + ":" + sa[0];
				
				String assetPath = sa[0];
				if(path.startsWith("/images/")) {
					sa[0] = sa[0].substring(7);
				} else if(path.startsWith("/scripts/")) {
					sa[0] = sa[0].substring(8);
				} else if(path.startsWith("/styles/")) {
					sa[0] = sa[0].substring(7);
				}

				int ix = assetPath.lastIndexOf('.');
				String ext = (ix != -1) ? assetPath.substring(ix+1) : null;

				ContentType type = ContentType.getFromExtension(ext, ContentType.HTML);

				Route route = new AssetRoute(checkRule(sa[0]), assetPath, type, sa[1], sa[2]);
				routes.add(route);
				addRoute(key, route);
				if(sa.length == 4) {
					addBasicAuthentication(RequestType.GET, sa[0], sa[3]);
				}
			}
			return new Routed(this, routes.toArray(new Route[routes.size()]));
		}
		return new Routed(this);
	}

	public void addBasicAuthentication(String path, String realm) {
		addBasicAuthentication(GET, path, realm);
	}
	
	public void addBasicAuthentication(RequestType requestType, String path, String realm) {
		if(realms == null) {
			realms = new HashMap<String, Realm>();
		}
		Realm r = realms.get(realm);
		if(r == null) {
			r = new Realm(realm);
			realms.put(realm, r);
		}
		if(authentications == null) {
			authentications = new HashMap<RequestType, Map<String,Realm>>();
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
	
	public void addRedirect(String from, String to) {
		from = checkRule(from);
		to = checkRule(to);
		Route route = new RedirectRoute(from, to);
		addRoute(getName(GET, from), route);
	}
	
	/**
	 * Add a single resource route to this router for the given model class and action.
	 * <p>Refer to the {@link Action} class for details on how the given action denotes the path and request type.</p>
	 * @param clazz the model class to use as a resource route
	 * @param action the action that this resource route is for
	 * @return a Routed object
	 * @see #addResources(Class)
	 * @see Action
	 */
	public Routed addResource(Class<? extends Model> clazz, Action action) {
		return addResource(null, clazz, action);
	}

	private String getResourceRule(String path, Action action) {
		if(path == null || path.length() == 0) {
			switch(action) {
			case create:	return "/{models}";
			case update:	return "/{models}/{id}";
			case destroy:	return "/{models}/{id}";
			case show:		return "/{models}/{id}";
			case showAll:	return "/{models}";
			case showEdit:	return "/{models}/{id}/edit";
			case showNew:	return "/{models}/new";
			default:		throw new IllegalArgumentException("unknown action: " + action);
			}
		} else if(path.charAt(0) == '?') {
			switch(action) {
			case create:	return "/{models}" + path;
			case update:	return "/{models}/{id}" + path;
			case destroy:	return "/{models}/{id}" + path;
			case show:		return "/{models}/{id}" + path;
			case showAll:	return "/{models}" + path;
			case showEdit:	return "/{models}/{id}/edit" + path;
			case showNew:	return "/{models}/new" + path;
			default:		throw new IllegalArgumentException("unknown action: " + action);
			}
		}
		return path;
	}
	
	/**
	 * Add a single resource route to this router for the given model class and action.
	 * Unlike {@link #addResource(Class, Action)}, this method allows for overriding the conventional path.
	 * <p>Refer to the {@link Action} class for details on how the given action denotes the path and request type.</p>
	 * @param clazz the model class to use as a resource route
	 * @param action the action that this resource route is for
	 * @return a Routed object
	 * @see Action
	 */
	public Routed addResource(String path, Class<? extends Model> clazz, Action action) {
		String rule = getResourceRule(path, action);
		Route route = addRoute(getKey(clazz, action), rule, clazz, action);
		return new Routed(this, route);
	}
	
	/**
	 * <p>Add all resource routes for the given model.</p>
	 * <table>
	 *   <tr align="left"><th>Verb</th><th>Path</th><th>Action</th><th>Purpose</th></tr>
	 *   <tr><td>POST</td><td>/{models}</td><td>{@link #create}</td><td>create a new model</td></tr>
	 *   <tr><td>PUT</td><td>/{models}/{id}</td><td>{@link #update}</td><td>update a specific model</td></tr>
	 *   <tr><td>DELETE</td><td>/{models}/{id}</td><td>{@link #destroy}</td><td>destroy a specific model</td></tr>
	 *   <tr><td>GET</td><td>/{models}/{id}</td><td>{@link #show}</td><td>show a specific model</td></tr>
	 *   <tr><td>GET</td><td>/{models}</td><td>{@link #showAll}</td><td>show all models</td></tr>
	 *   <tr><td>GET</td><td>/{models}/{id}/edit</td><td>{@link #showEdit}</td><td>return an HTML form to edit a specific model</td></tr>
	 *   <tr><td>GET</td><td>/{models}/new</td><td>{@link #showNew}</td><td>return an HTML form to create a new model</td></tr>
	 * </table>
	 * <p>{models} refers to the plural of the given model name.<br/>
	 * For example, if the given model class was
	 * Post.class then the showAll path would be: "/posts" and it purpose would be to show all models of type Post.</p>
	 * @param clazz the model class for which to add resource routes
	 * @return a Routes object
	 * @see Action
	 */
	public Routes addResources(Class<? extends Model> clazz) {
		return addResources(clazz, new Action[0]);
	}

	/**
	 * <p>Add resource routes for the given model and the given array of specific actions.</p>
	 * @param clazz the model class for which to add resource routes
	 * @param actions the specific actions for which to add resource routes
	 * @return a Routes object
	 * @see #addResources(Class)
	 * @see Action
	 */
	public Routes addResources(Class<? extends Model> clazz, Action...actions) {
		if(actions.length == 0) {
			actions = Action.values();
		}
		List<Routed> routed = new ArrayList<Routed>();
		for(Action action : actions) {
			routed.add(addResource(clazz, action));
		}
		return new Routes(this, routed, clazz, "/{models}/{id}");
	}
	
	/**
	 * Add all resource routes for the given model.
	 * Unlike {@link #addResources(Class)}, this method allows for overriding the conventional path.
	 * The given path may contain any number of variables ("{name:regex}") and constants ("{name=value}"), 
	 * and they may be in any order, however it <b>must</b> contain the models variable ("{models}") and, 
	 * for the model specific routes (update, destroy, show, showEdit), must also contain the id variable ("{id}").
	 * @param the custom resource path
	 * @param clazz the model class to use as a resource route
	 * @return a Routed object
	 * @see Action
	 */
	public Routes addResources(String path, Class<? extends Model> clazz) {
		return addResources(path, clazz, new Action[0]);
	}
	
	/**
	 * Add resource routes for the given model and the given array of specific actions.
	 * Unlike {@link #addResources(Class, Action...)}, this method allows for overriding the conventional path.
	 * The given path may contain any number of variables ("{name:regex}") and constants ("{name=value}"), 
	 * and they may be in any order, however it <b>must</b> contain the models variable ("{models}") and, 
	 * for the model specific routes (update, destroy, show, showEdit), must also contain the id variable ("{id}").
	 * @param the custom resource path
	 * @param clazz the model class to use as a resource route
	 * @param actions the specific actions for which to add resource routes
	 * @return a Routed object
	 * @see Action
	 */
	public Routes addResources(String path, Class<? extends Model> clazz, Action...actions) {
		String[] parsedRules = parseRules(path, clazz);
		
		if(actions.length == 0) {
			actions = Action.values();
		}

		List<Routed> routed = new ArrayList<Routed>();
		for(Action action : actions) {
			Routed r = null;
			switch(action) {
			case create:	r = addResource(parsedRules[create.ordinal()],	clazz, action); break;
			case destroy:	r = addResource(parsedRules[destroy.ordinal()],	clazz, action); break;
			case update:	r = addResource(parsedRules[update.ordinal()], 	clazz, action); break;
			case show:		r = addResource(parsedRules[show.ordinal()],	clazz, action); break;
			case showAll:	r = addResource(parsedRules[showAll.ordinal()],	clazz, action); break;
			case showEdit:	r = addResource(parsedRules[showEdit.ordinal()],clazz, action); break;
			case showNew:	r = addResource(parsedRules[showNew.ordinal()],	clazz, action); break;
			default:
				throw new IllegalArgumentException("unknown action: " + action);
			}
			routed.add(r);
		}
		return new Routes(this, routed, clazz, path);
	}

	/**
	 * Add a route to be handled by the given controller's handleRequest method in response to a
	 * GET request for the given path. The given path may contain variables and constants.
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed addRoute(String path, Class<? extends Controller> clazz) {
		return add(getName(GET, path)).asRoute(GET, path, clazz);
	}
	
	/**
	 * Add a route to be handled by the given controller in response to a
	 * request for the given path. The request type and controller method are determined by the
	 * given action. The given path may contain variables and constants.
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @param action the specific action for which to add the route
	 * @return a Routed object
	 * @see Action
	 */
	public Routed addRoute(String path, Class<? extends Controller> clazz, Action action) {
		return add(getName(action, path)).asRoute(path, clazz, action);
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
				fixedRoutes = new HashMap<RequestType, Map<String,Route>>();
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
	
	Route addRoute(String key, String rule, Class<? extends Controller> clazz, RequestType requestType) {
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
		RequestType type;
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
	
	/**
	 * Add a route to be handled by the given controller's handleRequest method.
	 * @param requestType the type of the request this route will handle
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed addRoute(RequestType requestType, String path, Class<? extends Controller> clazz) {
		return add(getName(requestType, path)).asRoute(requestType, path, clazz);
	}

	/**
	 * Add a view route for the given view class. The new route will render the view in response to
	 * a GET request with a path of <code>varName(clazz)</code>.<br>
	 * For example: addView(Home.class) will render the Home view for a "GET /home" request.
	 * @param clazz the view to be rendered
	 * @return a Routed object
	 */
	public Routed addView(Class<? extends View> clazz) {
		return add("show" + clazz.getSimpleName()).asView(clazz);
	}
	
	/**
	 * Add a view route to render the given view class in response to a GET request with the given path.<br>
	 * @param path the path that this request will handle (may contain constants, but not variables)
	 * @param clazz the view to be rendered
	 * @return a Routed object
	 */
	public Routed addView(String path, Class<? extends View> clazz) {
		return add(getName(show, path)).asView(path, clazz);
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
		checkName(name);
		if(routes.containsKey(name)) {
			return new NamedRoute(this, name);
		}
		return null;
	}
	
	private String getAssetName(Class<? extends DynamicAsset> clazz) {
		String name = "/" + underscored(clazz.getCanonicalName());
		name = name.replace('.', '/') + "." + DynamicAsset.getFileExtension(clazz);
		return name;
	}

	private Class<? extends Controller> getControllerClass(Class<?> clazz) {
		Class<? extends Controller> controllerClass = null;
		if(Model.class.isAssignableFrom(clazz)) {
			controllerClass = service.getControllerClass(clazz.asSubclass(Model.class));
		} else if(Controller.class.isAssignableFrom(clazz)) {
			controllerClass = clazz.asSubclass(Controller.class);
		}

		if(controllerClass != null) {
			checkClass(controllerClass);
			return controllerClass;
		}
		
		throw new IllegalArgumentException("could not locate controller class for " + clazz.getSimpleName());
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

	private String getName(Action action, String path) {
		String name = getName(path);
		return action.name() + camelCase(name);
	}

	private String getName(String path) {
		String name = (path.charAt(0) == '/') ? path.substring(1) : path;
		name = name.replaceAll("\\{([^\\}^\\:^\\=]+)[\\:\\=]?[^\\}]*\\}", "$1").replace('/', '_');
		return name;
	}

	private String getName(RequestType type, String path) {
		String name = getName(path);
		return type.name().toLowerCase() + camelCase(name);
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
	
	public List<String> getPaths(RequestType type) {
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
	
	public Map<String, Map<String, Map<String, String>>> getModelRouteMap() {
		return getModelRouteMap(getRoutes());
	}
	
	public Map<String, Map<String, Map<String, String>>> getModelRouteMap(Collection<Route> routes) {
		Map<String, Map<String, Map<String, String>>> results = new TreeMap<String, Map<String, Map<String, String>>>();

		for(Route route : routes) {
			if(route instanceof ControllerRoute) {
				ControllerRoute cr = (ControllerRoute) route;
				Class<?> c = cr.modelClass;
				Action a = cr.action;
				if(c != null && a != null) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					map.put("method", route.requestType.name());
					if(route.isFixed()) {
						map.put("path", route.path);
						map.put("fixed", "true");
					} else {
						map.put("path", route.rule);
					}

					String name = c.getName();
					Map<String, Map<String, String>> model = results.get(name);
					if(model == null) {
						model = new TreeMap<String, Map<String, String>>();
						results.put(name, model);
					}
					model.put(a.name(), map);
				}
			}
		}
		
		return results;
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

	public List<Route> getRoutes(RequestType type) {
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
	
	public ModuleService getService() {
		return service;
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
		
		if(!models) {
			path = path + "/{models}";
		}
		if(id == null) {
			path = path + "/{id}";
			id = new int[] { path.length()-4, path.length()-1 };
		}
		
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
		checkName(name);
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
	
	public void removeAsset(Class<? extends DynamicAsset> clazz) {
		String name = getAssetName(clazz);
		Route route = new DynamicAssetRoute(GET, name, clazz);
		removeRoute(name, route);
	}

	public void removeAssetRoutes(AssetProvider provider) {
		List<String> paths = toStringList(getResourceAsString(provider.getClass(), "assets.js"));
		if(!blank(paths)) {
			for(String path : paths) {
				String[] sa = path.split(":", 2);
				if(sa.length == 2) {
					removeBasicAuthentication(RequestType.GET, sa[0]);
				}
			}
			if(fixedRoutes != null) {
				Map<String, Route> map = fixedRoutes.get(GET);
				if(map != null) {
					for(Iterator<Route> rIter = map.values().iterator(); rIter.hasNext(); ) {
						Route route = rIter.next();
						if(route instanceof AssetRoute) {
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
					if(route instanceof AssetRoute) {
						iter.remove();
					}
				}
				if(patternRoutes.isEmpty()) {
					patternRoutes = null;
				}
			}
		}
	}

	public void removeBasicAuthentication(RequestType requestType, String path) {
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

	public void removeRedirect(String from) {
		from = checkRule(from);
		removeRoute(from);
	}
	
	public void removeResource(Class<? extends Model> clazz, Action action) {
		removeResource(null, clazz, action);
	}

	public void removeResource(String path, Class<? extends Model> clazz, Action action) {
		String rule = getResourceRule(path, action);
		removeRoute(getKey(clazz, action), rule, clazz, action);
	}

	public void removeResources(Class<? extends Model> clazz) {
		removeResources(clazz, new Action[0]);
	}
	
	public void removeResources(Class<? extends Model> clazz, Action...actions) {
		if(actions.length == 0) {
			actions = Action.values();
		}
		for(Action action : actions) {
			removeResource(clazz, action);
		}
	}

	public void removeResources(String path, Class<? extends Model> clazz) {
		removeResources(path, clazz, new Action[0]);
	}
	
	public void removeResources(String path, Class<? extends Model> clazz, Action...actions) {
		String[] parsedRules = parseRules(path, clazz);
		
		if(actions.length == 0) {
			actions = Action.values();
		}

		for(Action action : actions) {
			switch(action) {
			case create:	removeResource(parsedRules[create.ordinal()],	clazz, action); break;
			case destroy:	removeResource(parsedRules[destroy.ordinal()],	clazz, action); break;
			case update:	removeResource(parsedRules[update.ordinal()], 	clazz, action); break;
			case show:		removeResource(parsedRules[show.ordinal()],	clazz, action); break;
			case showAll:	removeResource(parsedRules[showAll.ordinal()],	clazz, action); break;
			case showEdit:	removeResource(parsedRules[showEdit.ordinal()],clazz, action); break;
			case showNew:	removeResource(parsedRules[showNew.ordinal()],	clazz, action); break;
			default:
				throw new IllegalArgumentException("unknown action: " + action);
			}
		}
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
	
	public void removeRoute(String path) {
		remove(getName(GET, path));
	}
	
	public void removeRoute(String path, Action action) {
		remove(getName(action, path));
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
		RequestType type;
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
	
	public void removeRoute(RequestType requestType, String path) {
		remove(getName(requestType, path));
	}
	
	public void removeView(Class<? extends View> clazz) {
		remove("show" + clazz.getSimpleName());
	}
	
	public void removeView(String path) {
		remove(getName(show, path));
	}
	
	/**
	 * Set the home path ("/") of this router to render the given view class.
	 * @param clazz the view class to render
	 * @return a Routed object
	 */
	public Routed setHome(Class<? extends View> clazz) {
		return setHome(clazz, (String) null);
	}
	
	/**
	 * Set the home path ("/") of this router to render the given view class with
	 * the given parameters.
	 * @param clazz the view class to render
	 * @param parameters a String of parameters in path format ("?{name:value}")
	 * @return a Routed object
	 */
	public Routed setHome(Class<? extends View> clazz, String parameters) {
		String path = (parameters == null) ? "/" : ("/?" + parameters);
		Route route = addRoute(HOME, path, clazz);
		return new Routed(this, route);
	}

	/**
	 * Set the home path ("/") of this router to call the given action of the give
	 * model or controller class.
	 * @param clazz the model or controller class whose action method should be called
	 * @return a Routed object
	 */
	public Routed setHome(Class<?> clazz, Action action) {
		return setHome(clazz, action, null);
	}

	/**
	 * Set the home path ("/") of this router to call the given action of the give
	 * model or controller class, with the given parameters.
	 * @param clazz the model or controller class whose action method should be called
	 * @param parameters a String of parameters in path format ("?{name:value}")
	 * @return a Routed object
	 */
	public Routed setHome(Class<?> clazz, Action action, String parameters) {
		if(Model.class.isAssignableFrom(clazz) || Controller.class.isAssignableFrom(clazz)) {
			String path = (parameters == null) ? "/" : ("/?" + parameters);
			Route route = addRoute(HOME, path, clazz, action);
			return new Routed(this, route);
		} else {
			throw new IllegalArgumentException("invalid type: " + clazz.getSimpleName() + " (valid types are Model and Controller)");
		}
	}

}
