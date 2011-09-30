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

import static org.oobium.app.controllers.HttpController.createCacheKey;
import static org.oobium.utils.StringUtils.join;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oobium.app.AppService;
import org.oobium.app.ModuleService;
import org.oobium.app.http.Action;
import org.oobium.app.http.MimeType;
import org.oobium.cache.CacheService;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.Observer;

public abstract class ControllerCache<T extends Model> extends Observer<T> {

	protected static final Logger logger = LogProvider.getLogger();
	
	private static final Map<Class<?>, ControllerCache<?>> cacheMap = new HashMap<Class<?>, ControllerCache<?>>();
	
	
	public synchronized static final void addCache(AppService handler, ModuleService module, Class<? extends ControllerCache<?>> cacheClass) {
		if(cacheMap.containsKey(cacheClass)) {
			logger.warn("cache " + cacheClass.getSimpleName() + " already added - skipping");
		} else {
			try {
				ControllerCache<?> cache = cacheClass.newInstance();
				addCache(handler, module, cache);
				if(logger.isLoggingInfo()) {
					logger.info("added controller cache (" + cache.getClass().getSimpleName() + ")");
				}
			} catch(Exception e) {
				logger.error("error adding cache " + cacheClass, e);
				throw new RuntimeException(e);
			}
		}
	}

	private synchronized static void addCache(AppService handler, ModuleService module, ControllerCache<? extends Model> cache) {
		Type type = cache.getClass().getGenericSuperclass();
		if(type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Class<?> clazz = (Class<?>) pt.getActualTypeArguments()[0];
			addCacheToMap(handler, module, cache, clazz.asSubclass(Model.class));
		} else {
			throw new IllegalArgumentException("ControllerCache class must be parameterized");
		}
	}

	private static void addCacheToMap(AppService handler, ModuleService module, ControllerCache<? extends Model> cache, Class<? extends Model> modelClass) {
		cache.handler = handler;
		cache.controllerClass = module.getControllerClass(modelClass);
		
		cacheMap.put(cache.controllerClass, cache);
		
		addObserver(cache);
	}

	public static List<ControllerCache<? extends Model>> getCaches(Collection<Class<?>> classes) {
		List<ControllerCache<? extends Model>> caches = new ArrayList<ControllerCache<? extends Model>>();
		for(Class<?> clazz : classes) {
			ControllerCache<? extends Model> cache = cacheMap.get(clazz);
			if(cache != null) {
				caches.add(cache);
			}
		}
		return caches;
	}
	
	public static String getCacheKey(HttpController controller) {
		Class<? extends HttpController> controllerClass = controller.getClass();
		ControllerCache<?> cache = cacheMap.get(controllerClass);
		if(cache != null) {
			return cache.getKey(controller);
		}
		return null;
	}

	public synchronized static void removeCache(ModuleService app, Class<?> clazz) {
		if(Model.class.isAssignableFrom(clazz)) {
			Class<?> controllerClass = app.getControllerClass(clazz.asSubclass(Model.class));
			ControllerCache<?> cache = cacheMap.remove(controllerClass);
			if(logger.isLoggingInfo() && cache != null) {
				logger.info("removed controller cache (" + cache.getClass().getSimpleName() + ")");
			}
		} else if(ControllerCache.class.isAssignableFrom(clazz)) {
			for(Iterator<ControllerCache<?>> cacheIter = cacheMap.values().iterator(); cacheIter.hasNext(); ) {
				ControllerCache<?> cache = cacheIter.next();
				if(cache.getClass() == clazz) {
					cacheIter.remove();
					if(logger.isLoggingInfo() && cache != null) {
						logger.info("removed controller cache (" + cache.getClass().getSimpleName() + ")");
					}
				}
			}
		}
	}
	
	
	private Class<? extends HttpController> controllerClass;
	private AppService handler;

	public final void expire() {
		doExpire(createCacheKey(handler, controllerClass));
	}
	
	public final void expire(Action action) {
		doExpire(createCacheKey(handler, controllerClass, action));
	}
	
	public final void expire(Action action, String...keys) {
		doExpire(createCacheKey(handler, controllerClass, action, keys));
	}
	
	public final void expire(Action action, MimeType type) {
		doExpire(createCacheKey(handler, controllerClass, action, type));
	}

	public final void expire(Action action, MimeType type, String...keys) {
		doExpire(createCacheKey(handler, controllerClass, action, type, keys));
	}

	public final void expire(String...keys) {
		expire(createCacheKey(handler, controllerClass, keys));
	}

	private void doExpire(String key) {
		CacheService service = handler.getCacheService();
		if(service != null) {
			service.expire(key);
		} else {
			logger.warn("cache service is not available");
		}
	}

	protected abstract String getKey(HttpController controller);
	
	protected final String createKey(Action action) {
		return action.name();
	}
	
	protected final String createKey(Action action, String...keys) {
		if(keys.length == 0 || (keys.length == 1 && keys[0] == null)) {
			return action.name();
		}
		if(keys.length == 1) {
			return action.name() + "/" + keys[0];
		}
		return action.name() + "/" + join(keys, '/');
	}
	
	protected final String createKey(Action action, MimeType type) {
		return join('/', action.name(), type.extension());
	}
	
	protected final String createKey(Action action, MimeType type, String...keys) {
		if(keys.length == 0 || (keys.length == 1 && keys[0] == null)) {
			return action.name() + "/" + type.extension();
		}
		if(keys.length == 1) {
			return action.name() + "/" + type.extension() + "/" + keys[0];
		}
		return action.name() + "/" + type.extension() + "/" + join(keys, '/');
	}
	
	protected final String createKey(String...keys) {
		if(keys.length == 0) {
			throw new IllegalArgumentException("must have one or more keys");
		}
		if(keys.length == 1) {
			if(keys[0] == null) {
				return null;
			}
			return keys[0];
		}
		return join('/', join(keys, '/'));
	}
	
}
