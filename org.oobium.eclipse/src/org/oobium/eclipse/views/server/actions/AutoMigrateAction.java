package org.oobium.eclipse.views.server.actions;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.server.ServerView;

public class AutoMigrateAction extends Action {

	private ServerView view;
	
	public AutoMigrateAction(ServerView view) {
		super("AutoMigrate", AS_CHECK_BOX);
		setToolTipText("Link with migration changes to update the database automatically");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/database_synced.png"));
		this.view = view;
		setChecked(true);
	}
	
	@Override
	public void run() {
		view.setAutoMigrate(isChecked());
	}
	
}
