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

import static org.jboss.netty.handler.codec.http.HttpMethod.DELETE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpMethod.PUT;
import static org.oobium.app.http.Action.create;
import static org.oobium.app.http.Action.destroy;
import static org.oobium.app.http.Action.show;
import static org.oobium.app.http.Action.showAll;
import static org.oobium.app.http.Action.showEdit;
import static org.oobium.app.http.Action.showNew;
import static org.oobium.app.http.Action.update;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findAny;
import static org.oobium.utils.CharStreamUtils.isEqual;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.camelCase;
import static org.oobium.utils.StringUtils.controllerCanonicalName;
import static org.oobium.utils.StringUtils.underscored;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.ModuleService;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.controllers.RtspController;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.app.http.Action;
import org.oobium.app.http.MimeType;
import org.oobium.app.request.Request;
import org.oobium.app.routing.routes.DynamicAssetRoute;
import org.oobium.app.routing.routes.HasManyRoute;
import org.oobium.app.routing.routes.HttpRoute;
import org.oobium.app.routing.routes.RedirectRoute;
import org.oobium.app.routing.routes.RtspRoute;
import org.oobium.app.routing.routes.StaticRoute;
import org.oobium.app.routing.routes.ViewRoute;
import org.oobium.app.routing.routes.WebsocketRoute;
import org.oobium.app.server.Websocket;
import org.oobium.app.views.DynamicAsset;
import org.oobium.app.views.View;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.utils.Base64;
import org.oobium.utils.json.JsonUtils;

public class Router {

	public static final String MODEL_NOTIFY_GROUP = "model-notifications";
	public static final String DEFAULT_MODEL_NOTIFY_PATH = "model_notifications";
	
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
		if(clazz != ApiController.class) {
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
				throw new IllegalArgumentException("name can consist only of letters, digits, and underscores: " + name);
			}
		}
	}

	
	protected final ModuleService service;
	protected final Logger logger;

	protected Map<String, Route[]> routes;
	protected List<Route> patternRoutes;
	protected Map<HttpMethod, Map<String, Route>> fixedRoutes;
	protected Map<String, Class<?>> namedClasses;
	protected Map<String, String> hasMany;
	
	protected Map<String, Set<Websocket>> websocketsByGroup;
	protected Map<String, Websocket> websocketsById;

	protected Map<HttpMethod, Map<String, Realm>> authentications;
	protected Map<String, Realm> realms;
	
	String modelNotificationPath;
	
	boolean autoPublish;
	Set<Route> published;
	
	public Router(ModuleService service) {
		this.service = service;
		this.logger = service.getLogger();
		this.autoPublish = true;
		addModelNotifier();
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
	
	public void addFromJson(String json) {
		for(Object o : JsonUtils.toList(json)) {
			try {
				addFromJson(o);
			} catch(Exception e) {
				if(logger.isLoggingDebug()) {
					logger.error(e);
				} else {
					logger.error(e.getMessage());
				}
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addFromJson(Object o) throws ClassNotFoundException {
		Map m = (Map) o;
		
		// Resource Route
		if(m.containsKey("model")) {
			String path = (String) m.get("path");
			Class<? extends Model> clazz = (Class<? extends Model>) Class.forName((String) m.get("model"));
			String s = (String) m.get("action");
			if(s == null) {
				s = (String) m.get("actions");
				if(s == null) {
					addResources(path, clazz);
				} else {
					for(String action : s.split("\\s*,\\s*")) {
						addResource(path, clazz, Action.valueOf(action));
					}
				}
			} else {
				addResource(path, clazz, Action.valueOf(s));
			}
		}
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

				MimeType type = MimeType.getFromExtension(ext, MimeType.HTML);

				Route route = new StaticRoute(checkRule(sa[0]), assetPath, type, sa[1], sa[2]);
				routes.add(route);
				addRoute(key, route);
				if(sa.length == 4) {
					addBasicAuthentication(HttpMethod.GET, sa[0], sa[3]);
				}
			}
			return new Routed(this, routes.toArray(new Route[routes.size()]));
		}
		return new Routed(this);
	}

	public void addBasicAuthentication(HttpMethod method, String path, String realm) {
		if(realms == null) {
			realms = new HashMap<String, Realm>();
		}
		Realm r = realms.get(realm);
		if(r == null) {
			r = new Realm(realm);
			realms.put(realm, r);
		}
		if(authentications == null) {
			authentications = new HashMap<HttpMethod, Map<String,Realm>>();
		}
		Map<String, Realm> auth = authentications.get(method);
		if(auth == null) {
			auth = new HashMap<String, Realm>();
			authentications.put(method, auth);
		}
		auth.put(path, r);
	}
	
	public void addBasicAuthentication(String path, String realm) {
		addBasicAuthentication(GET, path, realm);
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
	
	/**
	 * An alias for {@link #addBasicAuthorization(String, String, String)}
	 */
	public void addRealm(String realm, String username, String password) {
		addBasicAuthorization(realm, username, password);
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
	 * <p>Note that this method is not like {@link #addResource(String, Class, Action)} in that the path here
	 * <b>must</b> contain certain variables for certain actions, and they will be added if left out.
	 * The {@link #addResource(String, Class, Action)} method, on the other hand, will leave the exact given
	 * path untouched - use multiple calls to it if that is what is truly desired.</p>
	 * @param the custom resource path
	 * @param clazz the model class to use as a resource route
	 * @return a Routed object
	 * @see Action
	 * @see #addResource(String, Class, Action)
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
	 * <p>Note that this method is not like {@link #addResource(String, Class, Action)} in that the path here
	 * <b>must</b> contain certain variables for certain actions, and they will be added if left out.
	 * The {@link #addResource(String, Class, Action)} method, on the other hand, will leave the exact given
	 * path untouched - use multiple calls to it if that is what is truly desired.</p>
	 * @param the custom resource path
	 * @param clazz the model class to use as a resource route
	 * @param actions the specific actions for which to add resource routes
	 * @return a Routed object
	 * @see Action
	 * @see #addResource(String, Class, Action)
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
	 * Add a route to be handled by the given controller's handleRequest method.
	 * @param method the type of the request this route will handle
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed addRoute(HttpMethod method, String path, Class<? extends HttpController> clazz) {
		return add(getName(method, path)).asRoute(method, path, clazz);
	}
	
	/**
	 * Add a route to be handled by the given controller's handleRequest method in response to a
	 * GET request for the given path. The given path may contain variables and constants.
	 * @param path the path that this request will handle (may contain variables and constants)
	 * @param clazz the controller class that will handle the routed request
	 * @return a Routed object
	 */
	public Routed addRoute(String path, Class<? extends HttpController> clazz) {
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
	public Routed addRoute(String path, Class<? extends HttpController> clazz, Action action) {
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
				fixedRoutes = new HashMap<HttpMethod, Map<String,Route>>();
			}
			Map<String, Route> routes = fixedRoutes.get(route.httpMethod);
			if(routes == null) {
				routes = new HashMap<String, Route>();
				fixedRoutes.put(route.httpMethod, routes);
			}
			routes.put(route.path, route);
		} else {
			if(patternRoutes == null) {
				patternRoutes = new ArrayList<Route>();
			}
			patternRoutes.add(route);
		}
	}
	
	Route addRoute(String key, String rule, Class<? extends HttpController> clazz, HttpMethod method) {
		rule = checkRule(rule);
		Route route = new HttpRoute(method, rule, null, clazz, null);
		addRoute(key, route);
		return route;
	}

	HttpRoute addRoute(String key, String rule, Class<? extends Model> parentClass, String hasManyField, Class<? extends Model> clazz, Action action) {
		rule = checkRule(rule);
		HttpMethod type;
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
		Class<? extends HttpController> controllerClass = getControllerClass(clazz);
		HasManyRoute route = new HasManyRoute(type, rule, parentClass, hasManyField, modelClass, controllerClass, action);
		addRoute(key, route);
		return route;
	}
	
	Route addRtsp(String key, String rule, Class<? extends RtspController> clazz) {
		rule = checkRule(rule);
		Route route = new RtspRoute(rule, clazz);
		addRoute(key, route);
		return route;
	}
	
	Route addRoute(String key, String rule, Class<? extends View> clazz) {
		rule = checkRule(rule);
		Route route = new ViewRoute(GET, rule, clazz);
		addRoute(key, route);
		return route;
	}
	
	HttpRoute addRoute(String key, String rule, Class<?> clazz, Action action) {
		rule = checkRule(rule);
		HttpMethod type;
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
		Class<? extends HttpController> controllerClass = getControllerClass(clazz);
		HttpRoute route = new HttpRoute(type, rule, modelClass, controllerClass, action);
		addRoute(key, route);
		return route;
	}
	
	public Routed addRtsp(Class<? extends RtspController> clazz) {
		return add(underscored(clazz.getSimpleName())).asRtsp(clazz);
	}
	
	public Routed addRtsp(String path, Class<? extends RtspController> clazz) {
		return add(underscored(clazz.getSimpleName())).asRtsp(path, clazz);
	}
	
	/**
	 * Add a view route for the given view class. The new route will render the view in response to
	 * a GET request with a path of <code>varName(clazz)</code>.<br>
	 * For example: addView(Home.class) will render the Home view for a "GET /home" request.
	 * @param clazz the view to be rendered
	 * @return a Routed object
	 */
	public Routed addView(Class<? extends View> clazz) {
		return add(underscored(clazz.getSimpleName())).asView(clazz);
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
	
	public RoutedWebsocket addWebsocket(String path, Class<? extends WebsocketController> controller) {
		return add(getName(path)).asWebsocket(path, controller);
	}

	public RoutedWebsocket addNotifier(String path) {
		return add(getName(path)).asNotifier(path);
	}

	public Routed addModelNotifier() {
		return add(DEFAULT_MODEL_NOTIFY_PATH).asModelNotifier();
	}

	public Routed addModelNotifier(String path) {
		return add(getName(path)).asModelNotifier(path);
	}

	WebsocketRoute addWebsocketRoute(String key, String path, Class<? extends WebsocketController> controller, String group) {
		path = checkRule(path);
		WebsocketRoute route = new WebsocketRoute(path, controller, group);
		addRoute(key, route);
		updateModelNotificationPath(route);
		return route;
	}
	
	void updateModelNotificationPath(WebsocketRoute route) {
		if(MODEL_NOTIFY_GROUP.equals(route.group)) {
			modelNotificationPath = route.path;
		}
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

	private Class<? extends HttpController> getControllerClass(Class<?> clazz) {
		Class<? extends HttpController> controllerClass = null;
		if(Model.class.isAssignableFrom(clazz)) {
			controllerClass = service.getControllerClass(clazz.asSubclass(Model.class));
		} else if(HttpController.class.isAssignableFrom(clazz)) {
			controllerClass = clazz.asSubclass(HttpController.class);
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
	
	/**
	 * Get a Map of all model routes that have been routed, published or not.
	 * @return the Map of model routes
	 */
	public Map<String, Map<String, Map<String, String>>> getModelRouteMap() {
		return getModelRouteMap(getRoutes());
	}
	
	/**
	 * Get a Map of all model routes that are routed by the provided Collection of routes.
	 * @param routes the routes to check for models
	 * @return the Map of model routes
	 */
	public Map<String, Map<String, Map<String, String>>> getModelRouteMap(Collection<Route> routes) {
		Map<String, Map<String, Map<String, String>>> results = new TreeMap<String, Map<String, Map<String, String>>>();

		for(Route route : routes) {
			if(route instanceof HasManyRoute) {
				HasManyRoute hmr = (HasManyRoute) route;
				Class<?> c = hmr.parentClass;
				String f = hmr.hasManyField;
				Action a = hmr.action;
				if(c != null && a != null) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					map.put("method", route.httpMethod.getName());
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
					model.put(a.name() + ":" + f, map);
				}
			} else if(route instanceof HttpRoute) {
				HttpRoute cr = (HttpRoute) route;
				Class<?> c = cr.modelClass;
				Action a = cr.action;
				if(c != null && a != null) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					map.put("method", route.httpMethod.getName());
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

	private String getName(Action action, String path) {
		return action.name() + getName(path);
	}
	
	private String getName(HttpMethod type, String path) {
		return type.getName().toLowerCase() + getName(path);
	}

	private String getName(String path) {
		String name = (path.charAt(0) == '/') ? path.substring(1) : path;
		name = name.replaceAll("\\{([^\\}^\\:^\\=]+)[\\:\\=]?[^\\}]*\\}", "$1").replace('/', '_').replace('.', '_');
		name = camelCase(name).replace('?', '_');
		return name;
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

	public List<String> getPaths(HttpMethod type) {
		List<String> paths = new ArrayList<String>();
		if(fixedRoutes != null) {
			Map<String, Route> map = fixedRoutes.get(type);
			if(map != null) {
				paths.addAll(map.keySet());
			}
		}
		if(patternRoutes != null) {
			for(Route route : patternRoutes) {
				if(route.httpMethod == type) {
					paths.add(route.rule);
				}
			}
		}
		Collections.sort(paths);
		return paths;
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
	
	public List<Route> getRoutes(HttpMethod type) {
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
				if(route.httpMethod == type) {
					routes.add(route);
				}
			}
		}
		return routes;
	}
	
	public ModuleService getService() {
		return service;
	}

	/**
	 * Get the WebSocket registered under the given id.
	 * @param name the id of the WebSocket; can be null
	 * @return the WebSocket, or null if none is registered with the given name
	 */
	public Websocket getWebsocket(String id) {
		return (websocketsById != null) ? websocketsById.get(id) : null;
	}

	public Set<String> getWebsocketGroups() {
		return (websocketsByGroup != null) ? websocketsByGroup.keySet() : new HashSet<String>(0);
	}
	
	public Set<String> getWebsocketIds() {
		return (websocketsById != null) ? websocketsById.keySet() : new HashSet<String>(0);
	}

	public Set<Websocket> getWebsockets(Class<? extends WebsocketController> controllerClass) {
		return getWebsockets(controllerClass.getName());
	}
	
	/**
	 * Get the Set of WebSockets registered under the given group name.
	 * @param group the name of the group; can be null
	 * @return Set of WebSocket objects, or an empty Set if none are registered under the given group name; never null.
	 */
	public Set<Websocket> getWebsockets(String group) {
		if(websocketsByGroup != null) {
			Set<Websocket> sockets = websocketsByGroup.get(group);
			if(sockets != null) {
				return sockets;
			}
		}
		return new HashSet<Websocket>(0);
	}

	public Set<Websocket> getModelNotifiers() {
		return getWebsockets(MODEL_NOTIFY_GROUP);
	}
	
	public boolean isAuthorized(Request request, Realm realm) {
		String header = request.getHeader(HttpHeaders.Names.AUTHORIZATION);
		if(header != null && header.startsWith("Basic ")) {
			String[] sa = new String(Base64.decode(header.substring(6).getBytes())).split(":");
			if(sa.length == 2) {
				return realm.isAuthorized(sa[0], sa[1]);
			}
		}
		return false;
	}
	
	public boolean isAuthorized(Request request, String username, String password) {
		if(username == null) {
			return false;
		}
		String header = request.getHeader(HttpHeaders.Names.AUTHORIZATION);
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
	
	public void registerWebsocket(Websocket socket) {
		String id = socket.getId();
		if(id != null && id.length() > 0) {
			if(websocketsById == null) {
				websocketsById = new HashMap<String, Websocket>();
			}
			websocketsById.put(id, socket);
		}

		String group = socket.getGroup();
		if(group != null && group.length() > 0) {
			if(websocketsByGroup == null) {
				websocketsByGroup = new HashMap<String, Set<Websocket>>();
			}
			Set<Websocket> sockets = websocketsByGroup.get(group);
			if(sockets == null) {
				sockets = new HashSet<Websocket>();
				websocketsByGroup.put(group, sockets);
			}
			sockets.add(socket);
		}
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
	
	public void removeAssetRoutes() {
		List<String> paths = service.getAssetList();
		if(!blank(paths)) {
			for(String path : paths) {
				String[] sa = path.split(":", 2);
				if(sa.length == 2) {
					removeBasicAuthentication(HttpMethod.GET, sa[0]);
				}
			}
			if(fixedRoutes != null) {
				Map<String, Route> map = fixedRoutes.get(GET);
				if(map != null) {
					for(Iterator<Route> rIter = map.values().iterator(); rIter.hasNext(); ) {
						Route route = rIter.next();
						if(route instanceof StaticRoute) {
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
					if(route instanceof StaticRoute) {
						iter.remove();
					}
				}
				if(patternRoutes.isEmpty()) {
					patternRoutes = null;
				}
			}
		}
	}

	public void removeBasicAuthentication(HttpMethod method, String path) {
		if(authentications != null) {
			Map<String, Realm> auth = authentications.get(method);
			if(auth != null) {
				auth.remove(path);
				if(auth.isEmpty()) {
					authentications.remove(method);
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
	
	public void removeModelNotifier() {
		remove(DEFAULT_MODEL_NOTIFY_PATH);
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
	
	public void removeRoute(HttpMethod method, String path) {
		remove(getName(method, path));
	}

	private void removeRoute(Route route) {
		if(route.isFixed()) {
			if(fixedRoutes != null) {
				Map<String, Route> routes = fixedRoutes.get(route.httpMethod);
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

	HttpRoute removeRoute(String key, String path, Class<?> clazz, Action action) {
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		HttpMethod type;
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
		Class<? extends HttpController> controllerClass = getControllerClass(clazz);
		HttpRoute route = new HttpRoute(type, path, modelClass, controllerClass, action);
		removeRoute(key, route);
		return route;
	}
	
	public void removeView(Class<? extends View> clazz) {
		remove("show" + clazz.getSimpleName());
	}
	
	public void removeView(String path) {
		remove(getName(show, path));
	}
	
	public void setAutoPublish(boolean autoPublish) {
		this.autoPublish = autoPublish;
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
		if(Model.class.isAssignableFrom(clazz) || HttpController.class.isAssignableFrom(clazz)) {
			String path = (parameters == null) ? "/" : ("/?" + parameters);
			Route route = addRoute(HOME, path, clazz, action);
			return new Routed(this, route);
		} else {
			throw new IllegalArgumentException("invalid type: " + clazz.getSimpleName() + " (valid types are Model and Controller)");
		}
	}

	void unpublish(Route route) {
		if(published != null) {
			published.remove(route);
		}
	}

	public void unregisterWebsocket(Websocket socket) {
		String id = socket.getId();
		if(id != null && id.length() > 0) {
			if(websocketsById != null) {
				if(websocketsById.remove(id) != null) {
					if(websocketsById.isEmpty()) {
						websocketsById = null;
					}
				}
			}
		}

		String group = socket.getGroup();
		if(group != null && group.length() > 0) {
			if(websocketsByGroup != null) {
				Set<Websocket> sockets = websocketsByGroup.get(group);
				if(sockets != null) {
					if(sockets.remove(socket)) {
						if(sockets.isEmpty()) {
							websocketsByGroup.remove(group);
							if(websocketsByGroup.isEmpty()) {
								websocketsByGroup = null;
							}
						}
					}
				}
			}
		}
	}

}
