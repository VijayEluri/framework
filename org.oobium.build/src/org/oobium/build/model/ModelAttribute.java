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

import static org.oobium.build.model.ModelDefinition.getJavaEntries;
import static org.oobium.build.model.ModelDefinition.getString;
import static org.oobium.persist.Attribute.DEFAULT_CHECK;
import static org.oobium.persist.Attribute.DEFAULT_INDEXED;
import static org.oobium.persist.Attribute.DEFAULT_INIT;
import static org.oobium.persist.Attribute.DEFAULT_JSON;
import static org.oobium.persist.Attribute.DEFAULT_PRECISION;
import static org.oobium.persist.Attribute.DEFAULT_READONLY;
import static org.oobium.persist.Attribute.DEFAULT_SCALE;
import static org.oobium.persist.Attribute.DEFAULT_UNIQUE;
import static org.oobium.persist.Attribute.DEFAULT_VIRTUAL;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.simpleName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.HashMap;
import java.util.Map;

import org.oobium.persist.Attribute;
import org.oobium.persist.Binary;
import org.oobium.persist.Text;


public class ModelAttribute {

	private final ModelDefinition model;
	private String check;
	private String init;
	private String name;
	private boolean json;
	private int precision;
	private int scale;
	private String type;
	private boolean indexed;
	private boolean readOnly;
	private boolean unique;
	private boolean virtual;
	
	private ModelAttribute(ModelAttribute original, ModelDefinition model) {
		this.model = model;
		check(original.check);
		init(original.init);
		name(original.name);
		precision(original.precision);
		scale(original.scale);
		type(original.type);
		json(original.json);
		indexed(original.indexed);
		readOnly(original.readOnly);
		unique(original.unique);
		virtual(original.virtual);
	}
	
	public ModelAttribute(ModelDefinition model, String annotation) {
		this.model = model;

		if("createdAt".equals(annotation) || "updatedAt".equals(annotation) || "createdOn".equals(annotation) || "updatedOn".equals(annotation)) {
			name(annotation);
			if("createdAt".equals(annotation) || "updatedAt".equals(annotation)) {
				type("java.util.Date");
			} else {
				type("java.sql.Date");
			}
			json(DEFAULT_JSON);
			check(DEFAULT_CHECK);
			init(DEFAULT_INIT);
			precision(DEFAULT_PRECISION);
			scale(DEFAULT_SCALE);
			indexed(DEFAULT_INDEXED);
			readOnly(DEFAULT_READONLY);
			unique(DEFAULT_UNIQUE);
			virtual(DEFAULT_VIRTUAL);
		} else {
			char[] ca = annotation.toCharArray();
			int start = annotation.indexOf('(') + 1;
			int end = annotation.length() - 1;
			Map<String, String> entries = getJavaEntries(ca, start, end);
			
			name(getString(entries.get("name")));
			type(model.getType(entries.get("type")));
			json(coerce(entries.get("json"), DEFAULT_JSON));
			check(getString(entries.get("check")));
			init(getString(entries.get("init")));
			precision(coerce(entries.get("precision"), DEFAULT_PRECISION));
			scale(coerce(entries.get("scale"), DEFAULT_SCALE));
			indexed(coerce(entries.get("indexed"), DEFAULT_INDEXED));
			readOnly(coerce(entries.get("readOnly"), DEFAULT_READONLY));
			unique(coerce(entries.get("unique"), DEFAULT_UNIQUE));
			virtual(coerce(entries.get("virtual"), DEFAULT_VIRTUAL));
		}
	}
	
	public String check() {
		return check;
	}
	
	public ModelAttribute check(String check) {
		this.check = (check == null) ? DEFAULT_CHECK : check;
		return this;
	}
	
	public ModelAttribute getCopy() {
		return new ModelAttribute(this, model);
	}

	public ModelAttribute getCopy(ModelDefinition model) {
		return new ModelAttribute(this, model);
	}

	public Map<String, Object> getCustomProperties() {
		// when updating this method, make sure to also update #hasCustomProperties()
		Map<String, Object> props = new HashMap<String, Object>();
		if(!check.equals(DEFAULT_CHECK)) {
			props.put("check", check);
		}
		if(!init.equals(DEFAULT_INIT)) {
			props.put("init", init);
		}
		if(json != DEFAULT_JSON) {
			props.put("json", json);
		}
		if(precision != DEFAULT_PRECISION) {
			props.put("precision", precision);
		}
		if(scale != DEFAULT_SCALE) {
			props.put("scale", scale);
		}
		if(indexed != DEFAULT_INDEXED) {
			props.put("indexed", indexed);
		}
		if(readOnly != DEFAULT_READONLY) {
			props.put("readOnly", readOnly);
		}
		if(unique != DEFAULT_UNIQUE) {
			props.put("unique", unique);
		}
		if(virtual != DEFAULT_VIRTUAL) {
			props.put("virtual", virtual);
		}
		return props;
	}
	
	public Map<String, Object> getProperties() {
		Map<String, Object> props = getCustomProperties();
		props.put("name", name);
		props.put("type", type);
		return props;
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

	public boolean hasCustomProperties() {
		// when updating this method, make sure to also update #getCustomProperties()
		if(!check.equals(DEFAULT_CHECK)) {
			return true;
		}
		if(!init.equals(DEFAULT_INIT)) {
			return true;
		}
		if(json != DEFAULT_JSON) {
			return true;
		}
		if(precision != DEFAULT_PRECISION) {
			return true;
		}
		if(scale != DEFAULT_SCALE) {
			return true;
		}
		if(indexed != DEFAULT_INDEXED) {
			return true;
		}
		if(readOnly != DEFAULT_READONLY) {
			return true;
		}
		if(unique != DEFAULT_UNIQUE) {
			return true;
		}
		if(virtual != DEFAULT_VIRTUAL) {
			return true;
		}
		return false;
	}
	
	public boolean indexed() {
		return indexed;
	}

	public ModelAttribute indexed(boolean indexed) {
		this.indexed = indexed;
		return this;
	}

	public String init() {
		return init;
	}

	public ModelAttribute init(String init) {
		this.init = (init == null) ? DEFAULT_INIT : init;
		return this;
	}

	public boolean isPrimitive() {
		return (type.indexOf('.') == -1) && !type.endsWith("[]");
	}

	public boolean json() {
		return json;
	}

	public ModelAttribute json(boolean json) {
		this.json = json;
		return this;
	}

	public ModelDefinition model() {
		return model;
	}

	public String name() {
		return name;
	}

	public ModelAttribute name(String name) {
		if(name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		this.name = name;
		return this;
	}

	public int precision() {
		return precision;
	}

	public ModelAttribute precision(int precision) {
		this.precision = precision;
		return this;
	}

	public boolean readOnly() {
		return readOnly;
	}

	public ModelAttribute readOnly(boolean readOnly) {
		this.readOnly = readOnly;
		return this;
	}

	public int scale() {
		return scale;
	}

	public ModelAttribute scale(int scale) {
		this.scale = scale;
		return this;
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
		if(!json) {
			sb.append(", json=false");
		}

		sb.append(')');
		return sb.toString();
	}

	public String type() {
		return type;
	}

	public ModelAttribute type(String type) {
		if(type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		this.type = type;
		return this;
	}

	public boolean unique() {
		return unique;
	}

	public ModelAttribute unique(boolean unique) {
		this.unique = unique;
		return this;
	}

	public boolean virtual() {
		return virtual;
	}

	public ModelAttribute virtual(boolean virtual) {
		this.virtual = virtual;
		return this;
	}

	
}
