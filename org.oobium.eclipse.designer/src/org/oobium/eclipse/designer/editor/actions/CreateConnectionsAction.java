package org.oobium.eclipse.designer.editor.actions;

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.jface.action.Action;
import org.oobium.eclipse.designer.editor.tools.ConnectionCreateTool;

public class CreateConnectionsAction extends Action {

	public static final String ID = "Designer.Create.Connections";

	private DefaultEditDomain editDomain;
	
	public CreateConnectionsAction(DefaultEditDomain editor) {
		super("Create Connections");
		setId(ID);
		this.editDomain = editor;
	}
	
	@Override
	public void run() {
		editDomain.setActiveTool(new ConnectionCreateTool());
	}
	
}
