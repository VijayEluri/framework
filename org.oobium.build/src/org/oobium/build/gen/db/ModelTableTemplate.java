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
package org.oobium.build.gen.db;

import static org.oobium.persist.Relation.*;
import static org.oobium.utils.SqlUtils.*;
import static org.oobium.utils.StringUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;


public class ModelTableTemplate {

	private String name;
	private List<ColumnDescriptor> columns;
	private List<String> constraints;
	private List<String> indexes;

	public ModelTableTemplate(ModelDefinition model, Collection<ModelDefinition> models) {
		name = tableName(model.getSimpleName());
		columns = new ArrayList<ColumnDescriptor>();
		constraints = new ArrayList<String>();
		indexes = new ArrayList<String>();

		for(ModelRelation relation : model.getRelations()) {
			if(!relation.hasMany() && !relation.isThrough()) {
				addRelation(relation);
			}
		}

		for(ModelAttribute attribute : model.getAttributes()) {
			addAttribute(attribute);
		}
		
		for(String index : model.getIndexes()) {
			addIndex(index);
		}
	}
	
	private void addAttribute(ModelAttribute attribute) {
		columns.add(new ColumnDescriptor(attribute));
		if(attribute.isIndex()) {
			indexes.add(createIndexSql(name, attribute.isUnique(), columnName(attribute.getName())));
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
		indexes.add(createIndexSql(name, unique, columns));
	}
	
	private void addRelation(ModelRelation relation) {
		columns.add(new ColumnDescriptor(relation));
		String column = columnName(relation.getName());

		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE ").append(name).append(" ADD");
		sb.append(" CONSTRAINT ").append(name).append("_").append(column).append("_fk");
		sb.append(" Foreign Key (").append(safeSqlWord(column)).append(")");
		sb.append(" REFERENCES ").append(tableName(relation.getSimpleType())).append(" (id)");
		switch(relation.onDelete()) {
		case CASCADE:
			sb.append(" ON DELETE CASCADE");
			break;
		case RESTRICT:
			sb.append(" ON DELETE RESTRICT");
			break;
		case SET_NULL:
			sb.append(" ON DELETE SET NULL");
			break;
		case NO_ACTION:
			sb.append(" ON DELETE NO ACTION");
			break;
		default:
//			if(relation.isRequired()) {
//				sb.append(" ON DELETE CASCADE");
//			} else {
//				sb.append(" ON DELETE SET NULL");
//			}
			break;
		}
		constraints.add(sb.toString());

		indexes.add(createIndexSql(name, relation.isUnique(), column));
	}

	public List<String> getConstraintSQL() {
		return constraints;
	}

	public String getCreateSQL() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(safeSqlWord(name));
		sb.append("(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY");
		for(ColumnDescriptor column : columns) {
			sb.append(',');
			sb.append(safeSqlWord(column.name()));
			sb.append(' ');
			sb.append(column.type());
			if("DECIMAL".equals(column.type())) {
				sb.append("(").append(column.precision()).append(",").append(column.scale()).append(")");
			}
			if(column.unique()) {
				sb.append(" UNIQUE");
			}
			if(column.hasDefault()) {
				sb.append(" DEFAULT ").append(column.getDefault());
			}
			if(column.required()) {
				sb.append(" NOT NULL");
			}
			if(column.hasCheck()) {
				sb.append(" CHECK(").append(column.check()).append(")");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public List<String> getIndexSQL() {
		return indexes;
	}
	
	public boolean hasConstraint() {
		return !constraints.isEmpty();
	}
	
	public boolean hasIndex() {
		return !indexes.isEmpty();
	}
	
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " {" + name + "}";
	}
	
}
