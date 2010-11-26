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
package org.oobium.eclipse.wizards.migration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.oobium.eclipse.wizards.ProjectWizard;

public class NewMigrationWizard extends ProjectWizard {
	public static final String ID = NewMigrationWizard.class.getCanonicalName();

	private NewMigrationWizardPage page1;
	
	public NewMigrationWizard() {
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page1 = new NewMigrationWizardPage("New Web Page", getSelectedProject(selection));
		addPage(page1);
	}
	
	@Override
	public boolean performFinish() {
		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor) {
					if(monitor == null) monitor = new NullProgressMonitor(); 
//					Client client = new Client("localhost", 5000);
					
//					ClientResponse response = client.migrate(sql);
				}
			});
		} catch(Exception e) {
			return false;
		}
		return true;
	}

}
