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
package org.oobium.utils.coercion;

import static org.oobium.utils.json.JsonUtils.deserialize;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.oobium.utils.coercion.coercers.ArrayCoercer;
import org.oobium.utils.coercion.coercers.BigDecimalCoercer;
import org.oobium.utils.coercion.coercers.BooleanCoercer;
import org.oobium.utils.coercion.coercers.CalendarCoercer;
import org.oobium.utils.coercion.coercers.CollectionCoercer;
import org.oobium.utils.coercion.coercers.DateCoercer;
import org.oobium.utils.coercion.coercers.DoubleCoercer;
import org.oobium.utils.coercion.coercers.EnumCoercer;
import org.oobium.utils.coercion.coercers.FileCoercer;
import org.oobium.utils.coercion.coercers.IntegerCoercer;
import org.oobium.utils.coercion.coercers.JsonModelCoercer;
import org.oobium.utils.coercion.coercers.LocaleCoercer;
import org.oobium.utils.coercion.coercers.LongCoercer;
import org.oobium.utils.coercion.coercers.MapCoercer;
import org.oobium.utils.coercion.coercers.PrimitiveBooleanCoercer;
import org.oobium.utils.coercion.coercers.PrimitiveDoubleCoercer;
import org.oobium.utils.coercion.coercers.PrimitiveIntCoercer;
import org.oobium.utils.coercion.coercers.PrimitiveLongCoercer;
import org.oobium.utils.coercion.coercers.StringCoercer;
import org.oobium.utils.coercion.coercers.TimeCoercer;
import org.oobium.utils.coercion.coercers.TimeZoneCoercer;
import org.oobium.utils.coercion.coercers.TimestampCoercer;


public class TypeCoercer {

	private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private static final Map<Class<?>, Coercer> coercers;
	private static final Map<Coercer, Map<Class<?>, Method>> methods;
	
	private static final List<Class<?>> assignables;
	private static Coercer arrayCoercer;
	
	static {
		coercers = new HashMap<Class<?>, Coercer>();
		methods = new HashMap<Coercer, Map<Class<?>, Method>>();
		assignables = new ArrayList<Class<?>>();

		addCoercer(new ArrayCoercer());
		addCoercer(new BigDecimalCoercer());
		addCoercer(new BooleanCoercer());
		addCoercer(new CalendarCoercer());
		addCoercer(new CollectionCoercer());
		addCoercer(new DateCoercer());
		addCoercer(new DoubleCoercer());
		addCoercer(new EnumCoercer());
		addCoercer(new FileCoercer());
		addCoercer(new IntegerCoercer());
		addCoercer(new JsonModelCoercer());
		addCoercer(new LocaleCoercer());
		addCoercer(new LongCoercer());
		addCoercer(new MapCoercer());
		addCoercer(new PrimitiveBooleanCoercer());
		addCoercer(new PrimitiveDoubleCoercer());
		addCoercer(new PrimitiveIntCoercer());
		addCoercer(new PrimitiveLongCoercer());
		addCoercer(new StringCoercer());
		addCoercer(new TimeCoercer());
		addCoercer(new TimestampCoercer());
		addCoercer(new TimeZoneCoercer());
	}

	public static void addCoercer(Coercer coercer) {
		Class<?> objectType = coercer.getType();
		Map<Class<?>, Method> map = new HashMap<Class<?>, Method>();
		for(Method method : coercer.getClass().getMethods()) {
			if("coerce".equals(method.getName())) {
				Class<?>[] params = method.getParameterTypes();
				if(params.length == 2 && params[1] == Class.class) {
					map.put(params[0], method);
				}
			}
		}
		if(!map.isEmpty()) {
			lock.writeLock().lock();
			try {
				coercers.put(objectType, coercer);
				methods.put(coercer, map);
				if(coercer.handleSubTypes()) {
					assignables.add(objectType);
				}
				if(objectType.isArray()) {
					arrayCoercer = coercer;
				}
			} finally {
				lock.writeLock().unlock();
			}
		}
	}
	
	/**
	 * Creates a new object that represents the given object once it
	 * has been coerced into the given type.
	 * @param object the object to be coerced
	 * @param type the type to coerce the object into (also the return type)
	 * @return the new coerced object
	 * @throws IllegalArgumentException if the type is null or there is an error coercing the given object into the given type
	 * @throws UnsupportedOperationException if the given object cannot be coerced into the given type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T coerce(Object object, Class<T> type) {
		if(type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		
		if(object == null) {
			Coercer coercer = null;
			lock.readLock().lock();
			try {
				coercer = getCoercer(type);
			} finally {
				lock.readLock().unlock();
			}
			if(coercer == null) {
				return null;
			} else {
				// this type of cast handles Integer -> int... seems there should be a better way...
				return (T) coercer.coerceNull();
			}
		} else if(type.isInstance(object)) {
			return type.cast(object);
		} else {
			Coercer coercer = null;
			Method method = null;
			lock.readLock().lock();
			try {
				coercer = getCoercer(type);
				if(coercer != null) {
					// methods are added per coercer: next line should never return null
					Map<Class<?>, Method> map = methods.get(coercer);
					method = map.get(object.getClass());
					if(method == null) {
						// there is no specific method, try for a generic coerce(Object) method
						method = map.get(Object.class);
					}
				}
			} finally {
				lock.readLock().unlock();
			}
			if(coercer == null) {
				if(object instanceof String) {
					Object value = deserialize((String) object);
					if(value != null && type.isAssignableFrom(value.getClass())) {
						return (T) value;
					}
				}
				throw new UnsupportedOperationException("no coercer for type " + type.getName());
			} else if(methods == null) {
				throw new UnsupportedOperationException(coercer.getClass().getName() + " can not coerce type " + object.getClass().getName() + " to type "+ type.getName());
			} else {
				try {
					// this type of cast handles Integer -> int... seems there should be a better way...
					return (T) (method.invoke(coercer, object, type));
				} catch(ClassCastException e) {
					throw new UnsupportedOperationException(method + " in " + coercer.getClass().getName() + " does not return a type " + type.getName());
				} catch(UnsupportedOperationException e) {
					throw new UnsupportedOperationException(coercer.getClass().getName() + " can not coerce type " + object.getClass().getName() + " to type "+ type.getName());
				} catch(IllegalArgumentException e) {
					throw e;
				} catch(IllegalAccessException e) {
					throw new IllegalArgumentException(coercer.getClass().getName() + " can not coerce type " + object.getClass().getName() + " to type "+ type.getName(), e);
				} catch(InvocationTargetException e) {
					throw new IllegalArgumentException(coercer.getClass().getName() + " can not coerce type " + object.getClass().getName() + " to type "+ type.getName(), e);
				}
			}
		}
	}

	/**
	 * If the given object is not null, then creates a new object that represents the given object once it has been coerced 
	 * into the type of the given default value; otherwise, simply returns the default value.
	 * @param object the object to be coerced
	 * @param defaultValue the default to use if the given object is null; also the type to coerce the object into if it is not null.
	 * <b>This value cannot be null</b>.
	 * @return the new coerced object, or the default value if the given object is null
	 * @throws IllegalArgumentException if the defaultValue is null or there is an error coercing the given object into the given type
	 * @throws UnsupportedOperationException if the given object cannot be coerced into the given type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T coerce(Object object, T defaultValue) {
		if(defaultValue != null) {
			Object value = coerce(object, defaultValue.getClass());
			if(value != null && defaultValue.getClass().isAssignableFrom(value.getClass())) {
				return (T) value;
			} else {
				return defaultValue;
			}
		}
		throw new IllegalArgumentException("defaultValue cannot be null");
	}

	private static Coercer getCoercer(Class<?> type) {
		Coercer coercer = coercers.get(type);
		if(coercer == null) {
			if(type.isArray()) {
				coercer = arrayCoercer;
			} else {
				for(Class<?> clazz : assignables) {
					if(clazz.isAssignableFrom(type)) {
						coercer = coercers.get(clazz);
					}
				}
			}
		}
		return coercer;
	}


	private TypeCoercer() {
		// private constructor
	}
	
}
