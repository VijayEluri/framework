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

import static org.oobium.utils.StringUtils.getterName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.oobium.utils.json.JsonUtils;


public class AttributeBuilder extends PropertyBuilder {

	public AttributeBuilder(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public Map<String, String> getDeclarations() {
		Map<String, String> vars = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		sb.append("protected ").append(descriptor.type()).append(' ').append(descriptor.variable());
		if(descriptor.hasInit()) {
			sb.append(" = ").append(descriptor.init());
		}
		sb.append(';');
		vars.put(descriptor.variable(), sb.toString());
		return vars;
	}

	private String getGetterMethod() {
		StringBuilder sb = new StringBuilder();
		if("Map".equals(descriptor.type())) {
			sb.append("\tpublic Map<String, String> ").append(descriptor.getterName()).append("() {\n");
			sb.append("\t\treturn JsonUtils.toStringMap(get(").append(descriptor.enumProp()).append(", String.class));\n");
			sb.append("\t}");
		} else {
			sb.append("\tpublic ").append(descriptor.type()).append(' ').append(descriptor.getterName()).append("() {\n");
			sb.append("\t\treturn get(").append(descriptor.enumProp()).append(", ").append(descriptor.type()).append(".class);\n");
			sb.append("\t}");
		}
		return sb.toString();
	}
	
	private String getHasserMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("\tpublic boolean ").append(descriptor.hasserName()).append("() {\n");
		sb.append("\t\treturn get(").append(descriptor.enumProp()).append(") != null;\n");
		sb.append("\t}");
		return sb.toString();
	}

	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> imports = new ArrayList<String>();
		if(descriptor.hasImport()) {
			String fullType = descriptor.fullType();
			imports.add(fullType);
			if("Map".equals(descriptor.type())) {
				imports.add(JsonUtils.class.getCanonicalName());
			}
		}
		return imports;
	}
	
	private String getIsMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("\tpublic boolean ").append(getterName(descriptor.variable(), true)).append("() {\n");
		sb.append("\t\treturn Boolean.TRUE.equals(").append(descriptor.getterName()).append("());\n");
		sb.append("\t}");
		return sb.toString();
	}

	@Override
	public Map<String, String> getMethods() {
		Map<String, String> methods = new HashMap<String, String>();
		methods.put(descriptor.hasserName(), getHasserMethod());
		methods.put(descriptor.getterName(), getGetterMethod());
		if("Boolean".equalsIgnoreCase(descriptor.type())) {
			methods.put(getterName(descriptor.variable(), true), getIsMethod());
		}
		if(!descriptor.isReadOnly()) {
			methods.put(descriptor.setterName(), getSetterMethod());
		}
		return methods;
	}

	private String getSetterMethod() {
		String type = descriptor.modelType();
		String prop = descriptor.enumProp();
		String var = descriptor.variable();
		String varType = "Map".equals(descriptor.type()) ? "Map<String, String>" : descriptor.type();
		StringBuilder sb = new StringBuilder();
		sb.append("\tpublic ").append(type).append(' ').append(descriptor.setterName()).append("(").append(varType).append(" ").append(var).append(") {\n");
		if(descriptor.hasCheck()) {
			sb.append("\t\tif((").append(descriptor.getCheck()).append(")) {\n");
			sb.append("\t\t\treturn set(").append(prop).append(", ").append(var).append(");\n");
			sb.append("\t\t}\n");
		} else {
			sb.append("\t\treturn set(").append(prop).append(", ").append(var).append(");\n");
		}
		sb.append("\t}");
		return sb.toString();
	}

}
