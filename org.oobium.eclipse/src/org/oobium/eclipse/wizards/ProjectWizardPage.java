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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.dialogs.ProjectSelectionDialog;

public abstract class ProjectWizardPage extends WizardPage implements IWizardPage {

	protected static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private IProject project;
	private Text projectTxt;
	private Button browseBtn;

	protected ProjectWizardPage(String pageName) {
		super(pageName);
	}

	private void browseProjects() {
		Workspace workspace = OobiumPlugin.getWorkspace();
		List<IProject> projects = new ArrayList<IProject>();
		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if(project.isOpen()) {
				Application app = workspace.getApplication(project.getName());
				if(app != null) {
					projects.add(project);
				}
			}
		}
		if(projects.isEmpty()) {
			MessageDialog.openError(getShell(), "No Projects",
					"There are no Oobium projects in the current workspace.  \n"
							+ "You must create or import one before using this wizard.");
		} else {
			ProjectSelectionDialog dlg = new ProjectSelectionDialog(getShell(), projects);
			dlg.setSingleSelection(true);
			dlg.setShowOnlyApplicableProjects(true);
			if(Dialog.OK == dlg.open()) {
				IProject project = dlg.getProject();
				if(this.project != project) {
					setProject(project);
				}
			}
		}
	}

	protected abstract void createContents(Composite parent);

	@Override
	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NULL);
		control.setFont(parent.getFont());

		initializeDialogUnits(parent);

		control.setLayout(new GridLayout());
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		createProjectSelector(control);
		createContents(control);

		// Scale the button based on the rest of the dialog
		// setButtonLayoutData(locationArea.getBrowseButton());

		setPageComplete(false);

		setErrorMessage(null);
		setMessage(null);
		setControl(control);
	}

	private void createProjectSelector(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label lbl = new Label(comp, SWT.NONE);
		lbl.setText("Oobium Project:");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		projectTxt = new Text(comp, SWT.BORDER);
		if(project != null) {
			projectTxt.setText(project.getName());
		}
		projectTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		projectTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String websiteName = ((Text) e.widget).getText().trim();
				if(websiteName.length() > 0) {
					setProject(ResourcesPlugin.getWorkspace().getRoot().getProject(websiteName));
				} else {
					setProject(null);
				}
			}
		});
		projectTxt.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				if(e.character == ' ') {
					e.doit = false;
				}
			}
		});

		browseBtn = new Button(comp, SWT.PUSH);
		browseBtn.setText("Browse...");
		browseBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		browseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseProjects();
				setPageComplete(validate());
			}
		});
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
		if(projectTxt != null) {
			if(project == null) {
				projectTxt.setText("");
			} else {
				projectTxt.setText(project.getName());
			}
			setPageComplete(validate());
		}
	}
	
	protected abstract boolean validate();
	
}
