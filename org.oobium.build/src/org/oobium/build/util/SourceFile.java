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
package org.oobium.build.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.oobium.build.gen.model.PropertyDescriptor;


public class SourceFile {
	
	public enum ClassModifier {
		DEFAULT, PUBLIC, PROTECTED, PRIVATE;
		public String getModifier() {
			if(this == DEFAULT) {
				return "";
			}
			return name().toLowerCase();
		}
		public boolean isDefault() {
			return this == DEFAULT;
		}
	}

	public static int closer(StringBuilder sb, int start) {
		if(start >= 0 && start < sb.length()) {
			char opener = sb.charAt(start);
			char closer = closerChar(opener);
			int count = 1;
			for(int i = start+1; i >= 0 && i < sb.length(); i++) {
				char c0 = sb.charAt(i-1);
				char c1 = sb.charAt(i);
				if(c1 == opener && c1 != closer) {
					count++;
				} else if(c1 == closer) {
					if(closer != '"' || c0 != '\\') { // check for escape char
						count--;
						if(count == 0) {
							return i;
						}
					}
				} else if(c1 == '"') { // string
					for(i++; i < sb.length(); i++) {
						if(sb.charAt(i-1) != '\\' && sb.charAt(i) == '"') {
							break;
						}
					}
				} else if(sb.charAt(i-1) == '/' && sb.charAt(i) == '/') { // java line comment
					for(i++; i < sb.length(); i++) {
						if(sb.charAt(i) == '\n') {
							break;
						}
					}
				} else if(sb.charAt(i-1) == '/' && sb.charAt(i) == '*') { // java multiline comment
					for(i++; i < sb.length(); i++) {
						if(sb.charAt(i-1) == '*' && sb.charAt(i) == '/') {
							break;
						}
					}
				}
			}
		}
		return -1;
	}
	
	public static char closerChar(char c) {
		switch(c) {
			case '<': return '>';
			case '(': return ')';
			case '{': return '}';
			case '[': return ']';
			case '"': return '"';
			case '\'': return '\'';
		}
		return 0;
	}

	public static int find(StringBuilder sb, int from, int to, char...cs) {
		for(int i = from; i >= 0 && i < to && i < sb.length(); i++) {
			if(sb.charAt(i) == cs[0]) {
				boolean found = true;
				for(int j = 1; j < cs.length; j++) {
					if((i+j) == sb.length() || sb.charAt(i+j) != cs[j]) {
						found = false;
						break;
					}
				}
				if(found) {
					return i;
				}
			} else if(sb.charAt(i) == '(') {
				i = closer(sb, i);
				if(i == -1) {
					break;
				}
			} else if(sb.charAt(i) == '"') { // string
				for(i++; i < sb.length(); i++) {
					if(sb.charAt(i-1) != '\\' && sb.charAt(i) == '"') {
						break;
					}
				}
			} else if(sb.charAt(i) == '/' && sb.charAt(i+1) == '/') { // java line comment
				for(i+=2; i < sb.length(); i++) {
					if(sb.charAt(i) == '\n') {
						break;
					}
				}
			} else if(sb.charAt(i) == '<' && sb.charAt(i+1) == '*') { // java multiline comment
				for(i+=2; i < sb.length(); i++) {
					if(sb.charAt(i) == '*' && sb.charAt(i+1) == '>') {
						break;
					}
				}
			}
		}
		return -1;
	}

	public String simpleName;
	public String packageName;
	public String superName;
	public boolean isAbstract;
	public TreeSet<String> interfaces = new TreeSet<String>();
	public TreeMap<Integer, String> classAnnotations = new TreeMap<Integer, String>();
	public TreeSet<String> staticImports = new TreeSet<String>();
	public TreeSet<String> imports = new TreeSet<String>();
	public List<String> staticInitializers = new ArrayList<String>();
	public List<String> initializers = new ArrayList<String>();
	public TreeMap<String, String> staticMethods = new TreeMap<String, String>();
	public TreeMap<String, String> variables = new TreeMap<String, String>();
	public TreeMap<Integer, String> constructors = new TreeMap<Integer, String>();
	public TreeMap<String, String> methods = new TreeMap<String, String>();
	public TreeMap<String, PropertyDescriptor> properties = new TreeMap<String, PropertyDescriptor>();
	public boolean propertiesPrefix;

	public String rawSource;

	public ClassModifier classModifier = ClassModifier.PUBLIC;
	
	public String getCanonicalName() {
		return packageName + "." + simpleName;
	}
	
	public String getFileName() {
		return simpleName + ".java";
	}

	public String getFilePath() {
		return getCanonicalName().replace('.', File.separatorChar) + ".java";
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getSimpleName() {
		return simpleName;
	}
	
	public String toSource() {
		for(Iterator<String> iter = imports.iterator(); iter.hasNext(); ) {
			String imp = iter.next();
			if(imp.startsWith("static ")) {
				staticImports.add(imp.substring(7));
				iter.remove();
			}
		}
		
		StringBuilder sb = new StringBuilder();

		if(packageName != null) {
			sb.append("package ").append(packageName).append(";\n");
			sb.append('\n');
		}
		if(!staticImports.isEmpty()) {
			for(String imp : staticImports) {
				sb.append("import static ").append(imp).append(";\n");
			}
			sb.append('\n');
		}
		if(!imports.isEmpty()) {
			for(String imp : imports) {
				sb.append("import ").append(imp).append(";\n");
			}
			sb.append('\n');
		}
		for(String ann : classAnnotations.values()) {
			sb.append(ann).append('\n');
		}
		if(!classModifier.isDefault()) {
			sb.append(classModifier.getModifier()).append(' ');
		}
		if(isAbstract) {
			sb.append("abstract ");
		}
		sb.append("class ").append(simpleName);
		if(superName != null) {
			sb.append(" extends ").append(superName);
		}
		if(!interfaces.isEmpty()) {
			sb.append(" implements");
			for(String iface : interfaces) {
				sb.append(' ').append(iface);
			}
		}
		sb.append(" {\n");
		if(!properties.isEmpty()) {
			sb.append('\n');
			if(propertiesPrefix) {
				sb.append("\tpublic enum Field {");
				for(Iterator<PropertyDescriptor> i = properties.values().iterator(); i.hasNext();) {
					sb.append("\n\t\t");
					sb.append(i.next().enumProp());
					if(i.hasNext()) {
						sb.append(',');
					} else {
						sb.append("\n\t");
					}
				}
				sb.append("}\n");
			} else {
				for(PropertyDescriptor prop : properties.values()) {
					sb.append("\tpublic static final String ").append(prop.enumProp()).append(" = \"").append(prop.variable()).append("\";\n");
				}
			}
		}
		for(String method : staticMethods.values()) {
			sb.append('\n');
			sb.append(method).append('\n');
		}
		if(!staticInitializers.isEmpty()) {
			sb.append("\tstatic {\n");
			for(String init : staticInitializers) {
				sb.append("\t\t").append(init);
				if(!init.endsWith(";")) {
					sb.append(';');
				}
				sb.append('\n');
			}
			sb.append("\t}\n");
		}
		if(!variables.isEmpty()) {
			sb.append('\n');
			for(String var : variables.values()) {
				sb.append("\t");
				if(var.startsWith("public ") || var.startsWith("protected ") || var.startsWith("private ")) {
					sb.append(var);
				} else {
					sb.append("private ").append(var);
				}
				if(!var.endsWith(";")) {
					sb.append(';');
				}
				sb.append('\n');
			}
		}
		if(!initializers.isEmpty()) {
			sb.append("\n\t{\n");
			for(String init : initializers) {
				sb.append("\t\t").append(init);
				if(!init.endsWith(";")) {
					sb.append(';');
				}
				sb.append('\n');
			}
			sb.append("\t}\n");
		}
		if(rawSource != null && rawSource.length() > 0) {
			sb.append('\n');
			sb.append(rawSource);
		}
		for(String constructor : constructors.values()) {
			sb.append('\n');
			sb.append(constructor).append('\n');
		}
		for(String method : methods.values()) {
			sb.append('\n');
			sb.append(method).append('\n');
		}
		sb.append("\n}");

		return sb.toString();
	}
	
	@Override
	public String toString() {
		return super.toString() + " {" + getCanonicalName() + "}";
	}
	
}
