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
package org.oobium.eclipse.views.actions;

import org.eclipse.jface.action.Action;
import org.oobium.console.Console;
import org.oobium.eclipse.OobiumPlugin;

public class ClearAction extends Action {

	private Console console;
	
	public ClearAction(Console console) {
		super("Clear Console", AS_PUSH_BUTTON);
		setToolTipText("Clear Console");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/clear.gif"));
		this.console = console;
	}
	
	@Override
	public void run() {
		console.clear();
	}
	
}
