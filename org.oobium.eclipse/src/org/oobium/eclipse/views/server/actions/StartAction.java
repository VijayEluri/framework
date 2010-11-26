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
package org.oobium.eclipse.views.server.actions;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.server.ServerView;

public class StartAction extends Action {

	private ServerView view;
	
	public StartAction(ServerView view) {
		super("Start", AS_PUSH_BUTTON);
		setToolTipText("Start the server");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/start.gif"));
		this.view = view;
	}
	
	@Override
	public void run() {
		view.start();
	}
	
}
