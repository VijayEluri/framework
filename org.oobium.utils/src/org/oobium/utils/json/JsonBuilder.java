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

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.utils.Base64;

public class JsonBuilder {

	public static JsonBuilder jsonBuilder(IConverter converter) {
		JsonBuilder builder = new JsonBuilder();
		builder.setConverter(converter);
		return builder;
	}
	
	
	private String[] skip;
	private IConverter converter;
	
	public void setConverter(IConverter converter) {
		this.converter = converter;
	}
	
	public void setSkip(String...skip) {
		this.skip = skip;
	}
	
	public String toJson(Iterable<?> objs) {
		if(objs == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(Iterator<?> iter = objs.iterator(); iter.hasNext(); ) {
			sb.append(toJson(iter.next()));
			if(iter.hasNext()) sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Convert the give {@link Map} to a JSON formatted {@link String}
	 * @param obj a {@link Map} object that is to be converted to a JSON formatted {@link String}
	 * @return a {@link String} in JSON format
	 */
	public String toJson(Map<?, ?> obj) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for(Iterator<?> iter = obj.entrySet().iterator(); iter.hasNext(); ) {
			Entry<?, ?> entry = (Entry<?, ?> ) iter.next();
			String key = String.valueOf(entry.getKey());
			Object val = entry.getValue();
			sb.append('"').append(key).append('"').append(':');
			if(val instanceof String && skip != null && skip.length > 0) {
				boolean doSkip = false;
				for(int i = 0; i < skip.length; i++) {
					if(key.equals(skip[i])) {
						doSkip = true;
						break;
					}
				}
				if(doSkip) {
					sb.append(val);
				} else {
					sb.append(toJson(val));
				}
			} else {
				sb.append(toJson(val));
			}
			if(iter.hasNext()) {
				sb.append(',');
			}
		}
		sb.append('}');
		return sb.toString();
	}

	public String toJson(Object value) {
		if(converter != null) {
			value = converter.convert(value);
		}
		if(value == null) {
			return "null";
		}
		if(value instanceof String) {
			String s = (String) value;
			if(s.length() > 1 && 
					((s.charAt(0) == '"' && s.charAt(s.length()-1) == '"') || (s.charAt(0) == '\'' && s.charAt(s.length()-1) == '\''))) {
				return s;
			}
			return "\"" + value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + "\"";
		}
		if(value instanceof Character) {
			return "'" + value + "'";
		}
		if(value instanceof JsonModel) {
			return ((JsonModel) value).toJson();
		}
		if(value instanceof Map<?,?>) {
			return toJson((Map<?,?>) value);
		}
		if(value instanceof Iterable<?>) {
			return toJson((Iterable<?>) value);
		}
		if(value instanceof byte[]) {
			return "\"/Base64(" + new String(Base64.encode((byte[]) value)) + ")/\"";
		}
		if(value instanceof java.util.Date) {
			return "\"/Date(" + ((Date) value).getTime() + ")/\"";
		}
		if(value.getClass().isArray()) {
			if(value.getClass().getComponentType().isPrimitive()) {
				Object[] oa = new Object[Array.getLength(value)];
				for(int i = 0; i < oa.length; i++) {
					oa[i] = Array.get(value, i);
				}
				return toJson(oa);
			} else {
				return toJson((Object[]) value);
			}
		}
		if(value instanceof Number) {
			return value.toString();
		}
		if(value instanceof Boolean) {
			return value.toString();
		}
		if(value instanceof Class<?>) {
			return ((Class<?>) value).getName();
		}

		return "\"" + value.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + "\"";
	}
	
	public String toJson(Object[] objs) {
		if(objs == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i = 0; i < objs.length; i++) {
			if(i != 0) sb.append(",");
			sb.append(toJson(objs[i]));
		}
		sb.append("]");
		return sb.toString();
	}

	
}
