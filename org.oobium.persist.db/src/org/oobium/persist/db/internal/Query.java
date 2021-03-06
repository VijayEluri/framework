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
package org.oobium.persist.db.internal;

import static org.oobium.utils.StringUtils.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;

public class Query {

	public static final String ID_MARKER = "#{ids}";

	private String sourceField;
	private Class<? extends Model> parentClass;
	private String field;
	
	private Class<? extends Model> clazz;
	
	private String sql;
	private Object[] values;
	
	private Map<String, ModelAdapter> aliasAdapters;
	private Map<String, String> aliasFields;

	private Map<String, String> parentAliases;
	private List<Query> children;
	
	Query(Class<? extends Model> clazz) {
		this.clazz = clazz;
	}
	
	Query(String sourceField, Class<? extends Model> parentClass, String field, Class<? extends Model> clazz) {
		this.sourceField = sourceField;
		this.parentClass = parentClass;
		this.field = field;
		this.clazz = clazz;
	}
	
	void addChild(Query child) {
		if(children == null) {
			children = new ArrayList<Query>();
		}
		children.add(child);
	}

	ModelAdapter getAdapter(String alias) {
		if(aliasAdapters == null) {
			return null;
		}
		return aliasAdapters.get(alias);
	}

	List<Query> getChildren() {
		return children;
	}
	
	String getField() {
		return field;
	}
	
	String getField(String alias) {
		if(aliasFields == null) {
			return null;
		}
		return aliasFields.get(alias);
	}
	
	String getParentAlias(String alias) {
		if(parentAliases == null) {
			return null;
		}
		return parentAliases.get(alias);
	}
	
	Class<? extends Model> getParentClass() {
		return parentClass;
	}
	
	String getSql() {
		return sql;
	}
	
	String getSql(List<? extends Model> models) {
		List<Object> ids = new ArrayList<Object>();
		for(Model model : models) {
			if(sourceField != null) {
				model = (Model) model.get(sourceField);
			}
			if(model != null) {
				ids.add(model.getId());
			}
		}
		return ids.isEmpty() ? null : sql.replace(ID_MARKER, join(ids, ','));
	}
	
	Class<? extends Model> getType() {
		return clazz;
	}
	
	Class<? extends Model> getType(String alias) {
		if(aliasAdapters == null) {
			return clazz;
		}
		return aliasAdapters.get(alias).getModelClass();
	}
	
	public Object[] getValues() {
		return values;
	}
	
	boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	boolean hasField() {
		return field != null;
	}
	
	/**
	 * @return true if this is a sub-query (indicated by having a field)
	 * @see #hasField()
	 */
	boolean isSub() {
		return field != null;
	}

	void putAdapter(String alias, ModelAdapter adapter) {
		if(aliasAdapters == null) {
			aliasAdapters = new HashMap<String, ModelAdapter>();
		}
		aliasAdapters.put(alias, adapter);
	}
	
	void putField(String alias, String field) {
		if(aliasFields == null) {
			aliasFields = new HashMap<String, String>();
		}
		aliasFields.put(alias, field);
	}
	
	void putParentAlias(String alias, String parentAlias) {
		if(parentAliases == null) {
			parentAliases = new HashMap<String, String>();
		}
		parentAliases.put(alias, parentAlias);
	}
	
	void setSql(String sql) {
		this.sql = sql;
	}
	
	void setValues(Object[] values) {
		this.values = values;
	}
	
}
