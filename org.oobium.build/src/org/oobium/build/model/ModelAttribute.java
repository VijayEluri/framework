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

import static org.oobium.utils.StringUtils.simpleName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.build.model.ModelDefinition.*;

import java.util.Map;

import org.oobium.persist.Binary;
import org.oobium.persist.Text;


public class ModelAttribute {

	public final ModelDefinition model;
	public final String check;
	public final String init;
	public final String name;
	public final int precision;
	public final int scale;
	public final String type;
	public final boolean indexed;
	public final boolean readOnly;
	public final boolean unique;
	public final boolean virtual;
	
	public ModelAttribute(ModelDefinition model, String annotation) {
		this.model = model;

		if("createdOn".equals(annotation) || "updatedOn".equals(annotation)) {
			this.name = annotation;
			this.type = "java.sql.Date";
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
	
}
