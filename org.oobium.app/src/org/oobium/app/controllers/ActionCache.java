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
package org.oobium.app.controllers;

import static org.oobium.app.controllers.HttpController.createActionCacheKey;
import static org.oobium.app.controllers.HttpController.createCacheKey;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oobium.app.AppService;
import org.oobium.app.ModuleService;
import org.oobium.cache.CacheService;
import org.oobium.app.http.Action;
import org.oobium.app.http.MimeType;
import org.oobium.logging.Logger;
import org.oobium.logging.LogProvider;
import org.oobium.persist.Model;
import org.oobium.persist.Observer;

public class ActionCache<T extends Model> extends Observer<T> {

	protected static final Logger logger = LogProvider.getLogger();
	
	private static final Map<Class<?>, ActionCache<?>> cacheMap = new HashMap<Class<?>, ActionCache<?>>();
	private static final Map<Class<?>, Set<Action>> actionMap = new HashMap<Class<?>, Set<Action>>();
	
	protected static void forActions(Action...actions) {
		if(actions == null || actions.length == 0) {
			actions = Action.values();
		}
		actionMap.put(ActionCache.class, new HashSet<Action>(Arrays.asList(actions)));
	}
	
	
	public synchronized static final void addCache(AppService handler, ModuleService module, Class<? extends ActionCache<?>> cacheClass) {
		if(cacheMap.containsKey(cacheClass)) {
			logger.warn("cache " + cacheClass.getSimpleName() + " already added - skipping");
		} else {
			try {
				ActionCache<?> cache = cacheClass.newInstance();
				addCache(handler, module, cache);
				if(logger.isLoggingInfo()) {
					logger.info("added action cache (" + cache.getClass().getSimpleName() + ")");
				}
			} catch(Exception e) {
				logger.error("error adding cache " + cacheClass, e);
				throw new RuntimeException(e);
			} finally {
				actionMap.remove(ActionCache.class);
			}
		}
	}

	private synchronized static void addCache(AppService handler, ModuleService module, ActionCache<? extends Model> cache) {
		Type type = cache.getClass().getGenericSuperclass();
		if(type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Class<?> clazz = (Class<?>) pt.getActualTypeArguments()[0];
			addCacheToMap(handler, module, cache, clazz.asSubclass(Model.class));
		} else {
			throw new IllegalArgumentException("ActionCache class must be parameterized");
		}
	}

	private static void addCacheToMap(AppService handler, ModuleService module, ActionCache<? extends Model> cache, Class<? extends Model> modelClass) {
		cache.handler = handler;
		cache.controllerClass = module.getControllerClass(modelClass);
		
		cacheMap.put(cache.controllerClass, cache);
		actionMap.put(cache.controllerClass, actionMap.get(ActionCache.class));
		
		addObserver(cache);
	}

	public static List<ActionCache<? extends Model>> getCaches(Collection<Class<?>> classes) {
		List<ActionCache<? extends Model>> caches = new ArrayList<ActionCache<? extends Model>>();
		for(Class<?> clazz : classes) {
			ActionCache<? extends Model> cache = cacheMap.get(clazz);
			if(cache != null) {
				caches.add(cache);
			}
		}
		return caches;
	}
	
	public static boolean isCaching(HttpController controller, Action action) {
		Class<? extends HttpController> controllerClass = controller.getClass();
		return actionMap.containsKey(controllerClass) && actionMap.get(controllerClass).contains(action);
	}

	public synchronized static void removeCache(ModuleService app, Class<?> clazz) {
		if(Model.class.isAssignableFrom(clazz)) {
			Class<?> controllerClass = app.getControllerClass(clazz.asSubclass(Model.class));
			ActionCache<?> cache = cacheMap.remove(controllerClass);
			actionMap.remove(controllerClass);
			if(logger.isLoggingInfo() && cache != null) {
				logger.info("removed action cache (" + cache.getClass().getSimpleName() + ")");
			}
		} else if(ActionCache.class.isAssignableFrom(clazz)) {
			for(Iterator<ActionCache<?>> cacheIter = cacheMap.values().iterator(); cacheIter.hasNext(); ) {
				ActionCache<?> cache = cacheIter.next();
				if(cache.getClass() == clazz) {
					cacheIter.remove();
					actionMap.remove(cache.controllerClass);
					if(logger.isLoggingInfo() && cache != null) {
						logger.info("removed action cache (" + cache.getClass().getSimpleName() + ")");
					}
				}
			}
		}
	}
	
	
	private Class<? extends HttpController> controllerClass;
	private AppService handler;

	public void expire() {
		for(Action action : actionMap.get(controllerClass)) {
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
	
	public void expire(Action action, MimeType type) {
		CacheService service = handler.getCacheService();
		if(service != null) {
			service.expire(createCacheKey(handler, createActionCacheKey(controllerClass, action, type)));
		} else {
			logger.warn("cache service is not available");
		}
	}

}
