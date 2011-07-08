package org.oobium.eclipse.designer.editor.tools;

import static org.oobium.utils.StringUtils.varName;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.tools.ConnectionCreationTool;
import org.eclipse.swt.SWT;
import org.oobium.eclipse.designer.editor.dialogs.ConnectionDialog;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionCreateCommand;

public class ConnectionCreateTool extends ConnectionCreationTool {

	@Override
	protected void executeCommand(Command command) {
		if(command instanceof ConnectionCreateCommand) {
			ConnectionCreateCommand cmd = (ConnectionCreateCommand) command;
			String sourceField = varName(cmd.getTargetModel().getName());
			ConnectionDialog dlg = new ConnectionDialog(sourceField);
			if(dlg.open() == SWT.OK) {
				cmd.setSourceField(dlg.getSourceField());
				cmd.setSourceHasMany(dlg.getSourceHasMany());
				cmd.setTargetField(dlg.getTargetField());
				cmd.setTargetHasMany(dlg.getTargetHasMany());
			} else {
				return;
			}
		}
		super.executeCommand(command);
	}
	
}
