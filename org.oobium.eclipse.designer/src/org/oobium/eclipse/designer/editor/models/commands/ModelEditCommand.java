package org.oobium.eclipse.designer.editor.models.commands;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.RGB;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.build.model.ModelValidation;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class ModelEditCommand extends Command {

	private ModelElement model;
	private ModelDefinition original;
	private ModelDefinition update;
	
	private RGB origColor;
	private RGB color;
	
	public ModelEditCommand(ModelElement model) {
		this.model = model;
	}
	
	@Override
	public boolean canExecute() {
		return model != null && update != null;
	}

	@Override
	public void execute() {
		original = model.getDefinition().getCopy();
		origColor = model.getColor();
		redo();
	}
	
	@Override
	public void redo() {
		transferToModel(update);
		model.setColor(color);
	}

	public void setColor(RGB color) {
		this.color = color;
	}
	
	public void setDefinition(ModelDefinition definition) {
		this.update = definition;
	}

	private void transferToModel(ModelDefinition definition) {
		// remove
		for(ModelAttribute attribute : model.getDefinition().getAttributes(false)) {
			model.remove(attribute.name());
		}
		for(ModelValidation validation : model.getDefinition().getValidations()) {
			model.removeValidation(validation.field());
		}

		// add
		for(ModelAttribute attribute : definition.getAttributes(false)) {
			model.setAttribute(attribute);
		}
		for(ModelRelation relation : definition.getRelations()) {
			model.setRelation(relation);
		}
		for(ModelValidation validation : definition.getValidations()) {
			model.setValidation(validation);
		}

		model.setTimestamps(definition.timestamps());
		model.setDatestamps(definition.datestamps());
		model.setAllowUpdate(definition.allowUpdate());
		model.setAllowDelete(definition.allowDelete());
	}
	
	@Override
	public void undo() {
		transferToModel(original);
		model.setColor(origColor);
	}
	
}
