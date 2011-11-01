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
package org.oobium.eclipse.esp.config.actions;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.esp.EspPlugin;
import org.oobium.eclipse.esp.config.ConfigOutlinePage;

public class MergeAction extends Action {

	private ConfigOutlinePage page;
	
	public MergeAction(ConfigOutlinePage page) {
		super("Merge", AS_CHECK_BOX);
		setToolTipText("Merge");
		setImageDescriptor(EspPlugin.getImageDescriptor("/icons/sql-join-inner.png"));
		this.page = page;
	}
	
	@Override
	public void run() {
		page.setMerge(isChecked());
	}
	
}
