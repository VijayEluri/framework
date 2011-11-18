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
package org.oobium.eclipse.wizards.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.oobium.build.model.ModelDefinition;
import org.oobium.eclipse.wizards.ProjectWizard;

public class NewModelWizard extends ProjectWizard {
	public static final String ID = NewModelWizard.class.getCanonicalName();
	
	private NewModelWizardPage page1;

	public NewModelWizard() {
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page1 = new NewModelWizardPage("New Model");
		page1.setProject(getSelectedProject(selection));
		addPage(page1);
	}
	
	@Override
	public boolean performFinish() {
		ModelDefinition def = page1.getDefinition();
		System.out.println(def);
		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor) {
//					OobiumPlugin.getInstance().execute(ConsoleView.ID, "create model " + page1.getName());
				}
			});
		} catch(Exception e) {
			return false;
		}
		return true;
	}

}
