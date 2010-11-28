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
package org.oobium.app.server.controller;

import static org.oobium.app.server.controller.Controller.createActionCacheKey;
import static org.oobium.app.server.controller.Controller.createCacheKey;
import static org.oobium.utils.StringUtils.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oobium.app.AppService;
import org.oobium.cache.CacheService;
import org.oobium.http.constants.ContentType;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.Observer;
import org.oobium.utils.StringUtils;

/**
 * ActionCache is broken.
 * Observer have been made generic and ActionCache has not been updated yet.
 * Will be done the first chance I need an ActionCache - submit a bug if you need it sooner.
 */
public class ActionCache extends Observer {

	protected static final Logger logger = Logger.getLogger();
	
	private static final Map<Class<?>, ActionCache> cacheMap = new HashMap<Class<?>, ActionCache>();
	private static final Map<Class<?>, Set<Action>> caches = new HashMap<Class<?>, Set<Action>>();
	
	public static final void addCache(Class<? extends ActionCache> cacheClass, Class<? extends Model> modelClass, Action...actions) {
		addCache(cacheClass, modelClass, null, actions);
	}
	
	public static final void addCache(Class<? extends ActionCache> cacheClass, Class<? extends Model> modelClass, Class<? extends Controller> controllerClass) {
		addCache(cacheClass, modelClass, controllerClass, new Action[0]);
	}
	
	@SuppressWarnings("unchecked")
	public static final void addCache(Class<? extends ActionCache> cacheClass, Class<? extends Model> modelClass, Class<? extends Controller> controllerClass, Action...actions) {
		if(logger.isLoggingInfo()) {
			logger.info("adding cache (" + cacheClass.getSimpleName() + ", " + modelClass.getSimpleName() + ", " + 
					(controllerClass != null ? controllerClass.getSimpleName() : "null") + ", " + 
					(actions.length > 0 ? join(actions, ", ") : "<All>") + ")");
		}

		if(cacheMap.containsKey(cacheClass)) {
			logger.warn("cache already added - skipping");
			return;
		}
		
		try {
			if(controllerClass == null) {
				String name = modelClass.getCanonicalName().replace(".models.", ".controllers.") + "Controller";
				controllerClass = (Class<? extends Controller>) Class.forName(name, true, modelClass.getClassLoader());
			}
			if(actions == null || actions.length == 0) {
				actions = Action.values();
			}

			ActionCache cache = cacheClass.newInstance();
			cache.controllerClass = controllerClass;
			cache.actions = actions;

			cacheMap.put(cacheClass, cache);
			
			addObserver(cache);
			
			for(Action action : actions) {
				if(!caches.containsKey(controllerClass)) {
					caches.put(controllerClass, new HashSet<Action>());
				}
				caches.get(controllerClass).add(action);
			}
		} catch(Exception e) {
			logger.error("failed to add cache (" + cacheClass.getSimpleName() + ", " + modelClass.getSimpleName() + ", " + 
					(controllerClass != null ? controllerClass.getSimpleName() : "null") + ", " + StringUtils.join(actions, ", ") + ")");
			throw new RuntimeException(e);
		}
	}

	public static List<ActionCache> getCaches(Collection<Class<?>> classes) {
		List<ActionCache> caches = new ArrayList<ActionCache>();
		for(Class<?> clazz : classes) {
			ActionCache cache = cacheMap.get(clazz);
			if(cache != null) {
				caches.add(cache);
			}
		}
		return caches;
	}
	
	public static boolean isCaching(Controller controller, Action action) {
		Class<? extends Controller> controllerClass = controller.getClass();
		return caches.containsKey(controllerClass) && caches.get(controllerClass).contains(action);
	}

	
	private Class<? extends Controller> controllerClass;
	private Action[] actions;
	private AppService handler;

	public void expire() {
		for(Action action : actions) {
			expire(action);
		}
	}
	
	public void expire(Action action) {
		CacheService service = handler.getCacheService();
		if(service != null) {
			String baseKey = createCacheKey(handler, createActionCacheKey(controllerClass, action));
			service.expire(baseKey);
			String regex = baseKey.replaceAll("\\.", "\\.") + "\\..*";
			String[] keys = service.getKeys(regex);
			for(String key : keys) {
				service.expire(key);
			}
		}
	}
	
	public void expire(Action action, ContentType type) {
		CacheService service = handler.getCacheService();
		if(service != null) {
			service.expire(createCacheKey(handler, createActionCacheKey(controllerClass, action, type)));
		} else {
			logger.warn("cache service is not available");
		}
	}

	public final void setHandler(AppService handler) {
		this.handler = handler;
	}
	
}
