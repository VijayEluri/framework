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
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.simpleName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.lang.reflect.Field;
import java.util.Map;

import org.oobium.persist.Relation;

public class ModelRelation {

	public final ModelDefinition model;

	public final boolean hasMany;
	public int limit;
	public String name;
	public String type;
	public String opposite;
	public String through;
	public boolean readOnly;
	public boolean unique;
	public boolean virtual;
	public int dependent;
	public int onDelete;
	public int onUpdate;
	public String embed;
	public boolean embedded;
	public boolean include;
	private ModelRelation oppositeRelation;
	
	public ModelRelation(ModelDefinition model, String annotation, boolean hasMany) {
		this.model = model;
		this.hasMany = hasMany;

		char[] ca = annotation.toCharArray();
		int start = annotation.indexOf('(') + 1;
		int end = annotation.length() - 1;
		Map<String, String> entries = getJavaEntries(ca, start, end);
		
		this.name = getString(entries.get("name"));
		this.type = model.getType(entries.get("type"));
		this.limit = coerce(entries.get("limit"), -1);
		this.opposite = getString(entries.get("opposite"));
		this.through = getString(entries.get("through"));
		this.readOnly = coerce(entries.get("readOnly"), false);
		this.unique = coerce(entries.get("unique"), false);
		this.virtual = coerce(entries.get("virtual"), false);
		this.dependent = getReferential(entries.get("dependent"));
		this.onDelete = getReferential(entries.get("onDelete"));
		this.onUpdate = getReferential(entries.get("onUpdate"));
		this.embed = getString(entries.get("embed"));
		this.embedded = coerce(entries.get("embedded"), false);
		this.include = coerce(entries.get("include"), false);
	}

	private ModelRelation(ModelRelation original, ModelDefinition model, boolean hasMany) {
		this.model = model;
		this.hasMany = hasMany;
		this.name = original.name;
		this.type = original.type;
		this.limit = original.limit;
		this.opposite = original.opposite;
		this.through = original.through;
		this.readOnly = original.readOnly;
		this.unique = original.unique;
		this.virtual = original.virtual;
		this.dependent = original.dependent;
		this.onDelete = original.onDelete;
		this.onUpdate = original.onUpdate;
		this.embed = original.embed;
		this.embedded = original.embedded;
		this.include = original.include;
	}
	
	public ModelRelation getCopy() {
		return new ModelRelation(this, model, hasMany);
	}
	
	public ModelRelation getCopy(boolean hasMany) {
		return new ModelRelation(this, model, hasMany);
	}
	
	public ModelRelation getCopy(ModelDefinition model) {
		return new ModelRelation(this, model, hasMany);
	}
	
	public ModelRelation getCopy(ModelDefinition model, boolean hasMany) {
		return new ModelRelation(this, model, hasMany);
	}
	
	public ModelRelation getOpposite() {
		return oppositeRelation;
	}
	
	private int getReferential(String referential) {
		try {
			return coerce(referential, Relation.UNDEFINED);
		} catch(Exception e) {
			String constant;
			String type;
			int ix = referential.lastIndexOf('.');
			if(ix == -1) {
				constant = referential;
				type = model.getType(constant);
			} else {
				constant = referential.substring(ix+1);
				type = model.getType(referential.substring(0, ix));
			}
			try {
				Class<?> c = Class.forName(type);
				Field f = c.getField(constant);
				return f.getInt(c);
			} catch(Exception e2) {
				return Relation.UNDEFINED;
			}
		}
	}

	public String getSimpleType() {
		return simpleName(type);
	}

	public boolean hasOpposite() {
		return opposite != null && opposite.length() > 0;
	}
	
	public boolean isThrough() {
		return !blank(through);
	}

	public boolean isUnique() {
		return unique;
	}

	public boolean isVirtual() {
		return virtual;
	}

	void setOpposite(ModelDefinition[] models) {
		if(hasOpposite() && oppositeRelation == null) {
			for(ModelDefinition model : models) {
				if(type.equals(model.getCanonicalName())) {
					oppositeRelation = model.getRelation(opposite);
					if(oppositeRelation != null) {
						oppositeRelation.oppositeRelation = this;
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('@').append(Relation.class.getSimpleName()).append('(');
		sb.append("name=\"").append(name).append("\"");
		sb.append(", type=").append(getSimpleType()).append(".class");
		if(isThrough()) {
			sb.append(", through=\"").append(through).append('"');
		} else {
			if(hasOpposite()) {
				sb.append(", opposite=\"").append(opposite).append('"');
			}
			if(embedded) {
				sb.append(", embedded=true");
			} else if(!blank(embed)) {
				sb.append(", embed=\"").append(embed).append('"');
			}
			if(readOnly) {
				sb.append(", readOnly=true");
			}
		}
		if(limit != -1) {
			sb.append(", limit=").append(limit);
		}
		if(unique) {
			sb.append(", unique=true");
		}
		if(virtual) {
			sb.append(", virtual=true");
		}
		if(dependent != -1) {
			sb.append(", dependent=").append(dependent);
		}
		if(onDelete != -1) {
			sb.append(", onDelete=").append(onDelete);
		}
		if(onUpdate != -1) {
			sb.append(", onUpdate=").append(onUpdate);
		}
		if(include) {
			sb.append(", include=true");
		}
		sb.append(')');
		return sb.toString();
	}

}
