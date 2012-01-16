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
package org.oobium.eclipse.designer.views.data.actions;

import org.eclipse.jface.action.Action;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.views.data.DataView;

public class SearchAction extends Action {

//	private DataView view;
	
	public SearchAction(DataView view) {
		super("Search", AS_PUSH_BUTTON);
		setImageDescriptor(DesignerPlugin.getImageDescriptor("/icons/find.png"));
//		this.view = view;
	}

	@Override
	public void run() {
		// TODO
	}
	
}
