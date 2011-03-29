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
	
	public static class JavaSource implements Comparable<JavaSource> {
		String source;
		EspLocation[] locations;
		public JavaSource(String source) { this.source = source; }
		public JavaSource(String source, EspLocation location) { this.source = source; this.locations = new EspLocation[] { location }; }
		public JavaSource(String source, EspPart part) { this.source = source; this.locations = new EspLocation[] { new EspLocation(0, part) }; }
		public JavaSource(String source, List<EspLocation> locations) { this.source = source; this.locations = locations.toArray(new EspLocation[locations.size()]); }
		public int compareTo(JavaSource jsp) { return source.compareTo(jsp.source); }
		public String toString() { return source; }
	}

	
	private String source;
	private List<EspLocation> locations;
	
	private String simpleName;
	private String packageName;
	private String superName;

	private TreeMap<Integer, JavaSource> classAnnotations = new TreeMap<Integer, JavaSource>();
	private TreeMap<String, JavaSource> staticImports = new TreeMap<String, JavaSource>();
	private TreeMap<String, JavaSource> imports = new TreeMap<String, JavaSource>();
	private TreeMap<String, JavaSource> variables = new TreeMap<String, JavaSource>();
	private TreeMap<Integer, JavaSource> constructors = new TreeMap<Integer, JavaSource>();
	private TreeMap<String, JavaSource> methods = new TreeMap<String, JavaSource>();


	public void addClassAnnotation(int index, JavaSource source) {
		checkSource();
		classAnnotations.put(index, source);
	}
	
	public void addClassAnnotation(String source) {
		checkSource();
		classAnnotations.put(classAnnotations.size(), new JavaSource(source));
	}
	
	public void addConstructor(int index, JavaSource source) {
		checkSource();
		constructors.put(index, source);
	}
	
	public void addConstructor(JavaSource source) {
		checkSource();
		constructors.put(constructors.size(), source);
	}
	
	public void addImport(JavaSource source) {
		checkSource();
		String src = source.source;
		if(src.charAt(src.length()-1) == ';') {
			src = src.substring(0, src.length()-1);
		}
		imports.put(src, source);
	}
	
	public void addImport(String source) {
		checkSource();
		imports.put(source, new JavaSource(source));
	}
	
	private void addLocations(int offset, JavaSource part) {
		if(!blank(part.locations)) {
			for(EspLocation location : part.locations) {
				location.offset += offset;
				locations.add(location);
			}
		}
	}
	
	public void addMethod(String name, String source) {
		checkSource();
		methods.put(name, new JavaSource(source));
	}
	
	public void addMethod(String name, String source, List<EspLocation> locations) {
		checkSource();
		methods.put(name, new JavaSource(source, locations));
	}
	
	public void addStaticImport(JavaSource source) {
		checkSource();
		staticImports.put(source.source, source);
	}
	
	public void addStaticImport(String source) {
		checkSource();
		staticImports.put(source, new JavaSource(source));
	}

	public void addVariable(String name, JavaSource source) {
		checkSource();
		variables.put(name, source);
	}
	
	public void addVariable(String name, String source) {
		checkSource();
		variables.put(name, new JavaSource(source));
	}

	private void checkSource() {
		if(source != null) {
			throw new IllegalStateException("Source cannot be modified once being finalized");
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
			for(JavaSource imp : staticImports.values()) {
				sb.append("import static ");
				addLocations(sb.length(), imp);
				sb.append(imp).append(";\n");
			}
			sb.append('\n');
		}
		if(!imports.isEmpty()) {
			for(JavaSource imp : imports.values()) {
				sb.append("import ");
				addLocations(sb.length(), imp);
				sb.append(imp).append(';').append('\n');
			}
			sb.append('\n');
		}
		for(JavaSource ann : classAnnotations.values()) {
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
			for(JavaSource var : variables.values()) {
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
		for(JavaSource constructor : constructors.values()) {
			sb.append('\n');
			addLocations(sb.length(), constructor);
			sb.append(constructor).append('\n');
		}
		for(JavaSource method : methods.values()) {
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
		JavaSource part = constructors.get(index);
		if(part != null) {
			return part.source;
		}
		return null;
	}
	
	public int getConstructorCount() {
		return constructors.size();
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
	
	public String getFileName() {
		return simpleName + ".java";
	}

	public String getFilePath() {
		return getCanonicalName().replace('.', File.separatorChar) + ".java";
	}
	
	public String getImport(String key) {
		JavaSource part = imports.get(key);
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
		JavaSource part = methods.get(name);
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
	
	public String getStaticImport(String key) {
		JavaSource part = staticImports.get(key);
		if(part != null) {
			return part.toString();
		}
		return null;
	}
	
	public String getVariable(String name) {
		JavaSource part = variables.get(name);
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
