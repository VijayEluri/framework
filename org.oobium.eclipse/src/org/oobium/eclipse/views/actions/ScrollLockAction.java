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

public class ScrollLockAction extends Action {

	private Console console;
	
	public ScrollLockAction(Console console) {
		super("Scroll Lock", AS_CHECK_BOX);
		setToolTipText("Scroll Lock");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/scroll_lock.gif"));
		this.console = console;
	}
	
	@Override
	public void run() {
		console.setScrollLock(isChecked());
	}
	
}
