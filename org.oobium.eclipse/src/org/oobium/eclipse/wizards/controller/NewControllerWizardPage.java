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
package org.oobium.eclipse.wizards.controller;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.wizards.ProjectWizardPage;
import org.oobium.eclipse.wizards.model.NewModelValidator;

public class NewControllerWizardPage extends ProjectWizardPage {
	public static final String ID = NewControllerWizardPage.class.getCanonicalName();

	private String name;
//	private boolean isPersistable;
//	private boolean createView;
//	private boolean createViewController;
//	private boolean createActionController;
	
	private Text nameTxt;
	
	private NewModelValidator validator;

	protected NewControllerWizardPage(String pageName, IProject website) {
		super(pageName, website);
		validator = new NewModelValidator(this);

		setTitle("Create a new controller");
		setMessage("Create a new controller in the selected project.");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/wizards/newprj_wiz.gif"));

		setPageComplete(false);
	}

	protected void createContents(Composite parent) {
		int numParentColumns = ((GridLayout) parent.getLayout()).numColumns;
		
		Label lbl = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, numParentColumns, 1));

		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 15;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, numParentColumns, 1));

		lbl = new Label(comp, SWT.NONE);
		lbl.setText("Name:");
		lbl.setFont(parent.getFont());
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		nameTxt = new Text(comp, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
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
		
		Button persistableBtn = new Button(comp, SWT.CHECK);
		persistableBtn.setText("Persistable");
		persistableBtn.setSelection(true);
		persistableBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Group group = new Group(comp, SWT.NONE);
		group.setText("Controllers");
		group.setLayout(new GridLayout());
		data = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
		group.setLayoutData(data);
		
		Button actionsBtn = new Button(group, SWT.CHECK);
		actionsBtn.setText("Create Actions Controller");
		actionsBtn.setSelection(true);
		actionsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Button viewsBtn = new Button(group, SWT.CHECK);
		viewsBtn.setText("Create Views Controller");
		viewsBtn.setSelection(true);
		viewsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		group = new Group(comp, SWT.NONE);
		group.setText("Views");
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		
		Button viewBtn = new Button(group, SWT.CHECK);
		viewBtn.setText("Create View");
		viewBtn.setSelection(true);
		viewBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Button homeBtn = new Button(group, SWT.CHECK);
		homeBtn.setText("as Home Page");
		homeBtn.setSelection(true);
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.horizontalIndent = 25;
		homeBtn.setLayoutData(data);
		
		group = new Group(comp, SWT.NONE);
		group.setText("Routing");
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		
		Button defaultRoutingBtn = new Button(group, SWT.CHECK);
		defaultRoutingBtn.setText("Use default routing");
		defaultRoutingBtn.setSelection(true);
		defaultRoutingBtn.setEnabled(false);
		defaultRoutingBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	}

	public String getName() {
		return name;
	}

	@Override
	protected boolean validate() {
		return validator.validate(getProject(), name);
	}
	
}
