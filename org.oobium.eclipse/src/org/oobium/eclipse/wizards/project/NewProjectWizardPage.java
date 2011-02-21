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

import static org.eclipse.swt.SWT.FILL;
import static org.eclipse.swt.SWT.RADIO;
import static org.oobium.build.workspace.Project.Type.Application;
import static org.oobium.build.workspace.Project.Type.Module;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Project;
import org.oobium.eclipse.OobiumPlugin;

public class NewProjectWizardPage extends WizardNewProjectCreationPage implements IWizardPage {

	private Button[] b;
	private Project.Type type;
	private boolean webservice;
	
	protected NewProjectWizardPage(String pageName) {
		super(pageName);
		setTitle("Create a new Oobium Project");
		setMessage("Select the type of Oobium Project to create.");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/wizards/newprj_wiz.gif"));

		setPageComplete(true);

		type = Application;
	}

	public Bundle.Type getType() {
		return type;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		Composite comp = (Composite) getControl();

		Group group = new Group(comp, SWT.NONE);
		group.setText("Project Type");
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		group.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.verticalIndent = 15;
		group.setLayoutData(data);
		
		b = new Button[4];
		
		b[0] = new Button(group, RADIO);
		b[0].setSelection(true);
		b[0].setImage(OobiumPlugin.getImage("/icons/application_label.png"));
		b[0].setText("Application");
		b[0].setLayoutData(new GridData(FILL, FILL, true, false));
		b[0].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection();
			}
		});

		b[1] = new Button(group, RADIO);
		b[1].setImage(OobiumPlugin.getImage("/icons/bundle_label.png"));
		b[1].setText("Module");
		b[1].setLayoutData(new GridData(FILL, FILL, true, false));
		b[1].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSelection();
			}
		});
		
		Button b = new Button(comp, SWT.CHECK);
		b.setText("This project is a Webservice");
		b.setToolTipText("Webservices do not include a UI (this setting can always be changed later)");
		b.setSelection(false);
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				webservice = ((Button) e.widget).getSelection();
			}
		});
	}

	@Override
	public IWizardPage getPreviousPage() {
		return ((NewProjectWizard) getWizard()).projectPage;
	}


	private void updateSelection() {
		if(b[0].getSelection())			type = Application;
		else if(b[1].getSelection()) 	type = Module;
	}
	
	@Override
	public IWizardPage getNextPage() {
		switch(type) {
		case Application:	return ((NewProjectWizard) getWizard()).applicationPage;
		case Module:		return ((NewProjectWizard) getWizard()).modulePage;
		}
		throw new IllegalStateException();
	}
	
	public boolean isWebservice() {
		return webservice;
	}
	
	@Override
	protected boolean validatePage() {
		boolean valid = super.validatePage();
		setPageComplete(valid);
		return valid;
	}
	
}
