package org.oobium.eclipse.designer.editor.tools;

import static org.oobium.utils.StringUtils.varName;

import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.tools.ConnectionEndpointTracker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.oobium.eclipse.designer.editor.dialogs.EndpointDialog;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionReconnectCommand;
import org.oobium.eclipse.designer.editor.models.commands.TargetCreateCommand;

public class ConnectionTargetTracker extends ConnectionEndpointTracker {

	public ConnectionTargetTracker(ConnectionEditPart cep, Cursor cursor) {
		super(cep);
		setCommandName(RequestConstants.REQ_RECONNECT_TARGET);
		setDefaultCursor(cursor);
	}

	protected void executeCommand(Command command) {
		super.executeCommand(command);
		if(command instanceof ConnectionReconnectCommand) {
			ConnectionReconnectCommand cmd = (ConnectionReconnectCommand) command;
			if(getCurrentInput().isControlKeyDown() && !cmd.hasTargetField()) {
				EndpointDialog dlg = new EndpointDialog();
				dlg.setField(varName(cmd.getConnection().getSourceModel().getName()));
				if(dlg.open() == SWT.OK) {
					TargetCreateCommand ecmd = new TargetCreateCommand(cmd.getConnection());
					ecmd.setField(dlg.getField());
					ecmd.setHasMany(dlg.getHasMany());
					if(cmd.canExecute()) {
						cmd.setTarget(cmd.getTargetModel(), dlg.getField());
						CompoundCommand ccmd = new CompoundCommand();
						ccmd.add(ecmd);
						ccmd.add(cmd);
						getDomain().getCommandStack().undo();
						getDomain().getCommandStack().execute(ccmd);
					} else {
						getDomain().getCommandStack().undo();
					}
				} else {
					getDomain().getCommandStack().undo();
				}
			}
		}
	};
	
}
