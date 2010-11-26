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
package org.oobium.eclipse.wizards.view;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.oobium.eclipse.wizards.ProjectValidator;

public class NewViewValidator extends ProjectValidator {

	public NewViewValidator(WizardPage page) {
		super(page);
	}
	
	public boolean validate(String name) {
		if(name == null || name.length() == 0) {
			page.setErrorMessage("Page Name cannot be blank");
		} else if(!Character.isLetter(name.charAt(0))) {
			page.setErrorMessage("Page Name must begin with a letter (preferably uppercase)");
		} else {
			boolean invalidFormat = false;
			for(char ch : name.toCharArray()) {
				if(ch != '_' && !Character.isLetterOrDigit(ch)) {
					invalidFormat = true;
					break;
				}
			}
			if(invalidFormat) {
				page.setErrorMessage("Page Name can only contain letters, digits and underscores");
			} else {
				page.setErrorMessage(null);
			}
		}
		
		if(name != null && name.length() > 0 && Character.isLowerCase(name.charAt(0))) {
			page.setMessage("Page Name should begin with an uppercase letter", DialogPage.WARNING);
		} else {
			page.setMessage(null);
		}
		
		return page.getErrorMessage() == null;
	}

	public boolean validate(IProject website, String name) {
		if(validate(website)) {
			return validate(name);
		}
		return false;
	}
	
}
