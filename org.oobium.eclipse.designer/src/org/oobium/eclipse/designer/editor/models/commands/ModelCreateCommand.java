package org.oobium.eclipse.designer.editor.models.commands;

import java.io.File;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.ModuleElement;

public class ModelCreateCommand extends Command {

	private ModuleElement moduleElement;
	private ModelElement modelElement;
	private Rectangle bounds;
	
	private String name;
	private String[][] attributes;
	private boolean timestamps = true;
	private boolean datestamps;
	private boolean allowUpdate = true;
	private boolean allowDelete = true;
	
	
	public ModelCreateCommand(ModuleElement module, Rectangle bounds) {
		this.moduleElement = module;
		this.bounds = bounds;
	}
	
	@Override
	public boolean canExecute() {
		return moduleElement != null && bounds != null;
	}
	
	@Override
	public void execute() {
		Module module = moduleElement.getModule();
		File file = module.getModel(name);
		String source =
			"package " + module.packageName(file, true) + ";\n" +
			"\n" +
			"import org.oobium.persist.ModelDescription;\n" +
			"\n" +
			"@ModelDescription(\n" +
			"\ttimestamps = true\n" +
			")\n" +
			"public class " + module.getModelName(file) + " {\n" +
			"\n" +
			"}";

		ModelDefinition definition = new ModelDefinition(file, source);
		for(String[] attribute : attributes) {
			definition.addAttribute(attribute[0], attribute[1]);
		}
		definition.timestamps = timestamps;
		definition.datestamps = datestamps;
		definition.allowUpdate = allowUpdate;
		definition.allowDelete = allowDelete;
		
		modelElement = new ModelElement(moduleElement, definition);
		if(bounds.isEmpty()) {
			modelElement.setLocation(bounds.getLocation());
		} else {
			modelElement.setBounds(bounds);
		}
		redo();
	}
	
	@Override
	public void redo() {
		moduleElement.addModel(modelElement);
	}

	public void setAllowDelete(boolean allowDelete) {
		this.allowDelete = allowDelete;
	}

	public void setAllowUpdate(boolean allowUpdate) {
		this.allowUpdate = allowUpdate;
	}

	public void setAttributes(String[][] attributes) {
		this.attributes = attributes;
	}
	
	public void setDatestamps(boolean datestamps) {
		this.datestamps = datestamps;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setTimestamps(boolean timestamps) {
		this.timestamps = timestamps;
	}

	@Override
	public void undo() {
		File file = modelElement.getFile();
		if(file.isFile()) {
			modelElement.getFile().delete();
		}
		moduleElement.removeModel(modelElement);
	}
	
}
