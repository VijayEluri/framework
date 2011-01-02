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
package org.oobium.build.esp;

import static org.oobium.utils.StringUtils.blank;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ESourceFile {
	
	public static class EspLocation {
		int offset;
		int length;
		EspPart part;
		public EspLocation(int offset, EspPart part) { this.offset = offset; this.part = part; this.length = this.part.getLength(); }
		public String toString() { return "offset: " + offset + ", part: " + part; }
	}
	
	public static class JavaSourcePart implements Comparable<JavaSourcePart> {
		String source;
		EspLocation[] locations;
		public JavaSourcePart(String source) { this.source = source; }
		public JavaSourcePart(String source, EspLocation location) { this.source = source; this.locations = new EspLocation[] { location }; }
		public JavaSourcePart(String source, EspPart part) { this.source = source; this.locations = new EspLocation[] { new EspLocation(0, part) }; }
		public JavaSourcePart(String source, List<EspLocation> locations) { this.source = source; this.locations = locations.toArray(new EspLocation[locations.size()]); }
		public String toString() { return source; }
		public int compareTo(JavaSourcePart jsp) { return source.compareTo(jsp.source); }
	}

	
	private String source;
	private List<EspLocation> locations;
	
	private String simpleName;
	private String packageName;
	private String superName;

	private TreeMap<Integer, JavaSourcePart> classAnnotations = new TreeMap<Integer, JavaSourcePart>();
	private TreeMap<String, JavaSourcePart> staticImports = new TreeMap<String, JavaSourcePart>();
	private TreeMap<String, JavaSourcePart> imports = new TreeMap<String, JavaSourcePart>();
	private TreeMap<String, JavaSourcePart> variables = new TreeMap<String, JavaSourcePart>();
	private TreeMap<Integer, JavaSourcePart> constructors = new TreeMap<Integer, JavaSourcePart>();
	private TreeMap<String, JavaSourcePart> methods = new TreeMap<String, JavaSourcePart>();


	public void addClassAnnotation(int index, JavaSourcePart source) {
		checkSource();
		classAnnotations.put(index, source);
	}
	
	public void addClassAnnotation(String source) {
		checkSource();
		classAnnotations.put(classAnnotations.size(), new JavaSourcePart(source));
	}
	
	public void addConstructor(int index, JavaSourcePart source) {
		checkSource();
		constructors.put(index, source);
	}
	
	public void addConstructor(JavaSourcePart source) {
		checkSource();
		constructors.put(constructors.size(), source);
	}
	
	public void addImport(JavaSourcePart source) {
		checkSource();
		String src = source.source;
		if(src.charAt(src.length()-1) == ';') {
			src = src.substring(0, src.length()-1);
		}
		imports.put(src, source);
	}
	
	public void addImport(String source) {
		checkSource();
		imports.put(source, new JavaSourcePart(source));
	}
	
	public void addMethod(String name, String source) {
		checkSource();
		methods.put(name, new JavaSourcePart(source));
	}
	
	public void addMethod(String name, String source, List<EspLocation> locations) {
		checkSource();
		methods.put(name, new JavaSourcePart(source, locations));
	}
	
	public void addStaticImport(JavaSourcePart source) {
		checkSource();
		staticImports.put(source.source, source);
	}
	
	public void addStaticImport(String source) {
		checkSource();
		staticImports.put(source, new JavaSourcePart(source));
	}
	
	public void addVariable(String name, JavaSourcePart source) {
		checkSource();
		variables.put(name, source);
	}

	public void addVariable(String name, String source) {
		checkSource();
		variables.put(name, new JavaSourcePart(source));
	}
	
	private void checkSource() {
		if(source != null) {
			throw new IllegalStateException("Source cannot be modified once being finalized");
		}
	}

	private void addLocations(int offset, JavaSourcePart part) {
		if(!blank(part.locations)) {
			for(EspLocation location : part.locations) {
				location.offset += offset;
				locations.add(location);
			}
		}
	}
	
	public void finalizeSource() {
		locations = new ArrayList<ESourceFile.EspLocation>();
		StringBuilder sb = new StringBuilder();

		if(packageName != null) {
			sb.append("package ").append(packageName).append(";\n");
			sb.append('\n');
		}
		if(!staticImports.isEmpty()) {
			for(JavaSourcePart imp : staticImports.values()) {
				sb.append("import static ");
				addLocations(sb.length(), imp);
				sb.append(imp).append(";\n");
			}
			sb.append('\n');
		}
		if(!imports.isEmpty()) {
			for(JavaSourcePart imp : imports.values()) {
				sb.append("import ");
				addLocations(sb.length(), imp);
				sb.append(imp).append(';').append('\n');
			}
			sb.append('\n');
		}
		for(JavaSourcePart ann : classAnnotations.values()) {
			addLocations(sb.length(), ann);
			sb.append(ann).append('\n');
		}
		sb.append("public class ").append(simpleName);
		if(superName != null) {
			sb.append(" extends ").append(superName);
		}
		sb.append(" {\n");
		if(!variables.isEmpty()) {
			sb.append('\n');
			for(JavaSourcePart var : variables.values()) {
				sb.append("\t");
				if(var.source.startsWith("public ") || var.source.startsWith("protected ") || var.source.startsWith("private ")) {
					addLocations(sb.length(), var);
					sb.append(var);
				} else {
					sb.append("private ");
					addLocations(sb.length(), var);
					sb.append(var);
				}
				if(!var.source.endsWith(";")) {
					sb.append(';');
				}
				sb.append('\n');
			}
		}
		for(JavaSourcePart constructor : constructors.values()) {
			sb.append('\n');
			addLocations(sb.length(), constructor);
			sb.append(constructor).append('\n');
		}
		for(JavaSourcePart method : methods.values()) {
			sb.append('\n');
			addLocations(sb.length(), method);
			sb.append(method).append('\n');
		}
		sb.append("\n}");

		source = sb.toString();
	}
	
	public String getCanonicalName() {
		return packageName + "." + simpleName;
	}
	
	public String getConstructor(int index) {
		JavaSourcePart part = constructors.get(index);
		if(part != null) {
			return part.source;
		}
		return null;
	}
	
	public int getConstructorCount() {
		return constructors.size();
	}
	
	public String getFileName() {
		return simpleName + ".java";
	}
	
	public String getFilePath() {
		return getCanonicalName().replace('.', File.separatorChar) + ".java";
	}

	public int getEspOffset(int javaOffset) {
		for(EspLocation location : locations) {
			if(location.offset <= javaOffset && javaOffset < (location.offset + location.length)) {
				int offset = location.part.getStart();
				return Math.min(offset + (javaOffset - location.offset), location.part.getEnd());
			}
		}
		return -1;
	}
	
	public String getImport(String key) {
		JavaSourcePart part = imports.get(key);
		if(part != null) {
			return part.toString();
		}
		return null;
	}
	
	public int getJavaOffset(int espOffset) {
		for(EspLocation location : locations) {
			EspPart part = location.part;
			if(part.getStart() <= espOffset && espOffset < part.getEnd()) {
				int offset = location.offset;
				return offset + (espOffset - part.getStart());
			}
		}
		return -1;
	}
	
	public String getMethod(String name) {
		JavaSourcePart part = methods.get(name);
		if(part != null) {
			return part.source;
		}
		return null;
	}
	
	public int getMethodCount() {
		return methods.size();
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getSimpleName() {
		return simpleName;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getVariable(String name) {
		JavaSourcePart part = variables.get(name);
		if(part != null) {
			return part.source;
		}
		return null;
	}
	
	public boolean hasConstructor(int index) {
		return constructors.containsKey(index);
	}
	
	
	public boolean hasImport(String imp) {
		return imports.containsKey(imp);
	}
	
	public boolean hasMethod(String name) {
		return methods.containsKey(name);
	}

	public boolean hasStaticImport(String imp) {
		return staticImports.containsKey(imp);
	}
	
	public boolean hasVariable(String name) {
		return variables.containsKey(name);
	}
	
	public void setPackage(String name) {
		checkSource();
		packageName = name;
	}

	public void setSimpleName(String name) {
		checkSource();
		simpleName = name;
	}
	
	public void setSuperName(String name) {
		checkSource();
		superName = name;
	}
	
	@Override
	public String toString() {
		return super.toString() + " {" + getCanonicalName() + "}";
	}

}
