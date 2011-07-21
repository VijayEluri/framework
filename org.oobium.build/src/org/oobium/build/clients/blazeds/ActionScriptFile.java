package org.oobium.build.clients.blazeds;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.oobium.build.util.MethodCreator;

public class ActionScriptFile {

	public String simpleName;
	public String packageName;
	public String superName;
	public TreeSet<String> interfaces = new TreeSet<String>();
	public TreeSet<String> imports = new TreeSet<String>();
	public LinkedHashSet<String> classMetaTags = new LinkedHashSet<String>();
	public TreeMap<String, String> staticVariables = new TreeMap<String, String>();
	public TreeMap<String, String> staticMethods = new TreeMap<String, String>();
	public TreeMap<String, String> variables = new TreeMap<String, String>();
	public TreeMap<Integer, String> constructors = new TreeMap<Integer, String>();
	public TreeMap<String, String> methods = new TreeMap<String, String>();
	
	public void addMethod(MethodCreator mc){
		methods.put(mc.name, mc.toString());
	}

	public String getCanonicalName() {
		return packageName + "." + simpleName;
	}
	
	public String getFileName() {
		return simpleName + ".as";
	}

	public String getFilePath() {
		return getCanonicalName().replace('.', File.separatorChar) + ".as";
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getSimpleName() {
		return simpleName;
	}
	
	public String toSource() {
		StringBuilder sb = new StringBuilder();

		if(packageName != null) {
			sb.append("package ").append(packageName).append(" {\n");
			sb.append('\n');
		}
		if(!imports.isEmpty()) {
			for(String imp : imports) {
				sb.append("\timport ").append(imp).append(";\n");
			}
			sb.append('\n');
		}
		if(!classMetaTags.isEmpty()) {
			for(String tag : classMetaTags) {
				sb.append('\t').append('[').append(tag).append(']').append('\n');
			}
			sb.append('\n');
		}
		sb.append("\tpublic class ").append(simpleName);
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
		if(!staticVariables.isEmpty()) {
			sb.append('\n');
			for(String var : staticVariables.values()) {
				sb.append("\t\t");
				if(var.startsWith("public ") || var.startsWith("protected ") || var.startsWith("private ")) {
					sb.append(var);
				} else {
					if(var.startsWith("static ")) {
						sb.append("private ").append(var);
					} else {
						sb.append("private static ").append(var);
					}
				}
				if(!var.endsWith(";")) {
					sb.append(';');
				}
				sb.append('\n');
			}
		}
		for(String method : staticMethods.values()) {
			sb.append("\n\t\t");
			sb.append(method.replace("\n", "\n\t\t")).append('\n');
		}
		if(!staticVariables.isEmpty() || !staticMethods.isEmpty()) {
			sb.append('\n');
		}
		if(!variables.isEmpty()) {
			sb.append('\n');
			for(String var : variables.values()) {
				sb.append("\t\t");
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
		for(String constructor : constructors.values()) {
			sb.append("\n\t");
			sb.append(constructor).append('\n');
		}
		for(String method : methods.values()) {
			sb.append("\n\t\t");
			sb.append(method.replace("\n", "\n\t\t")).append('\n');
		}
		sb.append("\n\t}\n\n}");

		return sb.toString();
	}
	
	@Override
	public String toString() {
		return super.toString() + " {" + getCanonicalName() + "}";
	}

	
}
