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
package org.oobium.eclipse.wizards.observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.wizards.ProjectWizardPage;
import org.oobium.eclipse.wizards.view.NewViewValidator;

public class NewObserverWizardPage extends ProjectWizardPage {
	public static final String ID = NewObserverWizardPage.class.getCanonicalName();

	private String name;
	private Text nameTxt;
	private NewViewValidator validator;

	protected NewObserverWizardPage(String pageName) {
		super(pageName);
		validator = new NewViewValidator(this);

		setTitle("Create a new Observer");
		setMessage("Create a new Observer in the current project.");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/wizards/newprj_wiz.gif"));

		setPageComplete(false);
	}

	protected void createContents(Composite parent) {
		Label lbl = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.horizontalSpan = ((GridLayout) parent.getLayout()).numColumns;
		lbl.setLayoutData(data);

		lbl = new Label(parent, SWT.NONE);
		lbl.setText("Page Name:");
		lbl.setFont(parent.getFont());

		nameTxt = new Text(parent, SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		nameTxt.setLayoutData(data);
		nameTxt.setFont(parent.getFont());
		nameTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = ((Text) e.widget).getText();
				setPageComplete(validate());
			}
		});
	}

	public String getName() {
		return name;
	}

	@Override
	protected boolean validate() {
		return validator.validate(getProject(), name);
	}
	
}
