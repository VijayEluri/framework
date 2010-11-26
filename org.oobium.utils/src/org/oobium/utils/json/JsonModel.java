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
	public abstract int getId();
	public abstract boolean isBlank();
	public abstract boolean isEmpty();
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
