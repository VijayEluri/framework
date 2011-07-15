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
package org.oobium.build.gen.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HasOneBuilder extends PropertyBuilder {

	public HasOneBuilder(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Map<String, String> getDeclarations() {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put(descriptor.variable(), "protected " + descriptor.type() + " " + descriptor.variable() + " = null;");
		return vars;
	}
	
	private String getGetterMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("public ").append(descriptor.type()).append(' ').append(descriptor.getterName()).append("() {\n");
		sb.append("\treturn get(").append(descriptor.enumProp()).append(", ").append(descriptor.type()).append(".class);\n");
		sb.append("}");
		return sb.toString();
	}

	private String getHasserMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("public boolean ").append(descriptor.hasserName()).append("() {\n");
		sb.append("\treturn get(").append(descriptor.enumProp()).append(") != null;\n");
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> imports = new ArrayList<String>();
		if(descriptor.hasImport()) imports.add(descriptor.fullType());
		return imports;
	}

	@Override
	public Map<String, String> getMethods() {
		Map<String, String> methods = new HashMap<String, String>();
		methods.put(descriptor.hasserName(), getHasserMethod());
		methods.put(descriptor.getterName(), getGetterMethod());
		methods.put(descriptor.setterName(), getSetterMethod());
		return methods;
	}

	private String getSetterMethod() {
		String type = descriptor.modelType();
		String prop = descriptor.enumProp();
		String var = descriptor.variable();
		StringBuilder sb = new StringBuilder();
		sb.append("public ").append(type).append(' ').append(descriptor.setterName()).append("(").append(descriptor.type()).append(" ").append(var).append(") {\n");
		if(descriptor.hasCheck()) {
			sb.append("\tif((").append(descriptor.getCheck()).append(")) {\n");
			sb.append("\t\treturn set(").append(prop).append(", ").append(var).append(");\n");
			sb.append("\t}\n");
		} else {
			sb.append("\treturn set(").append(prop).append(", ").append(var).append(");\n");
		}
		sb.append("}");
		return sb.toString();
	}

}
