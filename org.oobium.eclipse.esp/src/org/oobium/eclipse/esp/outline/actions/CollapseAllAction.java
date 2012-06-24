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
package org.oobium.eclipse.esp.outline.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.oobium.eclipse.esp.EspPlugin;
import org.oobium.eclipse.esp.outline.EspOutlinePage;

public class CollapseAllAction extends Action {

	private EspOutlinePage page;
	
	public CollapseAllAction(EspOutlinePage page) {
		super("Collapse All", AS_PUSH_BUTTON);
		setToolTipText("Collapse All");
		setImageDescriptor(EspPlugin.getImageDescriptor("/icons/collapse_all.gif"));
		setDisabledImageDescriptor(EspPlugin.getImageDescriptor("/icons/collapse_all_dis.gif"));
		this.page = page;
	}
	
	@Override
	public void run() {
		for(TreeItem item : ((Tree) page.getControl()).getItems()) {
			item.setExpanded(false);
		}
	}
	
}
