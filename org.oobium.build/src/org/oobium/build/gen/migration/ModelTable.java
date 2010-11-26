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
package org.oobium.build.gen.migration;

import static org.oobium.persist.migrate.defs.Column.DATESTAMPS;
import static org.oobium.persist.migrate.defs.Column.TIMESTAMPS;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.columnName;
import static org.oobium.utils.StringUtils.tableName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.persist.Relation;
import org.oobium.persist.migrate.defs.Column;
import org.oobium.persist.migrate.defs.Index;
import org.oobium.persist.migrate.defs.columns.ForeignKey;


public class ModelTable {

	public String name;
	public List<Column> columns;
	public List<Index> indexes;
	public List<ForeignKey> foreignKeys;

	public ModelTable(ModelDefinition model, Collection<ModelDefinition> models) {
		name = tableName(model.getSimpleName());
		columns = new ArrayList<Column>();
		foreignKeys = new ArrayList<ForeignKey>();
		indexes = new ArrayList<Index>();

		boolean datestamps = model.getModel().datestamps();
		boolean timestamps = model.getModel().timestamps();
		for(ModelAttribute attribute : model.getAttributes()) {
			if(datestamps) {
				if(attribute.getName().equals("createdOn") || attribute.getName().equals("updatedOn")) {
					continue;
				}
			}
			if(timestamps) {
				if(attribute.getName().equals("createdAt") || attribute.getName().equals("updatedAt")) {
					continue;
				}
			}
			addAttribute(attribute);
		}
		if(datestamps) {
			columns.add(new Column(null, DATESTAMPS));
		}
		if(timestamps) {
			columns.add(new Column(null, TIMESTAMPS));
		}
		
		for(ModelRelation relation : model.getRelations()) {
			if(!relation.hasMany() && !relation.isThrough()) {
				addRelation(relation);
			}
		}

		for(String index : model.getIndexes()) {
			addIndex(index);
		}
	}
	
	private void addAttribute(ModelAttribute attribute) {
		String name = columnName(attribute.getName());
		String type = attribute.getType();
		
		Map<String, Object> options = new LinkedHashMap<String, Object>();
		if(attribute.isRequired() || attribute.isPrimitive()) {
			options.put("required", true);
		}
		if("BigDecimal".equals(attribute.getType())) {
			options.put("precision", attribute.getPrecision());
			options.put("scale", attribute.getScale());
		}
		if(attribute.isUnique()) {
			options.put("unique", true);
		}
		if(!blank(attribute.getCheck())) {
			options.put("check", attribute.getCheck());
		}
		if(attribute.isPrimitive()) {
			options.put("primitive", true);
		}

		columns.add(new Column(type, name, options.isEmpty() ? null : options));
		
		if(attribute.isIndex()) {
			indexes.add(new Index(columnName(attribute.getName()), attribute.isUnique()));
		}
	}

	private void addIndex(String index) {
		boolean unique = index.startsWith("Unique-");
		if(unique) {
			index = index.substring(7);
		}
		String[] fields = index.split(",");
		String[] columns = new String[fields.length];
		for(int i = 0; i < fields.length; i++) {
			columns[i] = columnName(fields[i]);
		}
		indexes.add(new Index(columns, unique));
	}
	
	private void addRelation(ModelRelation relation) {
		// add the column
		String name = columnName(relation.getName());
		String type = Integer.class.getCanonicalName();
		Map<String, Object> options = new LinkedHashMap<String, Object>();
		if(relation.isRequired()) {
			options.put("required", true);
		}
		columns.add(new Column(type, name, options.isEmpty() ? null : options));

		// add the foreign key
		String column = columnName(relation.getName());
		String reference = tableName(relation.getSimpleType());
		options = new LinkedHashMap<String, Object>();
		if(relation.onDelete() != Relation.UNDEFINED) {
			options.put("onDelete", relation.onDelete());
		}
		foreignKeys.add(new ForeignKey(column, reference, options.isEmpty() ? null : options));

		// add the index
		indexes.add(new Index(column, relation.isUnique()));
	}

	public boolean hasForeignKey() {
		return !foreignKeys.isEmpty();
	}
	
	public boolean hasIndex() {
		return !indexes.isEmpty();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " {" + name + "}";
	}
	
}
