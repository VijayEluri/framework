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
package org.oobium.eclipse.wizards.mailer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.oobium.eclipse.wizards.ProjectWizard;

public class NewMailerWizard extends ProjectWizard {
	public static final String ID = NewMailerWizard.class.getCanonicalName();
	
	private NewMailerWizardPage page1;

	public NewMailerWizard() {
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page1 = new NewMailerWizardPage("New Mailer");
		page1.setProject(getSelectedProject(selection));
		addPage(page1);
	}
	
	@Override
	public boolean performFinish() {
		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor) {
//					IProject project = page1.getProject();
//					String name = page1.getName();
//					if(monitor == null) monitor = new NullProgressMonitor(); 
//					ElementGenerator.createViewModel(project, name, monitor);
				}
			});
		} catch(Exception e) {
			return false;
		}
		return true;
	}

}
