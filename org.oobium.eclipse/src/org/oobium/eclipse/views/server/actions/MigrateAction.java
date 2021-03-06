package org.oobium.eclipse.views.server.actions;

import static org.oobium.client.Client.client;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.OobiumPlugin;

public class MigrateAction extends Action {

	public MigrateAction() {
		super("Migrate", AS_PUSH_BUTTON);
		setToolTipText("Migrate the database");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/database_go.png"));
	}
	
	@Override
	public void run() {
		new Thread() {
			@Override
			public void run() {
				client("localhost", 5001).post("/migrate");
			}
		}.start();
	}
	
}
