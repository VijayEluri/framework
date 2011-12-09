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
import static org.oobium.utils.StringUtils.varName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.build.model.ModelRelation;
import org.oobium.persist.ModelList;


public class HasManyBuilder extends PropertyBuilder {

	public HasManyBuilder(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	private String getGetterMethod() {
		StringBuilder sb = new StringBuilder();
		if(descriptor.isThrough()) {
			String[] sa = descriptor.through();
			ModelRelation relation = descriptor.model().getRelation(sa[0]);
			if(relation != null) {
				String type = List.class.getSimpleName() + "<" + descriptor.type() + ">";
				String ttype = relation.getSimpleType();
				sb.append("public ").append(type).append(' ').append(descriptor.getterName()).append("() {\n");
				sb.append("\treturn ").append(descriptor.getterName()).append("(null);\n");
				sb.append("}\n");
				sb.append("\n");
				sb.append("public ").append(type).append(' ').append(descriptor.getterName()).append("(String include) {\n");
				sb.append("\tif(include == null) {\n");
				sb.append("\t\tinclude = \"").append(sa[1]).append("\";\n");
				sb.append("\t} else {\n");
				sb.append("\t\tif(include.startsWith(\"include:\")) include = include.substring(8);\n");
				sb.append("\t\tinclude = \"").append(sa[1]).append(":\" + include;\n");
				sb.append("\t}\n");

				String tvar;
				if(relation.hasMany()) {
					tvar = varName(ttype, true);
					sb.append("\tModelList<").append(ttype).append("> ").append(tvar).append(" = ").append(tvar).append("(include);\n");
				} else {
					tvar = varName(ttype);
					String tgetter = getterName(sa[0]);
					sb.append("\t").append(ttype).append(' ').append(tvar).append(" = ").append(tgetter).append("(include);\n");
				}
				
				sb.append("\treturn ModelUtils.collectHasManyThrough(").append(tvar).append(", \"").append(sa[1]).append("\", ").append(descriptor.type()).append(".class);\n");
				sb.append("}");
			}
		}
		else {
			String type = ModelList.class.getSimpleName() + "<" + descriptor.type() + ">";
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
		}
		return sb.toString();
	}

	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> imports = new ArrayList<String>();
		imports.add(descriptor.fullType());
		imports.add(ModelList.class.getCanonicalName());
		if(descriptor.isThrough()) {
			imports.add(List.class.getCanonicalName());
		}
		return imports;
	}

	@Override
	public Map<String, String> getMethods() {
		Map<String, String> methods = new HashMap<String, String>();
		methods.put(descriptor.getterName(), getGetterMethod());
		return methods;
	}

}
