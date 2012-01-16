package org.oobium.eclipse.designer.views.data.actions;

import org.eclipse.jface.action.Action;
import org.oobium.client.Client;
import org.oobium.eclipse.designer.DesignerPlugin;

public class ResetAction extends Action {

	public ResetAction() {
		super("Reset Data", AS_PUSH_BUTTON);
		setToolTipText("Reset the Data");
		setImageDescriptor(DesignerPlugin.getImageDescriptor("/icons/database_lightning.png"));
	}
	
	@Override
	public void run() {
		new Thread() {
			@Override
			public void run() {
				Client.client("localhost", 5001).post("/migrate");
			}
		}.start();
	}
	
}
