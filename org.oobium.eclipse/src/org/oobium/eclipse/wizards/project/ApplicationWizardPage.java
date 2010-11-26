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
package org.oobium.eclipse.wizards.project;

import org.oobium.eclipse.OobiumPlugin;

public class ApplicationWizardPage extends ModuleWizardPage {

	public ApplicationWizardPage(String pageName) {
		super(pageName);

		setTitle("Create an Oobium Application");
		setMessage("Create an Oobium Application in the workspace.");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/wizards/java_app_wiz.png"));
	}
	
}
