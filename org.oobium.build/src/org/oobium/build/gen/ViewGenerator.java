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
package org.oobium.build.gen;

import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.camelCase;
import static org.oobium.utils.StringUtils.titleize;
import static org.oobium.utils.StringUtils.varName;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;

import org.oobium.build.gen.model.PropertyDescriptor;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.persist.Paginator;
import org.oobium.persist.Text;
import org.oobium.utils.StringUtils;

public class ViewGenerator {

	public static File createView(File folder, String name, String content) {
		String path = name;
		if(!path.endsWith(".esp")) {
			path = path + ".esp";
		}
		
		String simpleName = name.substring(name.lastIndexOf(File.separatorChar) + 1);
		
		StringBuilder sb = new StringBuilder();
		sb.append("title ").append(simpleName).append(" Page\n\n");
		if(content != null) {
			sb.append(content);
		}
		
		return writeFile(new File(folder, path), sb.toString());
	}
	
	public LinkedHashMap<String, PropertyDescriptor> properties;
	private String mPkg;
	private String mType;
	private String mVar;
	private String mVarPlural;

	public ViewGenerator(ModelDefinition model) {
		properties = new LinkedHashMap<String, PropertyDescriptor>();
		for(ModelAttribute attribute : model.attributes().values()) {
			properties.put(attribute.getName(), new PropertyDescriptor(attribute));
		}
		for(ModelRelation relation : model.relations().values()) {
			properties.put(relation.getName(), new PropertyDescriptor(relation));
		}

		mPkg = model.getPackageName();
		mType = model.getSimpleName();
		mVar = varName(mType);
		mVarPlural = varName(mType, true);
	}

	public ViewGenerator(String mType) {
		this.mType = mType;
	}

	private void generateLabelAndField(StringBuilder sb, PropertyDescriptor property, String var) {
		String ftype = property.fullType();
		if(Boolean.class.getCanonicalName().equals(ftype)) {
			sb.append("\t\tcheck(").append(var).append(")\n");
			sb.append("\t\tlabel(").append(var).append(")\n");
		} else if(java.sql.Date.class.getCanonicalName().equals(ftype)) {
			sb.append("\t\tdiv <- label(").append(var).append(")\n");
			sb.append("\t\tdiv <- date(").append(var).append(")\n");
		} else {
			sb.append("\t\tdiv <- label(").append(var).append(")\n");
			sb.append("\t\tdiv <- ");
			if(String.class.getCanonicalName().equals(ftype)) {
				if("password".equalsIgnoreCase(var)) {
					sb.append("password");
				} else if(Text.class.getCanonicalName().equals(property.rawType())) {
					sb.append("textArea");
				} else {
					sb.append("text");
				}
			} else if(property.hasOne()) {
				sb.append("select");
			} else if(Integer.class.getCanonicalName().equals(ftype)) {
				sb.append("number");
			} else if(Double.class.getCanonicalName().equals(ftype)) {
				sb.append("number");
			} else if(property.hasMany()) {
				sb.append("span hasMany");
			} else if(Date.class.getCanonicalName().equals(ftype) ||
					Timestamp.class.getCanonicalName().equals(ftype)) {
				sb.append("date");
			} else {
				sb.append("input");
			}
			sb.append("(").append(var).append(')');
			if(property.hasOne()) {
				String type = StringUtils.simpleName(property.relatedType());
				sb.append(" <- options(").append(type).append(".findAll())\n");
			} else {
				sb.append('\n');
			}
		}
	}
	
	public String generateForm() {
		StringBuilder sb = new StringBuilder();

		sb.append("import ").append(mPkg).append(".*\n");
		sb.append('\n');
		sb.append(mType).append("Form(").append(mType).append(' ').append(mVar).append(')').append('\n');
		sb.append('\n');
		sb.append("form(").append(mVar).append(")\n");
		sb.append("\terrors\n");
		for(PropertyDescriptor property : properties.values()) {
			String var = property.variable();
			if("createdAt".equals(var) || "updatedAt".equals(var) || "createdOn".equals(var) || "updatedOn".equals(var)) {
				continue; // not normally user-editable fields
			}
			sb.append("\tdiv.field\n");
			generateLabelAndField(sb, property, var);
		}
		sb.append("\tdiv.actions\n");
		sb.append("\t\tsubmit\n");

		return sb.toString();
	}

	public String generateLayout() {
		StringBuilder sb = new StringBuilder();

		sb.append("title ").append(mType).append(": { titleize(getChild().getClass().getSimpleName()) }\n");
		sb.append('\n');
		sb.append("head\n");
		sb.append("\tscript(defaults)\n");
		sb.append("\tstyle(defaults)\n");
		sb.append('\n');
		sb.append("div <- yield\n");

		return sb.toString();
	}

	public String generateShowAllView() {
		StringBuilder sb = new StringBuilder();

		sb.append("import java.util.Collection\n");
		sb.append("import ").append(Paginator.class.getCanonicalName()).append('\n');
		sb.append("import ").append(mPkg).append(".*\n");
		sb.append('\n');
		sb.append("ShowAll").append(camelCase(mVarPlural)).append("(Collection<").append(mType).append("> ").append(mVarPlural).append(")\n");
		sb.append("ShowAll").append(camelCase(mVarPlural)).append("(Paginator<").append(mType).append("> paginator)\n");
		sb.append('\n');
		sb.append("h1 Listing ").append(mVarPlural).append('\n');
		sb.append('\n');
		sb.append("table\n");
		sb.append("\ttr\n");
		for(PropertyDescriptor property : properties.values()) {
			sb.append("\t\tth ").append(titleize(property.variable())).append('\n');
		}
		sb.append("\t- for(").append(mType).append(" ").append(mVar).append(" : ").append(mVarPlural).append(") {\n");
		sb.append("\t\ttr\n");
		for(PropertyDescriptor property : properties.values()) {
			sb.append("\t\t\ttd { ").append(mVar).append(".").append(property.getterName()).append("() }\n");
		}
		sb.append("\t\t\ttd <- a(").append(mVar).append(", show) Show\n");
		sb.append("\t\t\ttd <- a(").append(mVar).append(", showEdit) Edit\n");
		sb.append("\t\t\ttd <- a(").append(mVar).append(", destroy, confirm: \"Are you sure?\") Delete\n");
		sb.append("\t- }\n");
		sb.append('\n');
		sb.append("br\n");
		sb.append('\n');
		sb.append("a(").append(mType).append(".class, showNew) New ").append(mType).append('\n');

		return sb.toString();
	}
	
	public String generateShowEditView() {
		StringBuilder sb = new StringBuilder();

		sb.append("import ").append(mPkg).append(".*\n");
		sb.append('\n');
		sb.append("ShowEdit").append(mType).append('(').append(mType).append(' ').append(mVar).append(')').append('\n');
		sb.append('\n');
		sb.append("h1 Editing ").append(mVar).append('\n');
		sb.append('\n');
		sb.append("view<").append(mType).append("Form>(").append(mVar).append(")\n");
		sb.append('\n');
		sb.append("a(").append(mVar).append(", show) Show\n");
		sb.append("span  | \n");
		sb.append("a(").append(mType).append(".class, showAll) Back\n");

		return sb.toString();
	}
	
	public String generateShowNewView() {
		StringBuilder sb = new StringBuilder();

		sb.append("import ").append(mPkg).append(".*\n");
		sb.append('\n');
		sb.append("ShowNew").append(mType).append('(').append(mType).append(' ').append(mVar).append(')').append('\n');
		sb.append('\n');
		sb.append("h1 New ").append(mVar).append('\n');
		sb.append('\n');
		sb.append("view<").append(mType).append("Form>(").append(mVar).append(")\n");
		sb.append('\n');
		sb.append("a(").append(mType).append(".class, showAll) Back\n");

		return sb.toString();
	}

	public String generateShowView() {
		StringBuilder sb = new StringBuilder();

		sb.append("import ").append(mPkg).append(".*\n");
		sb.append('\n');
		sb.append("Show").append(mType).append('(').append(mType).append(' ').append(mVar).append(')').append('\n');
		sb.append('\n');
		for(PropertyDescriptor property : properties.values()) {
			sb.append("p\n");
			sb.append("\tb ").append(titleize(property.variable())).append(":\n");
			sb.append("\t+  { ").append(mVar).append(".").append(property.getterName()).append("() }\n");
		}
		sb.append('\n');
		sb.append("a(").append(mVar).append(", showEdit) Edit\n");
		sb.append("span  | \n");
		sb.append("a(").append(mType).append(".class, showAll) Back\n");

		return sb.toString();
	}

}
