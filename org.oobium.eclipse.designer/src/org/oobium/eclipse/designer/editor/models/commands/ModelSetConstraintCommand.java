package org.oobium.eclipse.designer.editor.models.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class ModelSetConstraintCommand extends Command {

	private final ModelElement model;
	private final Rectangle newBounds;
	private final Rectangle oldBounds;
	
	
	public ModelSetConstraintCommand(ModelElement model, Rectangle bounds) {
		this.model = model;
		this.newBounds = bounds.getCopy();
		this.oldBounds = model.getBounds();
	}

	@Override
	public void execute() {
		model.setLocation(newBounds.getLocation());
		model.setSize(newBounds.getSize());
	}
	
	@Override
	public void undo() {
		model.setLocation(oldBounds.getLocation());
		model.setSize(oldBounds.getSize());
	}
	
}
