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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class HasManyBuilder extends PropertyBuilder {

	private Class<?> retType = Set.class;

	private String retTypeStr;
	
	public HasManyBuilder(PropertyDescriptor descriptor) {
		super(descriptor);
		retTypeStr = retType.getSimpleName() + "<" + descriptor.type() + ">";
	}

	@Override
	public Map<String, String> getDeclarations() {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put(descriptor.variable(), "protected " + retTypeStr + " " + descriptor.variable() + " = null;");
		return vars;
	}
	
	private String getGetterMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("@SuppressWarnings(\"unchecked\")\n");
		sb.append("public ").append(retTypeStr).append(' ').append(descriptor.getterName()).append("() {\n");
		sb.append("\treturn (").append(retTypeStr).append(") get(").append(descriptor.enumProp()).append(");\n");
		sb.append("}");
		return sb.toString();
	}

	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> imports = new ArrayList<String>();
		imports.add(descriptor.fullType());
		imports.add(retType.getCanonicalName());
		imports.add(SQLException.class.getCanonicalName());
		return imports;
	}

	@Override
	public Map<String, String> getMethods() {
		Map<String, String> methods = new HashMap<String, String>();
		methods.put(descriptor.getterName(), getGetterMethod());
		return methods;
	}

}
