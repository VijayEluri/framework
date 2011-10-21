package org.oobium.framework.tests.dyn;

import static org.oobium.utils.StringUtils.packageName;
import static org.oobium.utils.StringUtils.simpleName;

import org.oobium.build.model.ModelDefinition;
import org.oobium.persist.Model;

public class DynModel extends DynClass {

	private final ModelDefinition def;
	
	DynModel(DynClasses models, String fullName) {
		super(models, fullName, null);
		def = new ModelDefinition(simpleName(fullName), getSource(fullName));
	}

	public DynModel addAttr(String name, String type, String...options) {
		checkState();
		def.addAttribute(name, type, options);
		return this;
	}
	
	public DynModel addHasMany(String name, String type, String...options) {
		checkState();
		def.addHasMany(name, type, options);
		return this;
	}
	
	public DynModel addHasOne(String name, String type, String...options) {
		checkState();
		def.addHasOne(name, type, options);
		return this;
	}
	
	@Override
	public DynModel addImport(Class<?> clazz) {
		return (DynModel) super.addImport(clazz);
	}

	@Override
	public DynModel addImport(String fullName) {
		return (DynModel) super.addImport(fullName);
	}
	
	public Class<? extends Model> getModelClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return super.getDynamicClass().asSubclass(Model.class);
	}
	
	public Model newInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return (Model) super.newInstance();
	}
	
	public DynModel embedded() {
		def.embedded(true);
		return this;
	}
	
	public DynModel datestamps() {
		def.datestamps(true);
		return this;
	}
	
	public DynModel timestamps() {
		def.timestamps(true);
		return this;
	}
	
	public DynModel setTimestamps(boolean on) {
		def.timestamps(on);
		return this;
	}

	private String getSource(String fullName) {
		StringBuilder sb = new StringBuilder();
		String packageName = packageName(fullName);
		if(packageName != null) {
			sb.append("package ").append(packageName).append(";\n");
		}
		sb.append("import org.oobium.persist.Relation;\n");
		sb.append("import org.oobium.persist.*;\n");
		if(imports != null) {
			for(String imp : imports) {
				sb.append("import ").append(imp).append(";\n");
			}
		}
		sb.append("@ModelDescription()");
		sb.append("\npublic class ").append(simpleName(fullName)).append(" extends Model { }");
		return sb.toString();
	}
	
	public String getSource() {
		if(source == null) {
			for(String imp : def.getDescriptionImports()) {
				addImport(imp);
			}
			source = getSource(getFullName()).replace("@ModelDescription()", def.getDescription());
		}
		return source;
	}
	
	@Override
	public String toString() {
		return getSource();
	}
	
}
