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
package org.oobium.build.model;

import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.simpleName;
import static org.oobium.utils.StringUtils.varName;

import java.util.Collection;

import javax.lang.model.type.MirroredTypeException;

import org.oobium.persist.Relation;


public class ModelRelation {

	private ModelDefinition model;
	private Relation annotation;
	private boolean hasMany;
	private ModelRelation opposite;

	public ModelRelation(ModelDefinition model, Relation annotation, boolean hasMany) {
		this.model = model;
		this.annotation = annotation;
		this.hasMany = hasMany;
	}

	public int getLimit() {
		return annotation.limit();
	}

	public ModelDefinition getModel() {
		return model;
	}

	public String getName() {
		String name = annotation.name();
		if(name != null && name.length() > 0) {
			return name;
		}
		name = simpleName(getType());
		return varName(name, hasMany());
	}

	public ModelRelation getOpposite() {
		return opposite;
	}

	public String getSimpleType() {
		String type = getType();
		return type.substring(type.lastIndexOf('.')+1, type.length());
	}

	public String getType() {
		try {
			return annotation.type().getCanonicalName();
		} catch(MirroredTypeException e1) {
			return e1.getTypeMirror().toString();
		}
	}
	
	public boolean hasMany() {
		return hasMany;
	}
	
	public boolean hasOpposite() {
		return annotation.opposite() != null && annotation.opposite().length() > 0;
	}
	
	public boolean isReadOnly() {
		return annotation.readOnly();
	}

	public boolean isRequired() {
		return annotation.required();
	}
	
	public boolean isThrough() {
		return !blank(annotation.through());
	}
	
	public boolean isUnique() {
		return annotation.unique();
	}

	public boolean isVirtual() {
		return annotation.virtual();
	}

	public int onDelete() {
		return annotation.onDelete();
	}

	public int onUpdate() {
		return annotation.onUpdate();
	}

	void setOpposite(Collection<ModelDefinition> models) {
		if(hasOpposite() && opposite == null) {
			String type = getType();
			for(ModelDefinition model : models) {
				if(type.equals(model.getCanonicalName())) {
					opposite = model.getRelation(annotation.opposite());
					if(opposite != null) {
						opposite.opposite = this;
					}
				}
			}
		}
	}

}
