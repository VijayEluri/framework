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
package org.oobium.persist.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.persist.Model;

public class Cache {

	// {modelClass}:{modelId} -> model
	private static final ThreadLocal<Map<String, Model>> cachedModels = new ThreadLocal<Map<String, Model>>();
	
	// {modelClass}:{query} -> list of models
	private static final ThreadLocal<Map<String, List<? extends Model>>> cachedQueries = new ThreadLocal<Map<String, List<? extends Model>>>();

	public static void expireCache() {
		synchronized(cachedModels) {
			Map<String, Model> cache = cachedModels.get();
			if(cache != null) {
				cache.clear();
			}
			cachedModels.set(null);
		}
		synchronized(cachedQueries) {
			Map<String, List<? extends Model>> cache = cachedQueries.get();
			if(cache != null) {
				cache.clear();
			}
			cachedQueries.set(null);
		}
	}

	public static <T extends Model> T getCache(Class<T> clazz, Object id) {
		if(clazz != null) {
			Map<String, Model> cache = cachedModels.get();
			if(cache != null) {
				Object object = cache.get(clazz.getCanonicalName() + ":" + id);
				return clazz.cast(object);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Model> List<T> getCache(Class<T> clazz, String query) {
		if(clazz != null) {
			Map<String, Model> cache = cachedModels.get();
			if(cache != null) {
				String key = clazz.getCanonicalName() + ":" + query;
				Object object = cache.get(key);
				return (List<T>) object;
			}
		}
		return null;
	}

	public static void setCache(Class<? extends Model> clazz, String query, List<? extends Model> models) {
		Map<String, List<? extends Model>> cache = cachedQueries.get();
		if(cache == null) {
			synchronized(cachedQueries) {
				cache = cachedQueries.get();
				if(cache == null) {
					cache = new HashMap<String, List<? extends Model>>();
					cachedQueries.set(cache);
				}
			}
		}
		String key = clazz.getCanonicalName() + ":" + query;
		cache.put(key, models);
	}
	
	public static void setCache(Model model) {
		if(!model.isNew()) {
			String key = model.getClass().getCanonicalName() + ":" + model.getId();
			Map<String, Model> cache = cachedModels.get();
			if(cache == null) {
				synchronized(cachedModels) {
					cache = cachedModels.get();
					if(cache == null) {
						cache = new HashMap<String, Model>();
						cachedModels.set(cache);
					}
				}
			}
			cache.put(key, model);
		}
	}
	
}
