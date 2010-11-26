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
package org.oobium.eclipse.wizards.testsuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.oobium.eclipse.wizards.ProjectWizardPage;

public class NewTestSuiteWizardPage extends ProjectWizardPage {

	protected NewTestSuiteWizardPage(String pageName, IProject website) {
		super(pageName, website);
	}

	@Override
	protected void createContents(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean validate() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
