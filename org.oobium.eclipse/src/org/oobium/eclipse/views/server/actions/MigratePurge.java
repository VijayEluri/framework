package org.oobium.eclipse.views.server.actions;

import static org.oobium.client.Client.client;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.oobium.eclipse.OobiumPlugin;

public class MigratePurge extends Action {

	public MigratePurge() {
		super("Purge Database", AS_PUSH_BUTTON);
		setToolTipText("Purge / Drop the Database");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/database_lightning.png"));
	}
	
	@Override
	public void run() {
		MessageBox msg = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
		msg.setText("Purge Database");
		msg.setMessage("This will destroy all data!!!  Continue anyway?");
		int r = msg.open();
		if(r == SWT.YES) {
			new Thread() {
				@Override
				public void run() {
					client("localhost", 5001).post("/migrate/purge");
				}
			}.start();
		}
	}
	
}
