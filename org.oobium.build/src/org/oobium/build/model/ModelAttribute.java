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

import javax.lang.model.type.MirroredTypeException;

import org.oobium.persist.Attribute;


public class ModelAttribute {

	private ModelDefinition model;
	private Attribute annotation;

	public ModelAttribute(ModelDefinition model, Attribute annotation) {
		this.model = model;
		this.annotation = annotation;
	}

	public ModelDefinition getModel() {
		return model;
	}
	
	public String getCheck() {
		return annotation.check();
	}
	
	public String getInit() {
		return annotation.init();
	}
	
	public String getName() {
		return annotation.name();
	}

	public int getPrecision() {
		return annotation.precision();
	}
	
	public int getScale() {
		return annotation.scale();
	}

	public String getType() {
		try {
			return annotation.type().getCanonicalName();
		} catch(MirroredTypeException e) {
			return e.getTypeMirror().toString();
		}
	}

	public String getJavaType() {
		String type = getType();
		if("org.oobium.persist.Text".equals(type)) {
			return "java.lang.String";
		}
		return type;
	}

	public boolean isIndex() {
		return annotation.indexed();
	}
	
	public boolean isPrimitive() {
		String type = getType();
		return (type.indexOf('.') == -1) && !type.endsWith("[]");
	}
	
	public boolean isRequired() {
		return annotation.required();
	}
	
	public boolean isReadOnly() {
		return annotation.readOnly();
	}

	public boolean isUnique() {
		return annotation.unique();
	}
	
	public boolean isVirtual() {
		return annotation.virtual();
	}
	
}
