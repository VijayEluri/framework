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

import org.oobium.persist.ModelList;


public class HasManyBuilder extends PropertyBuilder {

	public HasManyBuilder(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	private String getGetterMethod() {
		String type = ModelList.class.getSimpleName() + "<" + descriptor.type() + ">";
		StringBuilder sb = new StringBuilder();
		sb.append("@SuppressWarnings(\"unchecked\")\n");
		sb.append("public ").append(type).append(' ').append(descriptor.getterName()).append("() {\n");
		sb.append("\treturn (").append(type).append(") get(").append(descriptor.enumProp()).append(");\n");
		sb.append("}\n");
		sb.append("\n");
		sb.append("public ").append(type).append(' ').append(descriptor.getterName()).append("(String include) {\n");
		sb.append("\tif(include.startsWith(\"include:\")) include = include.substring(8);\n");
		sb.append("\tload(").append(descriptor.enumProp()).append(" + \":\" + include);\n");
		sb.append("\treturn ").append(descriptor.getterName()).append("();\n");
		sb.append("}");
		return sb.toString();
	}

	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> imports = new ArrayList<String>();
		imports.add(descriptor.fullType());
		imports.add(ModelList.class.getCanonicalName());
		return imports;
	}

	@Override
	public Map<String, String> getMethods() {
		Map<String, String> methods = new HashMap<String, String>();
		methods.put(descriptor.getterName(), getGetterMethod());
		return methods;
	}

}
