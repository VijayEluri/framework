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
package org.oobium.cache;

public interface CacheService {

	public static final String TYPE = "type";
	public static final String TYPE_FILE = "file";
	public static final String TYPE_MEMORY = "memory";
	
	public abstract void expire(String key);
	
	public abstract void expire();
	
	public abstract CacheObject get(String key);
	
	public abstract void set(String key, byte[] value);
	
}
