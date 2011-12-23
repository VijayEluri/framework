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
package org.oobium.utils.coercion.coercers;

import java.lang.reflect.Method;
import java.util.Map;

import org.oobium.logging.LogProvider;
import org.oobium.utils.json.JsonModel;

public class JsonModelCoercer extends AbstractCoercer {

	public JsonModel coerce(Object object, Class<? extends JsonModel> toType) {
		if(object instanceof String) {
			return createModel((String) object, toType);
		}
		if(object instanceof Map<?,?>) {
			return coerce((Map<?,?>) object, toType);
		}
		return createModel(object, toType);
	}

	public JsonModel coerce(Map<?,?> map, Class<? extends JsonModel> toType) {
		return createModel(map, toType);
	}
	
	public JsonModel coerce(String string, Class<? extends JsonModel> toType) {
		try {
			int id = Integer.valueOf(string);
			return createModel(id, toType);
		} catch(NumberFormatException e) {
			return createModel(string, toType);
		}
	}

	@Override
	public JsonModel coerceNull(Class<?> toType) {
		return null;
	}
	
	private JsonModel createModel(Object id, Class<? extends JsonModel> toType) {
		try {
			JsonModel model = toType.newInstance();
			Method method = toType.getMethod("setId", Object.class);
			method.invoke(model, id);
			return model;
		} catch(Exception e) {
			LogProvider.getLogger().warn(e.getClass().getSimpleName() + ": " + e.getMessage());
			return null;
		}
	}
	
	/** 
	 * The JSON data is the raw state of the model object, not a bunch of parameters to be "set",
	 * therefore set it directly to the model's map.
	 * <p>This also allows attribute.init to work properly.</p>
	 */
	private JsonModel createModel(Map<?,?> data, Class<? extends JsonModel> toType) {
		try {
			JsonModel model = toType.newInstance();
			Method method = toType.getMethod("putAll", Map.class);
			method.invoke(model, data);
			return model;
		} catch(Exception e) {
			return null;
		}
	}

	private JsonModel createModel(String json, Class<? extends JsonModel> toType) {
		try {
			JsonModel model = toType.newInstance();
			// see comment in #createModel(Map, Class)
			Method method = toType.getMethod("putAll", String.class);
			method.invoke(model, json);
			return model;
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public Class<?> getType() {
		return JsonModel.class;
	}
	
	@Override
	public boolean handleSubTypes() {
		return true;
	}

}
