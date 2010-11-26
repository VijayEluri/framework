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
package org.oobium.eclipse.wizards.model;

import java.util.Map;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.wizards.ProjectWizardPage;

public class NewModelWizardPage extends ProjectWizardPage {
	public static final String ID = NewModelWizardPage.class.getCanonicalName();

	private String name;
	private Map<String, String> attrs;
	
	private Text nameTxt;
	
	private NewModelValidator validator;

	protected NewModelWizardPage(String pageName, IProject website) {
		super(pageName, website);
		validator = new NewModelValidator(this);

		setTitle("Create a new model");
		setMessage("Create a new model in the selected project.");
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
		nameTxt.setFocus();
		
		Group group = new Group(comp, SWT.NONE);
		group.setText("Attributes");
		group.setLayout(new GridLayout());
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		group.setLayoutData(data);

		Table table = new Table(group, SWT.FULL_SELECTION);
		table.setLayoutData(new GridData());
	}

	public String getName() {
		return name;
	}

	@Override
	protected boolean validate() {
		return validator.validate(getProject(), name);
	}
	
}
