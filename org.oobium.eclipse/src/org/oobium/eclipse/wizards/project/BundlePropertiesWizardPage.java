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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.oobium.eclipse.OobiumPlugin;

public class BundlePropertiesWizardPage extends WizardPage implements IWizardPage {

	private Map<String, String> properties;
	
	protected BundlePropertiesWizardPage(String pageName) {
		super(pageName);
		
		setTitle("Set Bundle Properties");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/wizards/newpprj_wiz.png"));

		properties = new HashMap<String, String>();
		
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        initializeDialogUnits(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group group = new Group(composite, SWT.NONE);
		group.setText("Bundle Properties");
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 15;
		layout.marginWidth = 15;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label lbl = new Label(group, SWT.NONE);
		lbl.setText("Name:");
		lbl.setToolTipText("The real-world name of the bundle (the Bundle-Name property in the bundle's manifest)");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		Text txt = new Text(group, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		txt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String s = ((Text) e.widget).getText();
				if(s.length() == 0) {
					properties.remove("Bundle-Name");
				}
				properties.put("Bundle-Name", s);
			}
		});
		
		lbl = new Label(group, SWT.NONE);
		lbl.setText("Provider:");
		lbl.setToolTipText("The name of the bundle's provider, or vendor (the Bundle-Vendor property in the bundle's manifest)");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		txt = new Text(group, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		txt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String s = ((Text) e.widget).getText();
				if(s.length() == 0) {
					properties.remove("Bundle-Vendor");
				}
				properties.put("Bundle-Vendor", s);
			}
		});

        setControl(composite);
        Dialog.applyDialogFont(composite);
	}

	public Map<String, String> getProperties() {
		return properties;
	}
	
	@Override
	public IWizardPage getNextPage() {
		return null;
	}
	
	protected boolean validatePage() {
		return true;
	}
	
}
