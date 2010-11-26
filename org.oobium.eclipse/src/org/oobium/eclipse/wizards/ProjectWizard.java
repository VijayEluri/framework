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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.oobium.eclipse.OobiumNature;

public abstract class ProjectWizard extends Wizard implements INewWizard {

	public IProject getSelectedProject(IStructuredSelection selection) {
		if(!selection.isEmpty()) {
			Object sel = selection.getFirstElement();
			if(sel instanceof IAdaptable) {
				sel = ((IAdaptable) sel).getAdapter(IResource.class);
				if(sel != null) {
					IProject project = ((IResource) sel).getProject();
					try {
						if(project.getNature(OobiumNature.ID) != null) {
							return project;
						}
					} catch(CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
}
