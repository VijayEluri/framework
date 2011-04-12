package org.oobium.framework.tests.dyn;

import static org.oobium.utils.StringUtils.*;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.oobium.persist.Model;

public class DynModel extends DynClass {

	private Map<String, String> attrs;
	private Map<String, String> hasone;
	private Map<String, String> hasmany;
	private boolean datestamps;
	private boolean timestamps;
	
	DynModel(DynClasses models, String fullName) {
		super(models, fullName, null);
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
	
	public Class<? extends Model> getModelClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return super.getDynamicClass().asSubclass(Model.class);
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
			for(Iterator<String> iter = attrs.values().iterator(); iter.hasNext(); ) {
				sb.append("\t\t").append(iter.next());
				if(iter.hasNext()) sb.append(',');
				sb.append('\n');
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
			for(Iterator<String> iter = hasone.values().iterator(); iter.hasNext(); ) {
				sb.append("\t\t").append(iter.next());
				if(iter.hasNext()) sb.append(',');
				sb.append('\n');
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
			for(Iterator<String> iter = hasmany.values().iterator(); iter.hasNext(); ) {
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
	
	public Model newInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return (Model) super.newInstance();
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
		String packageName = packageName(getFullName());
		if(packageName != null) {
			sb.append("package ").append(packageName).append(";\n");
		}
		sb.append("import org.oobium.persist.*;\n");
		sb.append(getModelDescription());
		sb.append("\npublic class ").append(simpleName(getFullName())).append(" extends Model { }");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return toSource();
	}
	
}
