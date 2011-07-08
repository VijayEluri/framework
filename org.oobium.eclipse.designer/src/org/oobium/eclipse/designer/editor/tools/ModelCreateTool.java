package org.oobium.eclipse.designer.editor.tools;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.swt.SWT;
import org.oobium.eclipse.designer.editor.dialogs.ModelDialog;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.commands.ModelCreateCommand;

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
			ModelDialog dlg = new ModelDialog();
			if(dlg.open() == SWT.OK) {
				ModelCreateCommand cmd = (ModelCreateCommand) command;
				cmd.setName(dlg.getName());
				cmd.setAttributes(dlg.getAttributes());
				cmd.setAllowDelete(dlg.getAllowDelete());
				cmd.setAllowUpdate(dlg.getAllowUpdate());
				cmd.setDatestamps(dlg.getDatestamps());
				cmd.setTimestamps(dlg.getTimestamps());
			} else {
				return;
			}
		}
		super.executeCommand(command);
	}
	
}
