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
package org.oobium.persist;

import static org.oobium.persist.ModelAdapter.getAdapter;
import static org.oobium.utils.json.JsonUtils.toJson;
import static org.oobium.utils.json.JsonUtils.toObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class ModelJsonBuilder {

	public static final String INCLUDE = "include:";
	public static final Pattern valuePattern = Pattern.compile("#\\{(\\d+)\\}");

	static String buildJson(Collection<? extends Model> models) {
		Model[] ma = models.toArray(new Model[models.size()]);
		ModelJsonBuilder builder = new ModelJsonBuilder();
		return builder.build(ma);
	}
	
	static String buildJson(Collection<? extends Model> models, String include, Object...values) {
		Model[] ma = models.toArray(new Model[models.size()]);
		ModelJsonBuilder builder = new ModelJsonBuilder();
		builder.setInclude(include, values);
		return builder.build(ma);
	}
	
	static String buildJson(Model model) {
		ModelJsonBuilder builder = new ModelJsonBuilder();
		return builder.build(model);
	}
	
	static String buildJson(Model model, String include, Object...values) {
		ModelJsonBuilder builder = new ModelJsonBuilder();
		builder.setInclude(include, values);
		return builder.build(model);
	}
	

	List<Object> includes;

	private ModelJsonBuilder() {
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
		if(clazz != null) { // clazz == null when a field is set that is not in the ModelDescription
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

	private void build(List<Object> list, Iterable<?> value, Object include) {
		for(Object item : value) {
			handleItem(list, item, include);
		}
	}

	private void build(Map<String, Object> map, Map<?,?> value, Object include) {
		for(Entry<?, ?> entry : value.entrySet()) {
			String field = String.valueOf(entry.getKey());
			handleField(map, field, entry.getValue(), include);
		}
	}

	private void build(Map<String, Object> map, Model model, Object include) {
		if(!model.isNew()) {
			map.put("id", model.getId());
		}
		if(model.hasErrors()) {
			map.put("errors", model.getErrorsList());
		}
		ModelAdapter adapter = ModelAdapter.getAdapter(model);
		for(Entry<String, Object> entry : model.getAll().entrySet()) {
			String field = entry.getKey();
			if(adapter.isJson(field)) {
				handleField(map, field, entry.getValue(), include);
			}
		}
	}
	
	private String build(Model model) {
		if(includes == null) {
			includes = new ArrayList<Object>(0);
		}
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		ModelAdapter adapter = ModelAdapter.getAdapter(model.getClass());
		addIncludes(adapter);
		build(map, model, includes);
		return toJson(map);
	}
	
	private String build(Model[] models) {
		if(includes == null) {
			includes = new ArrayList<Object>(0);
		}
		ModelAdapter adapter = ModelAdapter.getAdapter(models[0].getClass());
		addIncludes(adapter);
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		for(Model model : models) {
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			build(map, model, includes);
			list.add(map);
		}
		return toJson(list);
	}

	private void buildArray(List<Object> list, Object value, Object include) {
		for(int i = 0; i < Array.getLength(value); i++) {
			Object item = Array.get(value, i);
			handleItem(list, item, include);
		}
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

	private boolean equals(String field, Object object) {
		return (object instanceof String && field.equals(object)) || 
		(object instanceof Map<?, ?> && ((Map<?, ?>) object).containsKey(field)) ||
		(object instanceof List<?> && ((List<?>) object).contains(field));
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
	
	private Object getInclude(List<?> list, String field) {
		for(Object o : list) {
			if(field.equals(getField(o))) {
				return o;
			}
		}
		return null;
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
	
	private void handleArray(Map<String, Object> map, String field, Object value, Object include) {
		if(include instanceof List<?>) {
			Object inc = getInclude((List<?>) include, field);
			if(inc instanceof String) {
				includeArray(map, field, value, inc);
				return;
			}
			if(inc instanceof Map<?,?>) {
				includeArray(map, field, value, ((Map<?,?>) inc).values().iterator().next());
				return;
			}
		} else if(include instanceof Map<?,?>) {
			Entry<?,?> e = ((Map<?,?>) include).entrySet().iterator().next();
			if(field.equals(e.getKey())) {
				includeArray(map, field, value, e.getValue());
				return;
			}
		} else if(field.equals(include)) {
			includeArray(map, field, value, null);
			return;
		}
	}

	private void handleField(Map<String, Object> map, String field, Object object, Object include) {
		if(object == null) {
			map.put(field, null);
		}
		else if(object instanceof Model) {
			handleModel(map, field, (Model) object, include);
		}
		else if(object instanceof Map) {
			handleMap(map, field, (Map<?,?>) object, include);
		}
		else if(object instanceof Iterable) {
			handleIterable(map, field, (Iterable<?>) object, include);
		}
		else if(object.getClass().isArray()) {
			handleArray(map, field, object, include);
		}
		else {
			map.put(field, object);
		}
	}
	
	private void handleItem(List<Object> list, Object object, Object include) {
		if(object == null) {
			list.add(null);
		}
		else if(object instanceof Model) {
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			build(m, (Model) object, include);
			list.add(m);
		}
		else if(object instanceof Map) {
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			build(m, (Map<?,?>) object, include);
			list.add(m);
		}
		else if(object instanceof Iterable) {
			List<Object> l = new ArrayList<Object>();
			build(l, (Iterable<?>) object, include);
			list.add(l);
		}
		else if(object.getClass().isArray()) {
			List<Object> l = new ArrayList<Object>();
			buildArray(l, object, include);
			list.add(l);
		}
		else {
			list.add(object);
		}
	}
	
	private void handleIterable(Map<String, Object> map, String field, Iterable<?> value, Object include) {
		if(include instanceof List<?>) {
			Object inc = getInclude((List<?>) include, field);
			if(inc instanceof String) {
				include(map, field, value, inc);
				return;
			}
			if(inc instanceof Map<?,?>) {
				include(map, field, value, ((Map<?,?>) inc).values().iterator().next());
				return;
			}
		} else if(include instanceof Map<?,?>) {
			Entry<?,?> e = ((Map<?,?>) include).entrySet().iterator().next();
			if(field.equals(e.getKey())) {
				include(map, field, value, e.getValue());
				return;
			}
		} else if(field.equals(include)) {
			include(map, field, value, null);
			return;
		}
	}
	
	private void handleMap(Map<String, Object> map, String field, Map<?,?> value, Object include) {
		if(include instanceof List<?>) {
			Object inc = getInclude((List<?>) include, field);
			if(inc instanceof String) {
				include(map, field, value, inc);
				return;
			}
			if(inc instanceof Map<?,?>) {
				include(map, field, value, ((Map<?,?>) inc).values().iterator().next());
				return;
			}
		} else if(include instanceof Map<?,?>) {
			Entry<?,?> e = ((Map<?,?>) include).entrySet().iterator().next();
			if(field.equals(e.getKey())) {
				include(map, field, value, e.getValue());
				return;
			}
		} else if(field.equals(include)) {
			include(map, field, value, null);
			return;
		}
	}
	
	private void handleModel(Map<String, Object> map, String field, Model model, Object include) {
		if(include instanceof List<?>) {
			Object inc = getInclude((List<?>) include, field);
			if(inc instanceof String) {
				include(map, field, model, inc);
				return;
			}
			if(inc instanceof Map<?,?>) {
				include(map, field, model, ((Map<?,?>) inc).values().iterator().next());
				return;
			}
		} else if(include instanceof Map<?,?>) {
			Entry<?,?> e = ((Map<?,?>) include).entrySet().iterator().next();
			if(field.equals(e.getKey())) {
				include(map, field, model, e.getValue());
				return;
			}
		} else if(field.equals(include)) {
			include(map, field, model, null);
			return;
		}
		if(model.isNew()) {
			map.put(field, null);
		} else {
			map.put(field, model.getId());
		}
	}
	
	private void include(Map<String, Object> map, String field, Iterable<?> value, Object include) {
		List<Object> list = new ArrayList<Object>();
		build(list, value, include);
		map.put(field, list);
	}

	private void include(Map<String, Object> map, String field, Map<?,?> value, Object include) {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		build(m, value, include);
		map.put(field, m);
	}
	
	private void include(Map<String, Object> map, String field, Model value, Object include) {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		build(m, value, include);
		map.put(field, m);
	}
	
	private void includeArray(Map<String, Object> map, String field, Object value, Object include) {
		List<Object> list = new ArrayList<Object>();
		buildArray(list, value, include);
		map.put(field, list);
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
