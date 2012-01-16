/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.designer.views.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.dialogs.ServiceSelectionDialog;
import org.oobium.eclipse.designer.manager.DataServiceManager;
import org.oobium.eclipse.designer.views.IDataServiceView;
import org.oobium.utils.Config.Mode;

public class ConnectAction extends Action {

	private IDataServiceView view;
	boolean connected;
	
	public ConnectAction(IDataServiceView view, boolean connected) {
		super("Connect", AS_CHECK_BOX);
		this.view = view;
		this.connected = connected;
		setChecked(connected);
		update();
	}

	private void update() {
		if(connected) {
			setText("Disconnect");
			setToolTipText("Disconnect from the data service");
			setImageDescriptor(DesignerPlugin.getImageDescriptor("/icons/connected.png"));
		} else {
			setText("Connect");
			setToolTipText("Connect to the data service");
			setImageDescriptor(DesignerPlugin.getImageDescriptor("/icons/disconnected.png"));
		}
	}
	
	@Override
	public void run() {
		if(connected) {
			view.disconnect();
			connected = false;
		} else {
			String service = selectService();
			if(service == null) {
				setChecked(false);
			} else {
				Mode mode = Mode.DEV;
				view.connect(service, mode);
				connected = true;
			}
		}
		update();
	}
	
	private String selectService() {
		List<String> services = DataServiceManager.instance().getServices();
		if(services.isEmpty()) {
			MessageDialog.openError(view.getShell(), "No Services", "There are no services present.");
		} else {
			ServiceSelectionDialog dlg = new ServiceSelectionDialog(view.getShell(), services);
			dlg.setSingleSelection(true);
			if(Dialog.OK == dlg.open()) {
				return dlg.getService();
			}
		}
		return null;
	}
	
}
