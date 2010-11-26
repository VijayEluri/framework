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

public class GoAction extends Action {

	private BrowserPanel panel;
	
	public GoAction(BrowserPanel panel) {
		super("Go", AS_PUSH_BUTTON);
		setToolTipText("Go to the address in the Location Bar");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/browser/nav_go.gif"));
		setDisabledImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/browser/nav_go_dis.gif"));
		this.panel = panel;
	}
	
	@Override
	public void run() {
		panel.go();
	}
	
}
