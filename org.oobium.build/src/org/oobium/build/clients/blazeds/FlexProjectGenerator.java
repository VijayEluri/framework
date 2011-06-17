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
			project.delete();
		}
		else if(project.exists()) {
			throw new UnsupportedOperationException(project.getName() + " already exists");
		}
		
		createFolder(project, "bin");
		File src = createFolder(project, "src");

		for(File file : module.findModels()) {
			ModelDefinition model = new ModelDefinition(file);
			ActionScriptFile as = new ActionScriptFile();
			as.packageName = model.getPackageName();
			as.imports.add("mx.rpc.remoting.RemoteObject");
			as.imports.add("mx.rpc.events.ResultEvent");
			as.imports.add("mx.rpc.events.FaultEvent");
			as.classMetaTags.add("RemoteClass(alias=\"" + model.getCanonicalName() + "\")");
			as.simpleName = model.getSimpleType();
			as.variables.put("", "public var id:int");
			for(PropertyDescriptor des : model.getProperties().values()) {
				as.variables.put(des.variable(), "public var " + des.variable() + ":" + des.castType());
			}
			as.methods.put("save",
					"public function save():void {\n" +
					"\tthis.description = text;\n" +
					"\tuserRO = new RemoteObject();\n" +
					"\tuserRO.destination = \"" + model.getSimpleType() + "DAO\";\n" +
					"\tuserRO.getList.addEventListener(\"update\", getListResultHandler);\n" +
					"\tuserRO.addEventListener(\"fault\", faultHandler);\n" +
					"\tuserRO.update(this);\n" +
					"}"
				);
			writeFile(src, as.getFilePath(), as.toSource());
		}
		
		return project;
	}

	public void setForce(boolean force) {
		this.force = force;
	}
}
