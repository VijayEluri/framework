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

import static org.oobium.persist.ModelDescription.DEFAULT_ALLOW_DELETE;
import static org.oobium.persist.ModelDescription.DEFAULT_ALLOW_UPDATE;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.workspace.Application;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.wizards.ProjectWizardPage;
import org.oobium.eclipse.wizards.model.forms.AttributesForm;
import org.oobium.eclipse.wizards.model.forms.IndexesForm;
import org.oobium.eclipse.wizards.model.forms.ValidationsForm;

public class NewModelWizardPage extends ProjectWizardPage {
	public static final String ID = NewModelWizardPage.class.getCanonicalName();

	private Application application;
	private String name;
	
	public Combo nameCombo;
	private AttributesForm attributesForm;
	private ValidationsForm validationsForm;
	private IndexesForm indexesForm;
	private Button allowUpdateBtn;
	private Button allowDeleteBtn;
	
	private final List<String> modelNames;
	private final Map<String, ModelDefinition> systemModels;
	
	private final NewModelValidator validator;

	protected NewModelWizardPage(String pageName) {
		super(pageName);
		
		modelNames = new ArrayList<String>();
		
		systemModels = new LinkedHashMap<String, ModelDefinition>();
		for(ModelDefinition def : ModelDefinition.getSystemDefinitions()) {
			systemModels.put(def.getSimpleName(), def);
		}
		
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
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 15;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, numParentColumns, 1));

		lbl = new Label(comp, SWT.NONE);
		lbl.setText("Name:");
		lbl.setFont(parent.getFont());
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		nameCombo = new Combo(comp, SWT.BORDER | SWT.DROP_DOWN);
		nameCombo.setItems(systemModels.keySet().toArray(new String[systemModels.size()]));
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		nameCombo.setLayoutData(data);
		nameCombo.setFont(parent.getFont());
		nameCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				name = ((Combo) e.widget).getText();
				setPageComplete(validate());
			}
		});
		nameCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ModelDefinition def = systemModels.get(nameCombo.getText());
				attributesForm.setModel(def);
				validationsForm.setModel(def);
				indexesForm.setModel(def);
				if(def != null) {
					allowDeleteBtn.setSelection(def.allowDelete());
					allowUpdateBtn.setSelection(def.allowUpdate());
				}
			}
		});
		nameCombo.setFocus();
		
		CTabFolder folder = new CTabFolder(comp, SWT.BORDER);
		folder.setSimple(false);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText("Attributes");
		item.setControl(attributesForm = new AttributesForm(this, folder));
		
		item = new CTabItem(folder, SWT.NONE);
		item.setText("Validations");
		item.setControl(validationsForm = new ValidationsForm(this, folder));

		item = new CTabItem(folder, SWT.NONE);
		item.setText("Indexes");
		item.setControl(indexesForm = new IndexesForm(this, folder));

		folder.setSelection(0);
		
		allowDeleteBtn = new Button(comp, SWT.CHECK);
		allowDeleteBtn.setText("Allow Delete");
		allowDeleteBtn.setSelection(DEFAULT_ALLOW_DELETE); // disregard defaults
		allowDeleteBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		allowUpdateBtn = new Button(comp, SWT.CHECK);
		allowUpdateBtn.setText("Allow Update");
		allowUpdateBtn.setSelection(DEFAULT_ALLOW_UPDATE); // disregard defaults
		allowUpdateBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
	}

	public String getName() {
		return name;
	}

	public ModelDefinition getDefinition() {
		ModelDefinition def = new ModelDefinition(name);

		def.setPackageName(application.packageName(application.getModel(name), true));
		
		attributesForm.updateModel(def);
		validationsForm.updateModel(def);
		indexesForm.updateModel(def);

		def.allowDelete(allowDeleteBtn.getSelection());
		def.allowUpdate(allowUpdateBtn.getSelection());
		
		return def;
	}
	
	public Map<String, ModelDefinition> getSystemModels() {
		return systemModels;
	}

	@Override
	public void setProject(IProject project) {
		modelNames.clear();
		application = OobiumPlugin.getWorkspace().getApplication(project.getName());
		if(application != null) {
			for(File model : application.findModels()) {
				modelNames.add(application.getModelName(model));
			}
		}
		super.setProject(project);
	}
	
	@Override
	protected boolean validate() {
		return validator.validate(getProject(), name);
	}
	
}
