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
package org.oobium.app.server.controller;

import static org.oobium.persist.ModelAdapter.getAdapter;
import static org.oobium.utils.json.JsonUtils.toJson;
import static org.oobium.utils.json.JsonUtils.toObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oobium.persist.ActiveSet;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.RequiredSet;

public class JsonBuilder {

	public static final String INCLUDE = "include:";
	public static final Pattern valuePattern = Pattern.compile("#\\{(\\d+)\\}");

	public static String buildJson(Collection<? extends Model> models, String include, Object...values) {
		Model[] ma = models.toArray(new Model[models.size()]);
		JsonBuilder builder = new JsonBuilder();
		builder.setInclude(include, values);
		return builder.build(ma);
	}
	
	List<Object> includes;

	private JsonBuilder() {
		// private constructor
	}

	private void addIncludes(ModelAdapter adapter) {
		if(!addWildcards(adapter, includes)) {
			for(String field : adapter.getRelationFields()) {
				if(adapter.isIncluded(field)) {
					if(!contains(includes, field)) {
						includes.add(field);
					}
				}
			}
		}
		
		if(!includes.isEmpty()) {
			addIncludes(adapter, includes);
		}
	}

	@SuppressWarnings("unchecked")
	private void addIncludes(ModelAdapter adapter, List<Object> list) {
		for(Object object : list.toArray()) {
			if(object instanceof String) {
				if(isWildcard(getField(object))) {
					addWildcards(adapter, list);
					addIncludes(adapter, list);
				} else {
					addIncludesForStringInList(adapter, list, (String) object);
				}
			} else if(object instanceof Map<?,?>) {
				addIncludesForMap(adapter, (Map<?,Object>) object);
			} else {
				throw new IllegalArgumentException("lists cannot include objects of type " + ((object != null) ? object.getClass().getCanonicalName() : "null"));
			}
		}
	}

	// parentAdapter is the adapter for the container of the map
	private void addIncludesForMap(ModelAdapter parentAdapter, Map<?,Object> map) {
		Entry<?,Object> entry = map.entrySet().iterator().next();
		String field = (String) entry.getKey();
		Object value = entry.getValue();
		Class<? extends Model> clazz = parentAdapter.getRelationClass(getField(field));
		ModelAdapter adapter = getAdapter(clazz);
		List<Object> children = getModelIncludes(adapter);
		if(value instanceof List<?>) {
			for(Object object : (List<?>) value) {
				if(!contains(children, object)) { // object is a String or a Map
					children.add(object);
				}
			}
		} else { // a String or Map
			if(!contains(children, value)) {
				children.add(value);
			}
		}
		addIncludes(adapter, children);
		if(children.size() == 1) {
			entry.setValue(children.get(0));
		} else {
			entry.setValue(children);
		}
	}
	
	// parentAdapter is the adapter for the list
	private void addIncludesForStringInList(ModelAdapter parentAdapter, List<Object> list, String field) {
		Class<? extends Model> clazz = parentAdapter.getRelationClass(getField(field));
		ModelAdapter adapter = getAdapter(clazz);
		List<Object> children = getModelIncludes(adapter);
		if(!children.isEmpty()) {
			addIncludes(adapter, children);
			list.remove(field);
			Map<String, Object> map = new HashMap<String, Object>();
			if(field.matches("\\w+\\s+\\w+")) { // contains whitespace
				StringBuilder sb = new StringBuilder(field.length()+2);
				if(field.charAt(0) != '\'' && field.charAt(0) != '"') {
					sb.append('\'');
				}
				sb.append(field);
				if(field.charAt(0) != '\'' && field.charAt(0) != '"') {
					sb.append('\'');
				}
			}
			if(children.size() == 1) {
				map.put(field, children.get(0));
			} else {
				map.put(field, children);
			}
			list.add(map);
		}
	}
	
	private boolean addWildcards(ModelAdapter adapter, List<Object> list) {
		if(list.remove("*")) {
			for(String field : adapter.getRelationFields()) {
				if(!contains(list, field)) {
					list.add(field);
				}
			}
			return true;
		} else {
			if(list.remove("*1")) {
				for(String field : adapter.getHasOneFields()) {
					if(!contains(list, field)) {
						list.add(field);
					}
				}
			}
			if(list.remove("*M")) {
				for(String field : adapter.getHasManyFields()) {
					if(!contains(list, field)) {
						list.add(field);
					}
				}
			}
		}
		return false;
	}

	private String build(Model[] models) {
		ModelAdapter adapter = ModelAdapter.getAdapter(models[0].getClass());
		addIncludes(adapter);
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(Model model : models) {
			build(sb, model, includes);
			sb.append(',');
		}
		sb.setCharAt(sb.length()-1, ']');
		return sb.toString();
	}

	private void build(StringBuilder sb, Iterable<?> models, Object include) {
		sb.append('[');
		for(Object model : models) {
			build(sb, (Model) model, include);
			sb.append(',');
		}
		sb.setCharAt(sb.length()-1, ']');
	}
	
	private void build(StringBuilder sb, Model model, Object include) {
		sb.append("{id:").append(model.getId());
		for(Entry<String, Object> entry : model.getAll().entrySet()) {
			String field = entry.getKey();
			sb.append(",").append(field).append(":");
			Object value = entry.getValue();
			if(value instanceof Model) {
				Model m = (Model) value;
				if(include instanceof List<?>) {
					Object inc = getInclude((List<?>) include, field);
					if(inc instanceof String) {
						sb.append(m.toJson());
						continue;
					}
					if(inc instanceof Map<?,?>) {
						build(sb, m, ((Map<?,?>) inc).values().iterator().next());
						continue;
					}
				} else if(include instanceof Map<?,?>) {
					Entry<?,?> e = ((Map<?,?>) include).entrySet().iterator().next();
					if(field.equals(e.getKey())) {
						build(sb, m, e.getValue());
						continue;
					}
				} else if(field.equals(include)) {
					sb.append(m.toJson());
					continue;
				}
				sb.append(m.getId());
			} else if(value instanceof ActiveSet<?> || value instanceof RequiredSet<?>) {
				Set<?> s = (Set<?>) value;
				if(include instanceof List<?>) {
					Object inc = getInclude((List<?>) include, field);
					if(inc instanceof String) {
						sb.append(toJson(s));
						continue;
					}
					if(inc instanceof Map<?,?>) {
						build(sb, s, ((Map<?,?>) inc).values().iterator().next());
						continue;
					}
				} else if(include instanceof Map<?,?>) {
					Entry<?,?> e = ((Map<?,?>) include).entrySet().iterator().next();
					if(field.equals(e.getKey())) {
						build(sb, s, e.getValue());
						continue;
					}
				} else if(field.equals(include)) {
					build(sb, s, null);
					continue;
				}
				sb.append(toJson(value));
			} else if(value instanceof Iterable<?> || value instanceof Map<?,?>) {
				throw new IllegalArgumentException("JsonBuilder cannot currently handle fields of type " + value.getClass());
			} else {
				sb.append(toJson(value));
			}
		}
		sb.append("}");
	}
	
	private Object getInclude(List<?> list, String field) {
		for(Object o : list) {
			if(field.equals(getField(o))) {
				return o;
			}
		}
		return null;
	}
	
	private boolean contains(List<?> list, Object object) {
		String field = getField(object);
		for(Object o : list) {
			if(equals(field, o)) {
				return true;
			}
		}
		return false;
	}

	private String getField(Object object) {
		String field;
		if(object instanceof Map<?,?>) {
			field = (String) ((Map<?,?>) object).keySet().iterator().next();
		} else {
			StringBuilder sb = new StringBuilder((String) object);
			if(sb.charAt(0) == '\'') {
				sb.delete(0, 1);
			}
			if(sb.charAt(sb.length()-1) == '\'') {
				sb.delete(sb.length()-1, sb.length());
			}
			field = sb.toString().split("\\s+", 2)[0];
		}
		return field;
	}
	
	private boolean equals(String field, Object object) {
		return (object instanceof String && field.equals(object)) || 
		(object instanceof Map<?, ?> && ((Map<?, ?>) object).containsKey(field)) ||
		(object instanceof List<?> && ((List<?>) object).contains(field));
	}
	
	private List<Object> getModelIncludes(ModelAdapter adapter) {
		List<Object> includes = new ArrayList<Object>();
		for(String field : adapter.getRelationFields()) {
			if(adapter.isIncluded(field)) {
				includes.add(field);
			}
		}
		return includes;
	}
	
	private boolean isWildcard(Object s) {
		return "*".equals(s) || "*1".equals(s) || "*M".equals(s);
	}
	
	@SuppressWarnings("unchecked")
	public void setInclude(String include, Object...values) {
		StringBuilder sb = include.startsWith(INCLUDE) ? new StringBuilder(include.substring(INCLUDE.length())) : new StringBuilder(include);
		Matcher matcher = valuePattern.matcher(sb); // allow replacements?  ("include: {?}", "member")... why not?
		while(matcher.find()) {
			int ix = Integer.parseInt(matcher.group(1));
			sb.replace(matcher.start(), matcher.end(), (String) values[ix]);
		}
		Object object = toObject(sb.toString());
		if(object instanceof List<?>) {
			this.includes = (List<Object>) object;
		} else {
			this.includes = new ArrayList<Object>();
			this.includes.add(object);
		}
	}
	
}
