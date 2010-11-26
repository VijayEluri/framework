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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

	public static final String SERIALIZATION_TYPE_KEY = "__type__";
	
	public static Object deserialize(String json) {
		if(json == null) {
			return null;
		}
		
		Map<String, Object> map = JsonUtils.toMap(json);
		if(!map.containsKey(SERIALIZATION_TYPE_KEY)) {
			return toObject(json);
		}
		try {
			Class<?> type = Class.forName(String.valueOf(map.get(SERIALIZATION_TYPE_KEY)));
			Field[] fields = type.getDeclaredFields();
			Constructor<?> ctor = type.getDeclaredConstructor();
			if(ctor != null) {
				if(!ctor.isAccessible()) ctor.setAccessible(true);
				Object object = ctor.newInstance();
				for(Field field : fields) {
					String name = field.getName();
					if(map.containsKey(name)) {
						if(!field.isAccessible()) field.setAccessible(true);
						field.set(object, map.get(field.getName()));
					}
				}
				return object;
			}
		} catch(Exception e) {
			// discard - just return null
		}
		return null;
	}
	
	private static int end(StringBuilder sb, int start) {
		char c = sb.charAt(start);
		for(int i = start+1; i < sb.length(); i++) {
			if(sb.charAt(i) == c && i > 0 && sb.charAt(i-1) != '\\') {
				return i;
			}
		}
		return sb.length();
	}

	public static String format(String json) {
		int lvl = 0;
		StringBuilder sb = new StringBuilder(json);
		for(int i = 0; i < sb.length(); i++) {
			switch(sb.charAt(i)) {
			case '"':
			case '\'':
				i = end(sb, i);
				break;
			case '{':
			case '[':
				lvl++;
				i += newline(i+1, lvl, sb);
				break;
			case '}':
			case ']':
				lvl--;
				i += newline(i, lvl, sb);
				break;
			case ',':
				i += newline(i+1, lvl, sb);
				break;
			case ':':
				sb.insert(i+1, ' ');
				i++;
				break;
			}
		}
		return sb.toString();
	}
	
	private static int newline(int offset, int lvl, StringBuilder sb) {
		if(offset < sb.length()) {
			sb.insert(offset, '\n');
			for(int i = 1; i <= lvl; i++) {
				sb.insert(offset+i, '\t');
			}
			return lvl + 1;
		}
		return 0;
	}
	
	public static String serialize(Object object) {
		if(object == null) {
			return null;
		}
		if(object instanceof Iterable<?>) {
			return toJson(object);
		}
		if(object instanceof Map<?,?>) {
			return toJson(object);
		}
		if(object.getClass().isArray()) {
			return toJson(object);
		}
		
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		fields.put(SERIALIZATION_TYPE_KEY, object.getClass().getName());
		try {
			for(Field field : object.getClass().getDeclaredFields()) {
				if(!field.isSynthetic()) {
					field.setAccessible(true);
					fields.put(field.getName(), field.get(object));
				}
			}
			return JsonUtils.toJson(fields);
		} catch(Exception e) {
			// discard - just return null
		}
		return null;
	}

	public static String toJson(Iterable<?> objs) {
		return new JsonBuilder().toJson(objs);
	}
	
	/**
	 * Convert the give {@link Map} to a JSON formatted {@link String}
	 * @param obj a {@link Map} object that is to be converted to a JSON formatted {@link String}
	 * @return a {@link String} in JSON format
	 */
	public static String toJson(Map<?, ?> obj) {
		return new JsonBuilder().toJson(obj);
	}
	
	/**
	 * Convert the give {@link Map} to a JSON formatted {@link String}
	 * @param obj a {@link Map} object that is to be converted to a JSON formatted {@link String}
	 * @param skip an array of keys that should not be converted to JSON, typically because they are already in JSON format
	 * and a second conversion would corrupt the data.
	 * @return a {@link String} in JSON format
	 */
	public static String toJson(Map<?, ?> obj, String...skip) {
		JsonBuilder jb = new JsonBuilder();
		jb.setSkip(skip);
		return jb.toJson(obj);
	}

	public static String toJson(Object value) {
		return new JsonBuilder().toJson(value);
	}
	
	public static String toJson(Object value, IConverter converter) {
		JsonBuilder jb = new JsonBuilder();
		jb.setConverter(converter);
		return jb.toJson(value);
	}
	
	public static String toJson(Object[] objs) {
		return new JsonBuilder().toJson(objs);
	}

	public static List<Object> toList(String json) {
		return new JsonParser().toList(json);
	}

	public static Map<String, Object> toMap(String json) {
		return new JsonParser().toMap(json);
	}

	public static Map<String, Object> toMap(String json, boolean keepOrder) {
		JsonParser jp = new JsonParser();
		jp.setKeepOrder(keepOrder);
		return jp.toMap(json);
	}

	public static Object toObject(String json) {
		return new JsonParser().toObject(json);
	}

	public static Object toObject(String json, boolean keepOrder) {
		JsonParser jp = new JsonParser();
		jp.setKeepOrder(keepOrder);
		return jp.toObject(json);
	}

	public static List<String> toStringList(String json) {
		return new JsonParser().toStringList(json);
	}

	public static Map<String, String> toStringMap(String json) {
		return new JsonParser().toStringMap(json);
	}
	
	public static Map<String, String> toStringMap(String json, boolean keepOrder) {
		JsonParser jp = new JsonParser();
		jp.setKeepOrder(keepOrder);
		return jp.toStringMap(json);
	}
	
}
