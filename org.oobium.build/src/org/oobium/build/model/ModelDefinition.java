/*******************************************************************************
 * Copyright (c) 2010, 2011 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.build.model;

import static org.oobium.build.util.SourceFile.ensureImports;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findAll;
import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.controllerSimpleName;
import static org.oobium.utils.StringUtils.simpleName;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oobium.build.gen.model.PropertyDescriptor;
import org.oobium.persist.Attribute;
import org.oobium.persist.Relation;
import org.oobium.utils.json.JsonUtils;

public class ModelDefinition {

	private static final char[] MODEL_DESCRIPTION = "@ModelDescription".toCharArray();
	private static final char[] INDEXES = "@Indexes".toCharArray();
	
	private static final Set<String> primitives;
	static {
		primitives = new HashSet<String>();
		primitives.add("byte");
		primitives.add("short");
		primitives.add("int");
		primitives.add("long");
		primitives.add("float");
		primitives.add("double");
		primitives.add("boolean");
		primitives.add("char");
	}
	
	public static List<String> getJavaArguments(char[] ca, int start, int end) {
		List<String> args = new ArrayList<String>();
		
		int s1 = start;
		int s = start;
		while(s < end) {
			switch(ca[s]) {
			case ',':
				String value = new String(ca, s1, s-s1).trim();
				args.add(value);
				s++;
				s1 = s;
				break;
			case '<':
			case '(':
			case '{':
			case '[':
			case '"':
				s = closer(ca, s, end, true) + 1;
				if(s == 0) {
					s = end;
				}
				break;
			case '/':
				if(ca[s-1] == '/') { // line comment
					s = findEOL(ca, s+1);
				}
				break;
			case '*':
				if(ca[s-1] == '/') { // multiline comment
					s++;
					while(s < end) {
						if(ca[s] == '/' && ca[s-1] == '*') {
							s++;
							break;
						}
						s++;
					}
				}
				break;
			default:
				s++;
			}
		}

		if(end >= s1) {
			String value = new String(ca, s1, end-s1).trim();
			if(value.length() > 0) {
				args.add(value);
			}
		}
		
		return args;
	}
	
	public static Map<String, String> getJavaEntries(char[] ca, int start, int end) {
		Map<String, String> entries = new LinkedHashMap<String, String>();
		
		String name = null;
		int s1 = start;
		int s = start;
		while(s < end) {
			switch(ca[s]) {
			case '=':
				name = new String(ca, s1, s-s1).trim();
				s++;
				s1 = s;
				break;
			case ',':
				String value = new String(ca, s1, s-s1).trim();
				entries.put(name, value);
				name = null;
				s++;
				s1 = s;
				break;
			case '<':
			case '(':
			case '{':
			case '[':
			case '"':
				s = closer(ca, s, end, true) + 1;
				if(s == 0) {
					s = end;
				}
				break;
			case '/':
				if(ca[s-1] == '/') { // line comment
					s = findEOL(ca, s+1);
				}
				break;
			case '*':
				if(ca[s-1] == '/') { // multiline comment
					s++;
					while(s < end) {
						if(ca[s] == '/' && ca[s-1] == '*') {
							s++;
							break;
						}
						s++;
					}
				}
				break;
			default:
				s++;
			}
		}
		
		if(name != null) {
			String value = new String(ca, s1, end-s1).trim();
			if(value.length() > 0) {
				entries.put(name, value);
			}
		}
		
		return entries;
	}
	
	public static Map<String, String> getJavaEntries(String s) {
		return getJavaEntries(s.toCharArray(), 0, s.length());
	}

	public static ModelDefinition[] getModelDefinitions(Collection<File> models) {
		return getModelDefinitions(models.toArray(new File[models.size()]));
	}

	public static ModelDefinition[] getModelDefinitions(File[] models) {
		ModelDefinition[] defs = new ModelDefinition[models.length];

		for(int i = 0; i < defs.length; i++) {
			defs[i] = new ModelDefinition(models[i]);
		}
		
		for(ModelDefinition def : defs) {
			def.setOpposites(defs);
		}
		
		return defs;
	}
	
	public static String getString(String in) {
		if(in == null) {
			return "";
		}
		if(in.length() > 1 && in.charAt(0) == '"' && in.charAt(in.length()-1) == '"') {
			// it is a string literal - escape special characters
			StringBuilder sb = new StringBuilder(in.length());
			for(int j = 1; j < in.length()-1; j++) {
				char c = in.charAt(j);
				switch(c) {
				case '\\':
					if(j < in.length()-2) {
						char c2 = in.charAt(j+1);
						switch(c2) {
						case '\\':
						case '"':
							c = c2;
							j++; // skip the next character (don't add it twice)
						}
					}
				default:
					sb.append(c);
					break;
				}
			}
			return sb.toString();
		}
		return in;
	}

	
	private final File file; // TODO this file object will not work when we need to deal with jars...
	private String source;
	private int mdstart;
	private int mdend;

	public final String packageName;
	public final String type;
	private Map<String, ModelAttribute> attributes;
	private final Map<String, ModelRelation> hasOne;
	private final Map<String, ModelRelation> hasMany;
	private final List<String> indexes;
	public boolean datestamps;
	public boolean timestamps = true;
	public boolean allowUpdate = true; // TODO
	public boolean allowDelete = true; // TODO

	public String[] siblings;

	public ModelDefinition(File file) {
		this(file.getName(), null, file, null);
	}

	public ModelDefinition(File file, String source) {
		this(file.getName(), source, file, null);
	}

	private ModelDefinition(String simpleName, String source, File file, String[] siblings) {
		if(source == null) {
			if(file != null && file.isFile()) {
				this.source = readFile(file).toString();
			} else {
				this.source = "";
			}
		} else {
			this.source = source;
		}

		this.packageName = parsePackageName();
		this.type = parseType(simpleName);

		this.file = file;
		this.siblings = siblings;
		
		this.attributes = new LinkedHashMap<String, ModelAttribute>();
		this.hasOne = new LinkedHashMap<String, ModelRelation>();
		this.hasMany = new LinkedHashMap<String, ModelRelation>();
		this.indexes = new ArrayList<String>();

		parse();
	}

	public ModelDefinition(String simpleName, String source, String[] siblings) {
		this(simpleName, source, null, siblings);
	}

	public ModelAttribute addAttribute(ModelAttribute attribute) {
		ModelAttribute attr = attribute.getCopy();
		attributes.put(attr.name, attr);
		return attr;
	}
	
	public ModelAttribute addAttribute(String attribute) {
		ModelAttribute attr = new ModelAttribute(this, attribute);
		attributes.put(attr.name, attr);
		return attr;
	}
	
	public ModelAttribute addAttribute(String name, String type) {
		if(!type.endsWith(".class")) type = type + ".class";
		return addAttribute("(name=\"" + name + "\",type=" + type + ")");
	}
	
	public ModelRelation addRelation(ModelRelation relation) {
		ModelRelation rel = relation.getCopy(this);
		if(rel.hasMany) {
			this.hasMany.put(rel.name, rel);
		} else {
			this.hasOne.put(rel.name, rel);
		}
		return rel;
	}

	public ModelRelation addRelation(String annotation, boolean hasMany) {
		ModelRelation rel = new ModelRelation(this, annotation, hasMany);
		if(rel.hasMany) {
			this.hasMany.put(rel.name, rel);
		} else {
			this.hasOne.put(rel.name, rel);
		}
		return rel;
	}
	
	public ModelRelation addRelation(String name, String type, boolean hasMany) {
		if(!type.endsWith(".class")) type = type + ".class";
		return addRelation("(name=\"" + name + "\",type=" + type + ")", hasMany);
	}
	
	public ModelAttribute getAttribute(String name) {
		return attributes.get(name);
	}
	
	public List<ModelAttribute> getAttributes() {
		return getAttributes(true);
	}

	public List<ModelAttribute> getAttributes(boolean includeTemporal) {
		List<ModelAttribute> attrs = new ArrayList<ModelAttribute>(attributes.values());

		if(includeTemporal) {
			if(datestamps) {
				for(ModelAttribute attr : getDatestampFields()) {
					attrs.add(attr);
				}
			}
			if(timestamps) {
				for(ModelAttribute attr : getTimestampFields()) {
					attrs.add(attr);
				}
			}
		}
		return attrs;
	}
	
	public String getCanonicalName() {
		return type;
	}
	
	public String getControllerName() {
		return controllerSimpleName(type);
	}

	public ModelAttribute[] getDatestampFields() {
		return new ModelAttribute[] {
				new ModelAttribute(this, "createdOn"),
				new ModelAttribute(this, "updatedOn")
		};
	}
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("@ModelDescription(");
		boolean first = true;
		if(!attributes.isEmpty()) {
			if(first) {
				first = false;
				sb.append("\n");
			}
			sb.append("\tattrs = {\n");
			for(Iterator<ModelAttribute> iter = attributes.values().iterator(); iter.hasNext(); ) {
				sb.append("\t\t").append(iter.next());
				if(iter.hasNext()) sb.append(',');
				sb.append('\n');
			}
			sb.append("\t}");
		}
		if(!hasOne.isEmpty()) {
			if(first) {
				first = false;
				sb.append("\n");
			} else {
				sb.append(",\n");
			}
			sb.append("\thasOne = {\n");
			for(Iterator<ModelRelation> iter = hasOne.values().iterator(); iter.hasNext(); ) {
				sb.append("\t\t").append(iter.next());
				if(iter.hasNext()) sb.append(',');
				sb.append('\n');
			}
			sb.append("\t}");
		}
		if(!hasMany.isEmpty()) {
			if(first) {
				first = false;
				sb.append("\n");
			} else {
				sb.append(",\n");
			}
			sb.append("\thasMany = {\n");
			for(Iterator<ModelRelation> iter = hasMany.values().iterator(); iter.hasNext(); ) {
				sb.append("\t\t").append(iter.next());
				if(iter.hasNext()) sb.append(',');
				sb.append('\n');
			}
			sb.append("\t}");
		}
		if(datestamps) {
			if(first) {
				first = false;
				sb.append("\n");
			} else {
				sb.append(",\n");
			}
			sb.append("\tdatestamps = true");
		}
		if(timestamps) {
			if(first) {
				first = false;
				sb.append("\n");
			} else {
				sb.append(",\n");
			}
			sb.append("\ttimestamps = true");
		}
		if(!first) {
			sb.append('\n');
		}
		sb.append(')');
		return sb.toString();
	}
	
	public List<String> getDescriptionImports() {
		List<String> imports = new ArrayList<String>();
		if(hasAttributes()) {
			imports.add(Attribute.class.getCanonicalName());
			for(ModelAttribute attr : attributes.values()) {
				if(!attr.type.startsWith("java.lang") && !primitives.contains(attr.type)) {
					imports.add(attr.type);
				}
			}
		}
		if(hasRelations()) {
			imports.add(Relation.class.getCanonicalName());
			for(ModelRelation r : hasOne.values()) {
				Pattern p = Pattern.compile("import\\s+" + r.type + "\\s*;");
				Matcher m = p.matcher(source);
				if(m.find()) {
					imports.add(r.type);
				}
			}
			for(ModelRelation r : hasMany.values()) {
				Pattern p = Pattern.compile("import\\s+" + r.type + "\\s*;");
				Matcher m = p.matcher(source);
				if(m.find()) {
					imports.add(r.type);
				}
			}
		}
		return imports;
	}
	
	public File getFile() {
		return file;
	}

	public List<String> getIndexes() {
		return indexes;
	}
	
	public String getPackageName() {
		int ix = type.lastIndexOf('.');
		if(ix == -1) {
			return type;
		}
		return type.substring(0, ix);
	}

	public LinkedHashMap<String, PropertyDescriptor> getProperties() {
		LinkedHashMap<String, PropertyDescriptor> properties = new LinkedHashMap<String, PropertyDescriptor>();
		for(Entry<String, ModelAttribute> entry : attributes.entrySet()) {
			properties.put(entry.getKey(), new PropertyDescriptor(entry.getValue()));
		}
		for(Entry<String, ModelRelation> entry : hasOne.entrySet()) {
			properties.put(entry.getKey(), new PropertyDescriptor(entry.getValue()));
		}
		for(Entry<String, ModelRelation> entry : hasMany.entrySet()) {
			properties.put(entry.getKey(), new PropertyDescriptor(entry.getValue()));
		}
		if(properties.containsKey("createdAt")) {
			properties.put("createdAt", properties.remove("createdAt"));
		}
		if(properties.containsKey("createdOn")) {
			properties.put("createdOn", properties.remove("createdOn"));
		}
		if(properties.containsKey("updatedAt")) {
			properties.put("updatedAt", properties.remove("updatedAt"));
		}
		if(properties.containsKey("updatedOn")) {
			properties.put("updatedOn", properties.remove("updatedOn"));
		}
		return properties;
	}
	
	public ModelRelation getRelation(String field) {
		ModelRelation r = hasOne.get(field);
		if(r == null) {
			r = hasMany.get(field);
		}
		return r;
	}

	public List<ModelRelation> getRelations() {
		List<ModelRelation> relations = new ArrayList<ModelRelation>();
		relations.addAll(hasOne.values());
		relations.addAll(hasMany.values());
		return relations;
	}
	
	protected String[] getSiblings() {
		if(siblings != null) {
			return siblings;
		}
		return (file != null) ? file.getParentFile().list() : new String[0];
	}

	public String getSimpleName() {
		return simpleName(type);
	}

	public void setAttributeOrder(String[] names) {
		Map<String, ModelAttribute> attrs = attributes;
		attributes = new LinkedHashMap<String, ModelAttribute>();
		for(String name : names) {
			attributes.put(name, attrs.get(name));
		}
	}
	
	public ModelAttribute[] getTimestampFields() {
		return new ModelAttribute[] {
				new ModelAttribute(this, "createdAt"),
				new ModelAttribute(this, "updatedAt")
		};
	}
	
	String getType(String name) {
		String type = null;
		if(name.endsWith(".class")) name = name.substring(0, name.length() - 6);
		boolean array = name.endsWith("[]");
		if(array) name = name.substring(0, name.length()-2);
		
		int ix = name.indexOf('.');
		if(ix != -1) {
			// handle inner classes
			name = getType(name.substring(0, ix)) + name.substring(ix);
			if(name.startsWith("java.lang.")) {
				name = name.substring(10);
			}
			return array ? (name + "[]") : name;
		}
		
		if(primitives.contains(name)) {
			return array ? (name + "[]") : name;
		}
		
		Pattern p = Pattern.compile("import\\s+(static\\s+)?([\\w\\d\\._\\$]+\\." + name + ");");
		Matcher m = p.matcher(source);
		if(m.find()) {
			type = m.group(2);
			if(m.group(1) != null) {
				type = type.substring(0, type.lastIndexOf('.'));
			}
		} else {
			if("String".equals(name)) {
				type = "java.lang.String";
			} else {
				String src = name + ".java"; // TODO will be ".class" if this is in a jar
				String[] siblings = getSiblings();
				for(String sibling : siblings) {
					if(src.equals(sibling)) {
						type = (packageName != null) ? (packageName + "." + name) : name;
						break;
					}
				}
				if(type == null) {
					type = "java.lang." + name;
				}
			}
		}
		if(array) {
			return type + "[]";
		}
		return type;
	}
	
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}
	
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}
	
	public boolean hasField(String name) {
		return attributes.containsKey(name) || hasOne.containsKey(name) || hasMany.containsKey(name);
	}
	
	public boolean hasMany() {
		return !hasMany.isEmpty();
	}
	
	public boolean hasMany(String field) {
		return hasMany.containsKey(field);
	}
	
	public boolean hasOne() {
		return !hasOne.isEmpty();
	}

	public boolean hasOne(String field) {
		return hasOne.containsKey(field);
	}
	
	public boolean hasRelation(String name) {
		return hasOne.containsKey(name) || hasMany.containsKey(name);
	}

	public boolean hasRelations() {
		return !hasOne.isEmpty() || !hasMany.isEmpty();
	}
	
	public boolean isThrough(String field) {
		ModelRelation relation = hasOne.get(field);
		if(relation != null) {
			return relation.isThrough();
		}
		relation = hasMany.get(field);
		if(relation != null) {
			return relation.isThrough();
		}
		return false;
	}
	
	public void load() {
		attributes.clear();
		hasOne.clear();
		hasMany.clear();
		indexes.clear();

		if(file != null && file.isFile()) {
			source = readFile(file).toString();
		}
		
		parse();
	}
	
	private void parse() {
		mdstart = -1;
		mdend = -1;
		
		char[] ca = source.toCharArray();
		
		int s0 = findAll(ca, 0, MODEL_DESCRIPTION);
		if(s0 != -1) {
			int s1 = find(ca, '(', s0);
			if(s1 != -1) {
				int s2 = closer(ca, s1);
				if(s2 != -1) {
					mdstart = s0;
					mdend = s2+1;
					parseDescription(ca, s1+1, s2);
					s1 = findAll(ca, 0, INDEXES);
					if(s1 != -1) {
						s1 = find(ca, '(', s1);
						if(s1 != -1) {
							s2 = closer(ca, s1);
							if(s2 != -1) {
								parseIndexes(ca, s1+2, s2);
							}
						}
					}
				}
			}
		}
	}
	
	private void parseAttributes(String attribute) {
		char[] ca = attribute.toCharArray();
		for(String attr : getJavaArguments(ca, 1, ca.length-1)) {
			addAttribute(attr);
		}
	}
	
	private void parseDescription(char[] ca, int start, int end) {
		Map<String, String> parameters = getJavaEntries(ca, start, end);
		
		String v;
		
		v = parameters.get("attrs");
		if(v != null) {
			parseAttributes(v);
		}
		
		v = parameters.get("hasOne");
		if(v != null) {
			parseRelations(v, false);
		}

		v = parameters.get("hasMany");
		if(v != null) {
			parseRelations(v, true);
		}
		
		datestamps = "true".equals(parameters.get("datestamps"));
		timestamps = "true".equals(parameters.get("timestamps"));
	}

	private void parseIndexes(char[] ca, int start, int end) {
		String s = new String(ca, start, end-start+1).trim();
		indexes.addAll(JsonUtils.toStringList(s));
	}
	
	private String parsePackageName() {
		Pattern p = Pattern.compile("package\\s+([\\w\\d\\._]+);");
		Matcher m = p.matcher(source);
		if(m.find()) {
			return m.group(1);
		}
		return null;
	}

	private void parseRelations(String relations, boolean hasMany) {
		char[] ca = relations.toCharArray();
		for(String relation : getJavaArguments(ca, 1, ca.length-1)) {
			addRelation(relation, hasMany);
		}
	}

	private String parseType(String name) {
		int ix = name.indexOf('.');
		if(ix != -1) {
			name = name.substring(0, ix);
		}
		if(packageName != null) {
			return packageName + "." + name;
		}
		return name;
	}
	
	public boolean remove(String field) {
		if(attributes.remove(field) != null) {
			return true;
		}
		if(hasOne.remove(field) != null) {
			return true;
		}
		if(hasMany.remove(field) != null) {
			return true;
		}
		return false;
	}

	public void save() {
		if(file != null && mdstart != -1) {
			// TODO not the most efficient scheme in the world...
			String desciption = getDescription();
			List<String> imports = getDescriptionImports();
			load();
			StringBuilder sb = new StringBuilder(source);
			sb.replace(mdstart, mdend, desciption);
			ensureImports(sb, imports);
			writeFile(file, sb.toString());
			load();
		}
	}
	
	public void setOpposites(ModelDefinition[] models) {
		for(ModelRelation relation : hasOne.values()) {
			relation.setOpposite(models);
		}
		for(ModelRelation relation : hasMany.values()) {
			relation.setOpposite(models);
		}
	}

	@Override
	public String toString() {
		return type + " => {" + "}";
	}

}
