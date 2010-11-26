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
package org.oobium.persist.db.internal;

import java.util.HashMap;
import java.util.Map;

import org.oobium.persist.Model;

public class DbCache {

	private static final ThreadLocal<Map<String, Object>> threadCache = new ThreadLocal<Map<String, Object>>();

	public static void expireCache() {
		synchronized(threadCache) {
			Map<String, Object> cache = threadCache.get();
			if(cache != null) {
				cache.clear();
			}
			threadCache.set(null);
		}
	}

	public static <T extends Model> T getCache(Class<T> clazz, int id) {
		if(clazz != null && id >= 1) {
			Map<String, Object> cache = threadCache.get();
			if(cache != null) {
				Object object = getThreadCache().get(clazz.getCanonicalName() + ":" + id);
				return clazz.cast(object);
			}
		}
		return null;
	}

	private static Map<String, Object> getThreadCache() {
		Map<String, Object> cache = threadCache.get();
		if(cache == null) {
			cache = new HashMap<String, Object>();
			threadCache.set(cache);
		}
		return cache;
	}
	
	public static void setCache(Model model) {
		if(!model.isNew()) {
			String key = model.getClass().getCanonicalName() + ":" + model.getId();
			getThreadCache().put(key, model);
		}
	}
	
}
