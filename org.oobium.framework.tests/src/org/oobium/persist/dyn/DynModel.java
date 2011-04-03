package org.oobium.persist.dyn;

import static org.oobium.utils.StringUtils.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.oobium.persist.Model;

public class DynModel {

	private final DynModels models;
	private final String fullName;

	private Map<String, String> attrs;
	private Map<String, String> hasone;
	private Map<String, String> hasmany;
	private boolean datestamps;
	private boolean timestamps;
	
	private boolean compiled;
	
	DynModel(DynModels models, String fullName) {
		this.models = models;
		this.fullName = fullName;
		attrs = new LinkedHashMap<String, String>();
		hasone = new LinkedHashMap<String, String>();
		hasmany = new LinkedHashMap<String, String>();
	}

	public DynModel addAttr(String name, String type, String...options) {
		checkState();
		StringBuilder sb = new StringBuilder();
		sb.append("@Attribute");
		appendProperties(sb, name, type, options);
		attrs.put(name, sb.toString());
		return this;
	}
	
	public DynModel addHasMany(String name, String type, String...options) {
		checkState();
		StringBuilder sb = new StringBuilder();
		sb.append("@Relation");
		appendProperties(sb, name, type, options);
		hasmany.put(name, sb.toString());
		return this;
	}
	
	public DynModel addHasOne(String name, String type, String...options) {
		checkState();
		StringBuilder sb = new StringBuilder();
		sb.append("@Relation");
		appendProperties(sb, name, type, options);
		hasone.put(name, sb.toString());
		return this;
	}
	
	private void appendProperties(StringBuilder sb, String name, String type, String[] options) {
		checkState();
		sb.append("(name=\"").append(name).append("\",").append("type=").append(type);
		if(options.length > 0) {
			for(String option : options) {
				sb.append(',').append(option);
			}
		}
		sb.append(')');
	}
	
	private void checkState() {
		if(compiled) {
			throw new IllegalStateException(getClass().getSimpleName() + " has already been compiled");
		}
	}

	public String getFullName() {
		return fullName;
	}
	
	public Class<? extends Model> getModelClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		compiled = true;
		return models.newModelClass(fullName);
	}
	
	public String getModelDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("@ModelDescription(");
		boolean first = true;
		if(!attrs.isEmpty()) {
			if(first) {
				first = false;
				sb.append("\n");
			}
			sb.append("\tattrs = {\n");
			for(String attr : attrs.values()) {
				sb.append("\t\t").append(attr).append('\n');
			}
			sb.append("\t}");
		}
		if(!hasone.isEmpty()) {
			if(first) {
				first = false;
				sb.append("\n");
			} else {
				sb.append(",\n");
			}
			sb.append("\thasOne = {\n");
			for(String attr : hasone.values()) {
				sb.append("\t\t").append(attr).append('\n');
			}
			sb.append("\t}");
		}
		if(!hasmany.isEmpty()) {
			if(first) {
				first = false;
				sb.append("\n");
			} else {
				sb.append(",\n");
			}
			sb.append("\thasMany = {\n");
			for(String attr : hasmany.values()) {
				sb.append("\t\t").append(attr).append('\n');
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
	
	public Model newInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		compiled = true;
		return models.newInstance(fullName);
	}
	
	public DynModel datestamps() {
		datestamps = true;
		return this;
	}
	
	public DynModel timestamps() {
		timestamps = true;
		return this;
	}
	
	public DynModel setTimestamps(boolean on) {
		timestamps = on;
		return this;
	}
	
	public String toSource() {
		StringBuilder sb = new StringBuilder();
		String packageName = packageName(fullName);
		if(packageName != null) {
			sb.append("package ").append(packageName).append(";\n");
		}
		sb.append("import org.oobium.persist.*;\n");
		sb.append(getModelDescription());
		sb.append("\npublic class ").append(simpleName(fullName)).append(" extends Model { }");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return toSource();
	}
	
}
