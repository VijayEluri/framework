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

public class StopAction extends Action {

	private BrowserPanel panel;
	
	public StopAction(BrowserPanel panel) {
		super("Stop", AS_PUSH_BUTTON);
		setToolTipText("Stop loading this page");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/browser/nav_stop.gif"));
		setDisabledImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/browser/nav_stop_dis.gif"));
		this.panel = panel;
	}
	
	@Override
	public void run() {
		panel.stop();
	}
	
}
