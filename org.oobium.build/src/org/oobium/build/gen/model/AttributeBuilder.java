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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.oobium.utils.StringUtils;


public class AttributeBuilder extends PropertyBuilder {

	public AttributeBuilder(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	private String getGetterMethod() {
		StringBuilder sb = new StringBuilder();
		if("Map".equals(descriptor.type())) {
			sb.append("@SuppressWarnings(\"unchecked\")\n");
			sb.append("public Map<String, Object> ").append(descriptor.getterName()).append("() {\n");
			sb.append("\treturn (Map<String, Object>) get(").append(descriptor.enumProp()).append(", Map.class);\n");
			sb.append("}");
		}
		else {
			sb.append("public ").append(descriptor.type()).append(' ').append(descriptor.getterName()).append("() {\n");
			sb.append("\treturn get(").append(descriptor.enumProp()).append(", ").append(descriptor.type()).append(".class);\n");
			sb.append("}");
			if("Date".equals(descriptor.type())) {
				sb.append("\n\n");
				sb.append("public String ").append(descriptor.getterName()).append("(String format) {\n");
				sb.append("\tDate date = ").append(descriptor.getterName()).append("();\n");
				sb.append("\tif(date != null) {\n");
				sb.append("\t\tSimpleDateFormat sdf = new SimpleDateFormat(format);\n");
				sb.append("\t\treturn sdf.format(date);\n");
				sb.append("\t}\n");
				sb.append("\treturn null;\n");
				sb.append("}");
			}
		}
		return sb.toString();
	}
	
	private String getHasserMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("public boolean ").append(descriptor.hasserName()).append("() {\n");
		if("String".equals(descriptor.type())) {
			sb.append("\treturn !StringUtils.blank(get(").append(descriptor.enumProp()).append("));\n");
		} else {
			sb.append("\treturn get(").append(descriptor.enumProp()).append(") != null;\n");
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> imports = new ArrayList<String>();
		if(descriptor.hasImport()) {
			String fullType = descriptor.fullType();
			imports.add(fullType);
		}
		if("String".equals(descriptor.type())) {
			imports.add(StringUtils.class.getCanonicalName());
		}
		if("Date".equals(descriptor.type())) {
			imports.add(SimpleDateFormat.class.getCanonicalName());
		}
		return imports;
	}
	
	private String getIsMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("public boolean ").append(getterName(descriptor.variable(), true)).append("() {\n");
		sb.append("\treturn Boolean.TRUE.equals(").append(descriptor.getterName()).append("());\n");
		sb.append("}");
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
		String varType = "Map".equals(descriptor.type()) ? "Map<String, Object>" : descriptor.type();
		StringBuilder sb = new StringBuilder();
		sb.append("public ").append(type).append(' ').append(descriptor.setterName()).append("(").append(varType).append(" ").append(var).append(") {\n");
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
