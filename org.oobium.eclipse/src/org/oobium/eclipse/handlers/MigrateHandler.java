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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.oobium.eclipse.OobiumNature;
import org.oobium.eclipse.dialogs.ProjectSelectionDialog;

public class MigrateHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			
			List<IProject> projects = new ArrayList<IProject>();
			for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				if(project.isOpen() && project.getNature(OobiumNature.ID) != null) {
					projects.add(project);
				}
			}
			if(projects.isEmpty()) {
				MessageDialog.openError(shell, "No Projects",
						"There are no web projects in the current workspace.  \n"
								+ "You must create or import one before using this wizard.");
			} else {
				ProjectSelectionDialog dlg = new ProjectSelectionDialog(shell, projects);
				dlg.setSingleSelection(true);
				if(Dialog.OK == dlg.open()) {
					IProject project = dlg.getProject();
					IFile file = project.getFile("/generated/" + project.getName().replace('.', '/') + "/models/schema.sql");
					if(file.exists()) {
						throw new UnsupportedOperationException("not implemented yet (client is missing)");
//						Client client = new Client("localhost", 5000);
//						ClientResponse response = client.migrate(file.getLocation().toFile());
//						if(response.success()) {
//							MessageBox msg = new MessageBox(shell, SWT.OK);
//							msg.setMessage("migration completed");
//							msg.open();
//						} else {
//							MessageBox msg = new MessageBox(shell, SWT.OK);
//							if(response.exceptionThrown()) {
//								msg.setMessage(response.getException().getMessage());
//								response.getException().printStackTrace();
//							} else {
//								msg.setMessage("migration failed");
//							}
//							msg.open();
//						}
					}
				}
			}
		} catch(CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}
