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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.oobium.utils.json.JsonUtils;


public class MapCoercer extends AbstractCoercer {

	public Map<?,?> coerce(Object o, Class<?> toType) {
		if(o instanceof String) {
			return coerce((String) o, toType);
		}
		throw new IllegalArgumentException();
	}
	
	public Map<?,?> coerce(String s, Class<?> toType) {
		if(toType == LinkedHashMap.class) {
			return JsonUtils.toMap(s, true);
		}
		return JsonUtils.toMap(s);
	}
	
	@Override
	public Object coerceNull(Class<?> toType) {
		if(toType == LinkedHashMap.class) {
			return new LinkedHashMap<Object, Object>(0);
		}
		return new HashMap<Object, Object>(0);
	}
	
	@Override
	public Class<?> getType() {
		return Map.class;
	}

	@Override
	public boolean handleSubTypes() {
		return true;
	}
}
