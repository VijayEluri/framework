package org.oobium.eclipse.designer.editor.models.commands;

import java.io.File;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.RGB;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.ModuleElement;

public class ModelCreateCommand extends Command {

	private ModuleElement moduleElement;
	private ModelElement modelElement;
	private Rectangle bounds;
	
	private ModelDefinition definition;
	private RGB color;
	
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
		File file = module.getModel(definition.getSimpleName());
		definition.setFile(file);
		definition.setPackageName(module.packageName(file, true));
		modelElement = new ModelElement(moduleElement, definition);
		if(bounds.isEmpty()) {
			modelElement.setLocation(bounds.getLocation());
		} else {
			modelElement.setBounds(bounds);
		}
		modelElement.setColor(color);
		redo();
	}
	
	@Override
	public void redo() {
		moduleElement.addModel(modelElement);
	}

	public void setColor(RGB color) {
		this.color = color;
	}
	
	public void setDefinition(ModelDefinition definition) {
		this.definition = definition;
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
