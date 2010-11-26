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

import static org.oobium.utils.SqlUtils.getColumnType;
import static org.oobium.utils.StringUtils.columnName;

import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelRelation;

public class ColumnDescriptor {

	private String name;
	private String type;
	private boolean required;
	private int precision;
	private int scale;
	private boolean unique;
	private String check;
	private String defaultValue;

	public ColumnDescriptor(ModelAttribute attribute) {
		name = columnName(attribute.getName());
		type = getColumnType(attribute.getType());
		required = attribute.isRequired() || attribute.isPrimitive();
		precision = attribute.getPrecision();
		scale = attribute.getScale();
		unique = attribute.isUnique();
		check = attribute.getCheck();
		if(attribute.isPrimitive()) {
			String type = attribute.getType();
			if(type.equals("double") || type.equals("float")) {
				defaultValue = "0.0";
			} else {
				defaultValue = "0";
			}
		}
	}
	
	public ColumnDescriptor(ModelRelation relation) {
		name = columnName(relation.getName());
		type = getColumnType(Integer.class);
		required = relation.isRequired();
		unique = false; // the index will make it unique
	}
	
	public String name() { return name; }
	public boolean required() { return required; }
	public String type() { return type; }
	public int precision() { return precision; }
	public int scale() { return scale; }
	public boolean unique() { return unique; }
	public String check() { return check; }
	
	public boolean hasCheck() { return check != null && check.length() > 0; }

	public String getDefault() {
		return defaultValue;
	}
	
	public boolean hasDefault() {
		return defaultValue != null && defaultValue.length() > 0;
	}
	
}
