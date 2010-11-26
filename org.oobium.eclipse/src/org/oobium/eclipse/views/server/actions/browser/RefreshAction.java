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
package org.oobium.eclipse.views.server.actions.browser;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.server.BrowserPanel;

public class RefreshAction extends Action {

	private BrowserPanel panel;
	
	public RefreshAction(BrowserPanel panel) {
		super("Refresh", AS_PUSH_BUTTON);
		setToolTipText("Refresh the current page");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/browser/nav_refresh.gif"));
		setDisabledImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/browser/nav_refresh_dis.gif"));
		this.panel = panel;
	}
	
	@Override
	public void run() {
		panel.refresh();
	}
	
}
