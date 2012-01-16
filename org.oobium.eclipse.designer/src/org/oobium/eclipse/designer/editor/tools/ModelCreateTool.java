package org.oobium.eclipse.designer.editor.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.dialogs.Dialog;
import org.oobium.eclipse.designer.editor.dialogs.model.ModelDialog;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.SiteElement;
import org.oobium.eclipse.designer.editor.models.commands.ModelCreateCommand;
import org.oobium.eclipse.designer.editor.parts.SitePart;

public class ModelCreateTool extends CreationTool {

	public ModelCreateTool() {
		super(new CreationFactory() {
			@Override
			public Object getObjectType() {
				return ModelElement.class;
			}
			@Override
			public Object getNewObject() {
				return null;
			}
		});
	}
	
	@Override
	protected void executeCommand(Command command) {
		if(command instanceof ModelCreateCommand) {
			List<String> modelNames = new ArrayList<String>();
			for(Object o : getCurrentViewer().getRootEditPart().getChildren()) {
				if(o instanceof SitePart) {
					SiteElement site = ((SitePart) o).getModel();
					for(ApplicationElement app : site.getApplications()) {
						for(ModelElement model : app.getModels()) {
							modelNames.add(model.getName());
						}
					}
				}
			}
			ModelDialog dlg = new ModelDialog(modelNames);
			if(dlg.open() == Dialog.OK) {
				ModelCreateCommand cmd = (ModelCreateCommand) command;
				cmd.setColor(dlg.getColor());
				cmd.setDefinition(dlg.getDefinition());
			} else {
				return;
			}
		}
		super.executeCommand(command);
	}
	
}
