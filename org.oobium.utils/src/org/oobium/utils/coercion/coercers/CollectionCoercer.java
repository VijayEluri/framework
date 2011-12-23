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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.oobium.utils.json.JsonUtils;


public class CollectionCoercer extends AbstractCoercer {

	public Collection<?> coerce(Object o, Class<?> toType) {
		if(o instanceof String) {
			return coerce((String) o, toType);
		}
		if(o instanceof Collection) {
			return coerce((Collection<?>) o, toType);
		}
		if(o != null && o.getClass().isArray()) {
			int length = Array.getLength(o);
			List<Object> list = new ArrayList<Object>();
			for(int i = 0; i < length; i++) {
				list.add(Array.get(o, i));
			}
			return coerce(list, toType);
		}
		throw new IllegalArgumentException();
	}
	
	public Collection<?> coerce(String s, Class<?> toType) {
		return coerce(JsonUtils.toList(s), toType);
	}
	
	public Collection<?> coerce(Collection<?> c, Class<?> toType) {
		if(c.getClass() == toType) {
			return c;
		}
		if(toType.isInterface()) {
			if(toType == List.class) {
				if(c instanceof List) {
					return c;
				}
				return new ArrayList<Object>(c);
			} else if(toType == Set.class) {
				if(c instanceof Set) {
					return c;
				}
				return new LinkedHashSet<Object>(c);
			} else if(toType == Collection.class) {
				return c;
			}
			throw new IllegalArgumentException();
		} else {
			try {
				Constructor<?> ctor = toType.getConstructor(Collection.class);
				return (Collection<?>) ctor.newInstance(c);
			} catch(Exception e) {
				throw new IllegalArgumentException();
			}
		}
	}

	@Override
	public Object coerceNull(Class<?> toType) {
		if(toType.isInterface()) {
			if(toType == List.class) {
				return new ArrayList<Object>(0);
			}
			if(toType == Set.class) {
				return new LinkedHashSet<Object>(0);
			}
			if(toType == Collection.class) {
				return new ArrayList<Object>(0);
			}
			throw new IllegalArgumentException();
		} else {
			try {
				Constructor<?> ctor = toType.getConstructor(int.class);
				return (Collection<?>) ctor.newInstance(0);
			} catch(Exception e) {
				throw new IllegalArgumentException();
			}
		}
	}
	
	@Override
	public Class<?> getType() {
		return Collection.class;
	}

	@Override
	public boolean handleSubTypes() {
		return true;
	}
}
