package org.oobium.eclipse.views.server.actions;

import static org.oobium.client.Client.client;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.OobiumPlugin;

public class MigrateRedoAction extends Action {

	public MigrateRedoAction() {
		super("Redo Migration", AS_PUSH_BUTTON);
		setToolTipText("Redo the current migration (migrate down 1, and then back up 1)");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/database_refresh.png"));
	}
	
	@Override
	public void run() {
		new Thread() {
			@Override
			public void run() {
				client("localhost", 5001).post("/migrate/redo");
			}
		}.start();
	}
	
}
