package org.oobium.eclipse.designer.wizards.project;

import static org.oobium.utils.FileUtils.writeFile;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumNature;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.designer.manager.DataService;
import org.oobium.eclipse.designer.manager.DataServiceManager;
import org.oobium.utils.Config.Mode;

public class NewBackendServiceWizard extends Wizard implements INewWizard {

	public static final String ID = NewBackendServiceWizard.class.getCanonicalName();

	
	public IProject getSelectedProject(IStructuredSelection selection) {
		if(!selection.isEmpty()) {
			Object sel = selection.getFirstElement();
			if(sel instanceof IAdaptable) {
				sel = ((IAdaptable) sel).getAdapter(IResource.class);
				if(sel != null) {
					IProject project = ((IResource) sel).getProject();
					try {
						if(project.getNature(OobiumNature.ID) == null) {
							return project;
						}
					} catch(CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	private NewBackendServiceWizardPage page1;

	public NewBackendServiceWizard() {
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page1 = new NewBackendServiceWizardPage("Select client project", getSelectedProject(selection));
		addPage(page1);
	}
	
	@Override
	public boolean performFinish() {
		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor) {
					Workspace ws = OobiumPlugin.getWorkspace();
					File file = page1.getProject().getLocation().toFile();
					ws.load(file);
					Project target = ws.getProject(file);
					if(target == null) {
						target = ws.load(file);
					}
					File models = new File(target.file, "oobium.models");
					if(target instanceof Module) {
						if(!models.isFile()) {
							writeFile(models, "{}");
						}
					} else {
						DataService service = DataServiceManager.instance().getService(page1.getServiceName(), Mode.DEV);
						if(!service.exists()) {
							service.create();
						}
						if(!models.isFile()) {
							writeFile(models, "{service: \"" + service.getName() + "\"}");
						}
					}
					Eclipse.openFile(target.file, models);
				}
			});
		} catch(Exception e) {
			return false;
		}
		return true;
	}

}
