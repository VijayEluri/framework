package org.oobium.eclipse.designer.editor.models.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class ModelEditCommand extends Command {

	private ModelElement model;

	private List<ModelAttribute> oldAttributes;
	private boolean oldTimestamps;
	private boolean oldDatestamps;
	private boolean oldAllowUpdate;
	private boolean oldAllowDelete;

	private String[][] attributes;
	private boolean timestamps = true;
	private boolean datestamps;
	private boolean allowUpdate = true;
	private boolean allowDelete = true;
	
	
	public ModelEditCommand(ModelElement model) {
		this.model = model;
	}
	
	@Override
	public boolean canExecute() {
		return model != null && attributes != null;
	}

	private boolean hasAttribute(String name) {
		for(String[] attribute : attributes) {
			if(attribute[0].equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void execute() {
		ModelDefinition definition = model.getDefinition();

		oldAttributes = new ArrayList<ModelAttribute>();
		for(ModelAttribute attr : definition.getAttributes(false)) {
			oldAttributes.add(attr.getCopy());
		}

		oldTimestamps = definition.timestamps;
		oldDatestamps = definition.datestamps;
		oldAllowUpdate = definition.allowUpdate;
		oldAllowDelete = definition.allowDelete;

		redo();
	}
	
	@Override
	public void redo() {
		if(attributes.length == 0) {
			if(model.hasAttributes()) {
				for(ModelAttribute attribute : model.getDefinition().getAttributes()) {
					model.remove(attribute.name);
				}
			}
		} else if(!model.hasAttributes()) {
			for(String[] attribute : attributes) {
				model.setAttribute(attribute[0], attribute[1]);
			}
		} else {
			for(String[] attribute : attributes) {
				model.setAttribute(attribute[0], attribute[1]);
			}
			for(ModelAttribute attribute : model.getDefinition().getAttributes()) {
				if(!hasAttribute(attribute.name)) {
					model.remove(attribute.name);
				}
			}
		}
		
		model.setTimestamps(timestamps);
		model.setDatestamps(datestamps);
		model.setAllowUpdate(allowUpdate);
		model.setAllowDelete(allowDelete);

		if(attributes.length > 0) {
			String[] names = new String[attributes.length];
			for(int i = 0; i < attributes.length; i++) {
				names[i] = attributes[i][0];
			}
			model.setAttributeOrder(names);
		}
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
	
	public void setTimestamps(boolean timestamps) {
		this.timestamps = timestamps;
	}

	@Override
	public void undo() {
		for(ModelAttribute attribute : model.getDefinition().getAttributes()) {
			model.remove(attribute.name);
		}
		for(ModelAttribute attribute : oldAttributes) {
			model.setAttribute(attribute);
		}
		model.setTimestamps(oldTimestamps);
		model.setDatestamps(oldDatestamps);
		model.setAllowUpdate(oldAllowUpdate);
		model.setAllowDelete(oldAllowDelete);
	}
	
}
