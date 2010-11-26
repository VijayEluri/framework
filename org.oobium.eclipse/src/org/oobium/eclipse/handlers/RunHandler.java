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
package org.oobium.eclipse.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.oobium.build.workspace.Application;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.developer.ConsoleView;

public class RunHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection sel = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		Object o = sel.getFirstElement();
		if(o instanceof IJavaProject) {
			o = ((IJavaProject) o).getProject();
		}
		if(o instanceof IProject) {
			File file = ((IProject) o).getLocation().toFile();
			Application app = OobiumPlugin.getWorkspace().getApplication(file);
			if(app != null) {
				OobiumPlugin.getInstance().execute(ConsoleView.ID, "start " + app.getName());
			}
		}
		return null;
	}

}
