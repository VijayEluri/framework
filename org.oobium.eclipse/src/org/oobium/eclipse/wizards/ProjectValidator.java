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
package org.oobium.eclipse.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.oobium.eclipse.OobiumNature;

public class ProjectValidator {

	protected WizardPage page;
	
	public ProjectValidator(WizardPage page) {
		this.page = page;
	}
	
	public boolean validate(IProject project) {
		try {
			if(project == null || !project.exists()) {
				page.setErrorMessage("Project does not exist in the workspace");
			} else if(project.getNature(OobiumNature.ID) == null) {
				page.setErrorMessage("Project is not a Oobium project");
			} else {
				return true;
			}
		} catch(CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
