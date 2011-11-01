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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.oobium.eclipse.OobiumPlugin;

public class ModuleWizardPage extends WizardPage implements IWizardPage {

	private boolean createMigration;
	private boolean createTestSuite;
	
	public ModuleWizardPage(String pageName) {
		super(pageName);

		setTitle("Create an Oobium Module");
		setMessage("Create an Oobium Module in the workspace.");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/wizards/newpprj_wiz.png"));
		
		createMigration = true;
		createTestSuite = true;
		
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        initializeDialogUnits(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Button b;
		
		b = new Button(composite, SWT.CHECK);
		b.setText("Create a Migration");
		b.setSelection(createMigration);
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createMigration = ((Button) e.widget).getSelection();
			}
		});

		b = new Button(composite, SWT.CHECK);
		b.setText("Create a Test Suite");
		b.setSelection(createTestSuite);
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createTestSuite = ((Button) e.widget).getSelection();
			}
		});

        setControl(composite);
        Dialog.applyDialogFont(composite);
	}

	public boolean createMigration() {
		return createMigration;
	}
	
	public boolean createTestSuite() {
		return createTestSuite;
	}
	
	@Override
	public IWizardPage getNextPage() {
		return null; // ((NewProjectWizard) getWizard()).bundlePropsPage;
	}

	@Override
	public IWizardPage getPreviousPage() {
		return ((NewProjectWizard) getWizard()).projectPage;
	}
	
}
