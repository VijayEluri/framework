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
package org.oobium.utils.json;

import java.util.Map;

public interface JsonModel {
	public abstract Object get(String field);
	public abstract Map<String, Object> getAll();
	
	/**
	 * Get the ID for this model. ID values greater than zero indicate that this
	 * model has been persisted and is no longer new. Newly created models will
	 * start out with their ID set to the Java <code>int</code> default of zero.
	 * @return the Id of this model
	 */
	public abstract int getId();
	
	/**
	 * A model is considered blank if it is new, and the Map returned by {@link #getAll()}
	 * is blank - it is either empty, or contains only empty objects (String of zero length,
	 * empty Maps, empty Collections, etc.).
	 * @return true if this model is considered to be blank; false otherwise
	 */
	public abstract boolean isBlank();
	
	/**
	 * A model is considered empty if the Map returned by {@link #getAll()} is empty.
	 * Note that the ID may be set even though this method returns true.
	 * @return true if this model is considered to be empty; false otherwise
	 * @see #getAll()
	 */
	public abstract boolean isEmpty();
	
	/**
	 * A model is considered new until its ID is set to a value greater than zero.
	 * Note that even is a model is not new, it may still be empty (but not blank).
	 * @return true if this model is considered to be new; false otherwise
	 */
	public abstract boolean isNew();
	
	public abstract boolean isSet(String field);
	public abstract JsonModel put(String field, Object value);
	public abstract JsonModel putAll(JsonModel model);
	public abstract JsonModel putAll(Map<String, Object> data);
	public abstract JsonModel putAll(String json);
	public abstract JsonModel set(String field, Object value);
	public abstract JsonModel setAll(Map<String, Object> data);
	public abstract JsonModel setAll(String json);
	public abstract JsonModel setId(int id);
	public abstract String toJson();
}
