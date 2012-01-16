package org.oobium.eclipse.designer.wizards.project;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.designer.dialogs.ServiceSelectionDialog;
import org.oobium.eclipse.designer.manager.DataServiceManager;

public class NewBackendServiceWizardPage extends ProjectWizardPage {

	private String serviceName;
	private Text serviceTxt;
	private Button browseBtn;
	
	protected NewBackendServiceWizardPage(String pageName, IProject project) {
		super(pageName, project);

		setTitle("Create a new backend service");
		setMessage("Create a new backend service for the selected project.");
		setImageDescriptor(OobiumPlugin.getImageDescriptor("/icons/wizards/newprj_wiz.gif"));

		setPageComplete(project != null);
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	private void browseServices() {
		List<String> services = DataServiceManager.instance().getServices();
		if(services.isEmpty()) {
			MessageDialog.openError(getShell(), "No Services", "There are no services present.");
		} else {
			ServiceSelectionDialog dlg = new ServiceSelectionDialog(getShell(), services);
			dlg.setSingleSelection(true);
			if(Dialog.OK == dlg.open()) {
				String service = dlg.getService();
				if(this.serviceName != service) {
					this.serviceName = service;
					serviceTxt.setText(service);
				}
			}
		}
	}

	@Override
	protected void createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label lbl = new Label(comp, SWT.NONE);
		lbl.setText("Service Name:");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		serviceTxt = new Text(comp, SWT.BORDER);
		if(serviceName != null) {
			serviceTxt.setText(serviceName);
		} else {
			handleProjectChanged();
		}
		serviceTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		serviceTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				serviceName = ((Text) e.widget).getText().trim();
				setPageComplete(validate());
			}
		});
		serviceTxt.addVerifyListener(new VerifyListener() {
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
				browseServices();
				setPageComplete(validate());
			}
		});
	}

	@Override
	protected void handleProjectChanged() {
		IProject prj = getProject();
		if(prj != null) {
			Workspace ws = OobiumPlugin.getWorkspace();
			Project project = ws.load(prj.getLocation().toFile());
			if(project != null) {
				serviceTxt.setText(serviceName = DataServiceManager.instance().getServiceName(project));
			}
		}
	}
	
	@Override
	protected boolean validate() {
		return getProject() != null && serviceName != null && serviceName.trim().length() > 0;
	}

}
