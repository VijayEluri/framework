package org.oobium.build.clients.blazeds;

import static org.oobium.utils.FileUtils.*;

import java.io.File;

import org.oobium.build.gen.model.PropertyDescriptor;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.workspace.Module;

public class FlexProjectGenerator {

	private final Module module;
	private final File project;
	
	private boolean force;
	
	public FlexProjectGenerator(Module module) {
		this(module, null);
	}
	
	public FlexProjectGenerator(Module module, File project) {
		this.module = module;
		if(project == null) {
			project = module.file.getParentFile();
		}
		if(project.isDirectory()) {
			this.project = new File(project, module.name + ".blazeds.flex");
		} else {
			this.project = project;
		}
	}
	
	public File create() {
		if(force) {
			deleteContents(project);
		}
		else if(project.exists()) {
			throw new UnsupportedOperationException(project.getName() + " already exists");
		}
		
		createProjectFile();

		createFolder(project, "bin");
		File src = createFolder(project, "src");

		for(File file : module.findModels()) {
			createModel(src, file);
		}
		
		return project;
	}

	private void createModel(File srcFolder, File modelFile) {
		ModelDefinition model = new ModelDefinition(modelFile);
		ActionScriptFile as = new ActionScriptFile();
		as.packageName = model.getPackageName();
		as.imports.add("mx.rpc.remoting.RemoteObject");
		as.imports.add("mx.rpc.events.ResultEvent");
		as.imports.add("mx.rpc.events.FaultEvent");
		as.classMetaTags.add("RemoteClass(alias=\"" + model.getCanonicalName() + "\")");
		as.simpleName = model.getSimpleType();
		
		as.staticVariables.put("ro", "public static const ro:Object = createRemoteObject()");
		
		as.staticMethods.put("createRemoteObject", 
				"private static function createRemoteObject():RemoteObject {\n" +
				"\tvar ro:RemoteObject = new RemoteObject();\n" +
				"\tro.destination = \"" + as.simpleName + "Controller\";\n" +
				"\treturn ro;\n" +
				"}"
			);

		as.variables.put("", "public var id:int");
		for(PropertyDescriptor prop : model.getProperties().values()) {
			as.variables.put(prop.variable(), "public var " + prop.variable() + ":" + prop.castType());
		}

		as.staticMethods.put("find",
				"public static function find(o:Object, callback:Function):void {\n" +
				"\t" + as.simpleName + ".ro.find.addEventListener(\"result\", callback);\n" +
				"\tif(typeof(o) == \"number\") {\n" +
				"\t\t" + as.simpleName + ".ro.find(o as int);\n" +
				"\t} else if(typeof(o) == \"string\") {\n" +
				"\t\t" + as.simpleName + ".ro.find(o as String);\n" +
				"\t} else if(type != null) {\n" +
				"\t\t" + as.simpleName + ".ro.find(o.toString());\n" +
				"\t} else {\n" +
				"\t\tthrow new Error(\"o cannot be null\");\n" +
				"\t}\n" +
				"}"
			);

		as.staticMethods.put("findAll",
				"public static function findAll(o:Object, callback:Function):void {\n" +
				"\t" + as.simpleName + ".ro.findAll.addEventListener(\"result\", callback);\n" +
				"\tif(o == \"*\") {\n" +
				"\t\t" + as.simpleName + ".ro.findAll();\n" +
				"\t} else if(typeof(o) == \"string\") {\n" +
				"\t\t" + as.simpleName + ".ro.findAll(o as String);\n" +
				"\t} else if(type != null) {\n" +
				"\t\t" + as.simpleName + ".ro.findAll(o.toString());\n" +
				"\t} else {\n" +
				"\t\tthrow new Error(\"o cannot be null\");\n" +
				"\t}\n" +
				"}"
			);
		
		as.methods.put("create",
				"public function create():void {\n" +
				"\t" + as.simpleName + ".ro.create(this);\n" +
				"}"
			);
		
		as.methods.put("update",
				"public function update():void {\n" +
				"\t" + as.simpleName + ".ro.update(this);\n" +
				"}"
			);
		
		as.methods.put("save",
				"public function save():void {\n" +
				"\tif(id < 1) {\n" +
				"\t\t" + as.simpleName + ".ro.create(this);\n" +
				"\t} else {\n" +
				"\t\t" + as.simpleName + ".ro.update(this);\n" +
				"\t}\n" +
				"}"
			);
		
		as.methods.put("destroy",
				"public function destroy():void {\n" +
				"\t" + as.simpleName + ".ro.destroy(this);\n" +
				"}"
			);

		writeFile(srcFolder, as.getFilePath(), as.toSource());
	}

	private void createProjectFile() {
		writeFile(project, ".project",
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<projectDescription>\n" +
				"\t<name>" + project.getName() + "</name>\n" +
				"\t<comment></comment>\n" +
				"\t<projects>\n" +
				"\t</projects>\n" +
				"\t<buildSpec>\n" +
				"\t\t<buildCommand>\n" +
				"\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n" +
				"\t\t\t<arguments>\n" +
				"\t\t\t</arguments>\n" +
				"\t\t</buildCommand>\n" +
				"\t</buildSpec>\n" +
				"\t<natures>\n" +
				"\t\t<nature>com.adobe.flexbuilder.project.flexnature</nature>\n" +
				"\t\t<nature>com.adobe.flexbuilder.project.actionscriptnature</nature>\n" +
				"\t</natures>\n" +
				"</projectDescription>\n"
			);
	}
	
	public void setForce(boolean force) {
		this.force = force;
	}

}
