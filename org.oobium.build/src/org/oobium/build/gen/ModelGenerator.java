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

import static org.oobium.build.util.ProjectUtils.getSrcAnnotations;
import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.plural;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oobium.build.gen.model.PropertyDescriptor;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.build.util.PersistConfig;
import org.oobium.build.util.PersistConfig.Service;
import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Paginator;
import org.oobium.persist.PersistService;
import org.oobium.persist.Relation;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.StringUtils;
import org.oobium.utils.json.JsonModel;
import org.oobium.utils.json.JsonUtils;


public class ModelGenerator {

	public static final int
		GEN_MODELS 	 	= 1 << 0,
		GEN_VIEWS 		= 1 << 1,
		GEN_CONTROLLERS = 1 << 2,
		GEN_SCHEMA		= 1 << 3,
		GEN_TESTS		= 1 << 4,
		GEN_APP 		= GEN_MODELS | GEN_VIEWS | GEN_CONTROLLERS,
		GEN_APP_WS 		= GEN_MODELS | GEN_CONTROLLERS,
		GEN_ALL 		= GEN_MODELS | GEN_VIEWS | GEN_CONTROLLERS | GEN_SCHEMA | GEN_TESTS;

	/**
	 * Append the given javadoc (after replacing all variables with the supplied vars array) to
	 * the given StringBuilder.
	 */
	private static void appendDoc(StringBuilder sb, String javadoc, String...vars) {
		StringBuilder doc = new StringBuilder(javadoc.length() + StringUtils.count(vars) + 5);
		doc.append(javadoc);
		int i = 0;
		int pos = 0;
		while((pos = doc.indexOf("?", pos)) != -1 && i < vars.length) {
			doc.replace(pos, pos+1, vars[i++]);
		}
		
		sb.append("\t/**\n");
		for(String line : doc.toString().split("\n")) {
			sb.append("\t * ").append(line).append('\n');
		}
		sb.append("\t*/\n");
	}

	private static String build(String type, String sig, String body) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t@Override\n");
		sb.append("\tpublic ").append(type).append(' ').append(sig).append(" {\n");
		sb.append("\t\treturn (").append(type).append(") super.").append(body).append(";\n");
		sb.append("\t}");
		return sb.toString();
	}
	
	private static void createConstructor(SourceFile src, List<String> inits) {
		StringBuilder sb = new StringBuilder();
		sb.append("\tpublic ").append(src.simpleName).append("() {\n");
		sb.append("\t\tsuper();\n");
		for(String init : inits) {
			sb.append("\t\t").append(init).append('\n');
		}
		sb.append("\t}");
		src.constructors.put(0, sb.toString());
	}

	private static String createInitializer(SourceFile src, PropertyDescriptor property) {
		String init = property.init();
		if(property.isType(Map.class)) {
			if(init != null && init.length() > 1 && init.charAt(0) == '{' && init.charAt(init.length()-1) == '}') {
				src.imports.add(JsonUtils.class.getCanonicalName());
				init = "JsonUtils.toMap(\"" + init + "\")";
			}
		}
		return "set(" + property.enumProp() + ", " + init + ");";
	}
	
	private static void createOverrideMethods(SourceFile src, ModelDefinition model) {
		String type = model.getSimpleType();
		
		src.methods.put("put(String field, Object value)", 	build(type, "put(String field, Object value)", "put(field, value)"));
		src.methods.put("putAll(JsonModel model)", 			build(type, "putAll(JsonModel model)", "putAll(model)"));
		src.methods.put("putAll(Map<String, Object> data)", build(type, "putAll(Map<String, Object> data)", "putAll(data)"));
		src.methods.put("putAll(String json)", 				build(type, "putAll(String json)", "putAll(json)"));
		src.methods.put("set(String field, Object value)", 	build(type, "set(String field, Object value)", "set(field, value)"));
		src.methods.put("setAll(Map<String, Object> data)", build(type, "setAll(Map<String, Object> data)", "setAll(data)"));
		src.methods.put("setAll(String json)", 				build(type, "setAll(String json)", "setAll(json)"));
		src.methods.put("setId(int id)", 					build(type, "setId(int id)", "setId(id)"));
	}
	
	private static String createStaticMethods(String type) {
		StringBuilder sb = new StringBuilder();
		appendDoc(sb, "Get the PersistService appropriate for the ? class.", type);
		sb.append("\tpublic static PersistService getPersistService() {\n");
		sb.append("\t\treturn Model.getPersistService(").append(type).append(".class);\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Get the count of all instances of ?", type);
		sb.append("\tpublic static int count() throws SQLException {\n");
		sb.append("\t\treturn Model.count(").append(type).append(".class);\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Get the count of all instances of ? using the given sql query and values.", type);
		sb.append("\tpublic static int count(String sql, Object...values) throws SQLException {\n");
		sb.append("\t\treturn Model.count(").append(type).append(".class, sql, values);\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Find the ? with the given id", type);
		sb.append("\tpublic static ").append(type).append(" find(int id) throws SQLException {\n");
		sb.append("\t\treturn Model.find(").append(type).append(".class, id);\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Find the ? with the given id and include the given fields.\nThe include option can start with 'include:', but it is not required.\nIf include is null, then this method delegates to find(int).", type);
		sb.append("\tpublic static ").append(type).append(" find(int id, String include) throws SQLException {\n");
		sb.append("\t\tif(include == null) {\n");
		sb.append("\t\t\treturn Model.find(").append(type).append(".class, id);\n");
		sb.append("\t\t} else {\n");
		sb.append("\t\t\tString sql = (include.startsWith(\"include:\") ? \"where id=? \" : \"where id=? include:\") + include;\n");
		sb.append("\t\t\treturn Model.find(").append(type).append(".class, sql, id);\n");
		sb.append("\t\t}\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Find the ? using the given sql query and values.  Note that only one instance will be returned.\nPrepend the query with 'where' to enter only the where clause.", type);
		sb.append("\tpublic static ").append(type).append(" find(String sql, Object...values) throws SQLException {\n");
		sb.append("\t\treturn Model.find(").append(type).append(".class, sql, values);\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Find all instances of ?", type);
		sb.append("\tpublic static List<").append(type).append("> findAll() throws SQLException {\n");
		sb.append("\t\treturn Model.findAll(").append(type).append(".class);\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Find all instances of ? using the given query Map and array of its entries to use.", type);
		sb.append("\tpublic static List<").append(type).append("> findAll(Map<?, ?> query, String...entries) throws SQLException {\n");
		sb.append("\t\treturn Model.findAll(").append(type).append(".class, query, entries);\n");
		sb.append("\t}\n");
		sb.append("\n");
		appendDoc(sb, "Find all instances of ? using the given sql query and values.", type);
		sb.append("\tpublic static List<").append(type).append("> findAll(String sql, Object...values) throws SQLException {\n");
		sb.append("\t\treturn Model.findAll(").append(type).append(".class, sql, values);\n");
		sb.append("\t}\n");
		sb.append("\n");
		sb.append("\tpublic static Paginator<").append(type).append("> paginate(int page, int perPage) throws SQLException {\n");
		sb.append("\t\treturn Paginator.paginate(").append(type).append(".class, page, perPage);\n");
		sb.append("\t}\n");
		sb.append("\n");
		sb.append("\tpublic static Paginator<").append(type).append("> paginate(int page, int perPage, String sql, Object...values) throws SQLException {\n");
		sb.append("\t\treturn Paginator.paginate(").append(type).append(".class, page, perPage, sql, values);\n");
		sb.append("\t}");
		return sb.toString();
	}
	
	public static File[] generate(Workspace workspace, Module module, File...models) {
		return generate(workspace, module, GEN_MODELS, models);
	}
	

	public static File[] generate(Workspace workspace, Module module, File model, int flags) {
		return generate(workspace, module, flags, new File[] { model });
	}
	
	private static File[] generate(Workspace workspace, Module module, int action, File...models) {
		ModelGenerator gen = new ModelGenerator(workspace, module, action);
		gen.process(models);
		return gen.getFiles();
	}
	
	public static File[] generate(Workspace workspace, Module module, List<File> models) {
		return generate(workspace, module, GEN_MODELS, models.toArray(new File[models.size()]));
	}
	
	public static File generateSchema(Workspace workspace, Application app, Mode mode) {
		Migrator migrator = workspace.getMigratorFor(app);
		if(migrator == null) {
			return null;
		}
		
		PersistConfig config = new PersistConfig(app, mode);
		
		List<File> models = new ArrayList<File>();
		
		for(PersistConfig modConfig : config.getModuleConfigs()) {
			Module module = workspace.getModule(modConfig.getModule());
			if(modConfig.isDb()) { // add all, then remove those that are not db
				models.addAll(module.findModels());
				for(Service service : modConfig.getServices()) {
					if(!service.isDb()) {
						for(String model : service.getModels()) {
							models.remove(module.getModel(model));
						}
					}
				}
			} else { // add only those that are db
				for(Service service : modConfig.getServices()) {
					if(service.isDb()) {
						for(String model : service.getModels()) {
							models.add(module.getModel(model));
						}
					}
				}
			}
		}
		
		ModelDefinition[] defs = new ModelDefinition[models.size()];

		for(int i = 0; i < models.size(); i++) {
			defs[i] = new ModelDefinition(models.get(i));
		}
		
		String src = DbGenerator.generate(app.name, defs);

		File file = migrator.getInitialMigration();
		
		return writeFile(file, src);
	}

	private final Workspace workspace;


	private final Module module;
	private int action;
	private final List<File> files;
	
	private ModelGenerator(Workspace workspace, Module module, int action) {
		this.workspace = workspace;
		this.module = module;
		this.action = action;
		files = new ArrayList<File>();
	}

	public void addFile(File file) {
		files.add(file);
	}

	private String generate(String classAnnotations, ModelDefinition model) {
		SourceFile src = new SourceFile();

		src.classAnnotations.put(0, model.description);
		src.imports.add(ModelDescription.class.getCanonicalName());
		if(model.hasAttributes()) {
			src.imports.add(Attribute.class.getCanonicalName());
		}
		if(model.hasRelations()) {
			src.imports.add(Relation.class.getCanonicalName());
		}
		for(String di : model.descriptionImports) {
			src.imports.add(di);
		}
		
		src.simpleName = model.getSimpleType() + "Model";
		src.packageName = model.getPackageName();
		src.superName = Model.class.getSimpleName();
		src.isAbstract = true;

		src.propertiesArray = "FIELDS";
		for(ModelAttribute attribute : model.attributes.values()) {
			src.properties.put(attribute.name, new PropertyDescriptor(attribute));
		}

		for(ModelRelation relation : model.relations.values()) {
			src.properties.put(relation.name, new PropertyDescriptor(relation));
		}

		src.imports.add(JsonModel.class.getCanonicalName());
		src.imports.add(Model.class.getCanonicalName());
		src.imports.add(List.class.getCanonicalName());
		src.imports.add(Map.class.getCanonicalName());
		src.imports.add(Paginator.class.getCanonicalName());
		src.imports.add(SQLException.class.getCanonicalName());
		src.imports.add(PersistService.class.getCanonicalName());

		List<String> inits = new ArrayList<String>();
		for(PropertyDescriptor property : src.properties.values()) {
			src.imports.addAll(property.imports());
			if(property.hasInit()) {
				inits.add(createInitializer(src, property));
			}
			src.methods.putAll(property.methods());
		}

		createConstructor(src, inits);

		createOverrideMethods(src, model);
		
		src.staticMethods.put("finders", createStaticMethods(model.getSimpleType()));
		
		for(Iterator<String> iter = src.imports.iterator(); iter.hasNext(); ) {
			if(model.getCanonicalName().equals(iter.next())) {
				iter.remove();
			}
		}

		return "/*\n" + classAnnotations + "*/\n" + src.toSource();
	}

	private void generateControllerFiles(Module module, ModelDefinition[] models) {
		for(ModelDefinition model : models) {
			String src = ControllerGenerator.generate(module, model);

			File controller = module.getController(model.getSimpleType());
			files.add(writeFile(controller, src));
		}
	}

	private void generateModelFiles(Module module, ModelDefinition[] models) {
		for(ModelDefinition model : models) {
			try {
				File genFile = module.getGenModel(model.file);
				String annotations = getSrcAnnotations(model.file);
				String src = generate(annotations, model);
				files.add(writeFile(genFile, src));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void generateViewFiles(Module module, ModelDefinition[] models) {
		for(ModelDefinition model : models) {
			String name = model.getSimpleType();
			String plur = plural(name);
			File folder = module.getViewsFolder(name);
			
			ViewGenerator gen = new ViewGenerator(model);

			files.add(writeFile(folder, "ShowEdit" + name + ".esp", gen.generateShowEditView()));
			files.add(writeFile(folder, "ShowAll"  + plur + ".esp", gen.generateShowAllView()));
			files.add(writeFile(folder, "ShowNew"  + name + ".esp", gen.generateShowNewView()));
			files.add(writeFile(folder, "Show" 	 + name + ".esp", gen.generateShowView()));
			files.add(writeFile(folder, name	   + "Form" + ".esp", gen.generateForm()));
		}
	}
	
	File[] getFiles() {
		return files.toArray(new File[files.size()]);
	}
	
	private ModelDefinition[] getModelDefinitions(File[] models) {
		ModelDefinition[] defs = new ModelDefinition[models.length];

		for(int i = 0; i < defs.length; i++) {
			defs[i] = new ModelDefinition(models[i]);
		}
		
		for(ModelDefinition def : defs) {
			def.setOpposites(defs);
		}
		
		return defs;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}

	private void modifySrcFile(ModelDefinition model) {
		StringBuilder sb = readFile(model.file);
		String s = "class " + model.getSimpleType();
		int start = sb.indexOf(s) + s.length();
		int end = sb.indexOf("implements", start);
		if(end == -1) {
			end = sb.indexOf("{", start);
		}
		String superStr = " " + model.getSimpleType() + "Model ";
		s = sb.toString().substring(start, end);
		if(!s.contains(superStr)) {
			sb.replace(start, end, " extends" + superStr);
			files.add(writeFile(model.file, sb.toString()));
		}
	}
	
	private void modifySrcFiles(ModelDefinition[] models) {
		for(ModelDefinition model : models) {
			modifySrcFile(model);
		}
	}

	private void process(File[] models) {
		ModelDefinition[] defs = getModelDefinitions(models);
		
		if((action & GEN_MODELS) != 0) {
			generateModelFiles(module, defs);
			modifySrcFiles(defs);
		}
		if((action & GEN_VIEWS) != 0) {
			generateViewFiles(module, defs);
		}
		if((action & GEN_CONTROLLERS) != 0) {
			generateControllerFiles(module, defs);
		}
	}
	
}
