/*******************************************************************************
 * Copyright (c) 2010, 2011 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.build.model;

import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.build.model.ModelDefinition.*;

import java.util.Map;

import org.oobium.persist.Attribute;
import org.oobium.persist.Binary;
import org.oobium.persist.Text;


public class ModelAttribute {

	public final ModelDefinition model;
	public String check;
	public String init;
	public String name;
	public boolean json;
	public int precision;
	public int scale;
	public String type;
	public boolean indexed;
	public boolean readOnly;
	public boolean unique;
	public boolean virtual;
	
	public ModelAttribute(ModelDefinition model, String annotation) {
		this.model = model;

		if("createdOn".equals(annotation) || "updatedOn".equals(annotation)) {
			this.name = annotation;
			this.type = "java.sql.Date";
			this.json = false;
			this.check = "";
			this.init = "";
			this.precision = 0;
			this.scale = 0;
			this.indexed = false;
			this.readOnly = false;
			this.unique = false;
			this.virtual = false;
		} else if("createdAt".equals(annotation) || "updatedAt".equals(annotation)) {
			this.name = annotation;
			this.type = "java.util.Date";
			this.json = false;
			this.check = "";
			this.init = "";
			this.precision = 0;
			this.scale = 0;
			this.indexed = false;
			this.readOnly = false;
			this.unique = false;
			this.virtual = false;
		} else {
			char[] ca = annotation.toCharArray();
			int start = annotation.indexOf('(') + 1;
			int end = annotation.length() - 1;
			Map<String, String> entries = getJavaEntries(ca, start, end);
			
			this.name = getString(entries.get("name"));
			this.type = model.getType(entries.get("type"));
			this.json = coerce(entries.get("json"), false);
			this.check = getString(entries.get("check"));
			this.init = getString(entries.get("init"));
			this.precision = coerce(entries.get("precision"), 8);
			this.scale = coerce(entries.get("scale"), 2);
			this.indexed = coerce(entries.get("indexed"), false);
			this.readOnly = coerce(entries.get("readOnly"), false);
			this.unique = coerce(entries.get("unique"), false);
			this.virtual = coerce(entries.get("virtual"), false);
		}
	}
	
	private ModelAttribute(ModelAttribute original, ModelDefinition model) {
		this.model = model;
		this.check = original.check;
		this.init = original.init;
		this.name = original.name;
		this.precision = original.precision;
		this.scale = original.scale;
		this.type = original.type;
		this.json = original.json;
		this.indexed = original.indexed;
		this.readOnly = original.readOnly;
		this.unique = original.unique;
		this.virtual = original.virtual;
	}
	
	public ModelAttribute getCopy() {
		return new ModelAttribute(this, model);
	}
	
	public ModelAttribute getCopy(ModelDefinition model) {
		return new ModelAttribute(this, model);
	}
	
	public String getJavaType() {
		if(Text.class.getCanonicalName().equals(type)) {
			return "java.lang.String";
		}
		if(Binary.class.getCanonicalName().equals(type)) {
			return "byte[]";
		}
		return type;
	}

	public String getSimpleType() {
		return simpleName(type);
	}

	public boolean isPrimitive() {
		return (type.indexOf('.') == -1) && !type.endsWith("[]");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('@').append(Attribute.class.getSimpleName()).append('(');
		sb.append("name=\"").append(name).append("\"");
		sb.append(", type=").append(getSimpleType()).append(".class");

		if(!blank(check)) {
			sb.append(", check=\"").append(check).append("\"");
		}
		if(!blank(init)) {
			sb.append(", init=\"").append(init).append("\"");
		}
		if(precision != 8) {
			sb.append(", precision=").append(precision);
		}
		if(scale != 2) {
			sb.append(", scale=").append(scale);
		}
		if(indexed) {
			sb.append(", indexed=true");
		}
		if(readOnly) {
			sb.append(", readOnly=true");
		}
		if(unique) {
			sb.append(", unique=true");
		}
		if(virtual) {
			sb.append(", virtual=true");
		}

		sb.append(')');
		return sb.toString();
	}
	
}
