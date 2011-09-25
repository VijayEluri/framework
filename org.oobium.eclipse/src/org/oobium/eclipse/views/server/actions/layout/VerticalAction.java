/*******************************************************************************
 * Copyright (c) 2011 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.views.server.actions.layout;

import static org.oobium.eclipse.views.server.ServerView.Layout.Vertical;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.views.server.ServerView;

public class VerticalAction extends Action {

	private ServerView view;
	
	public VerticalAction(ServerView view) {
		super("Vertical", AS_RADIO_BUTTON);
		setToolTipText("Vertical Layout");
		this.view = view;
	}
	
	@Override
	public void run() {
		view.setLayout(Vertical);
	}
	
}
