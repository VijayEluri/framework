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

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project.Type;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.developer.ConsoleView;
import org.oobium.eclipse.wizards.ProjectWizard;

public class NewProjectWizard extends ProjectWizard implements INewWizard {
	
	public static final String ID = NewProjectWizard.class.getCanonicalName();
	
	public NewProjectWizardPage projectPage;
	public ApplicationWizardPage applicationPage;
	public ModuleWizardPage modulePage;
//	public BundlePropertiesWizardPage bundlePropsPage;

	public NewProjectWizard() {
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		projectPage = new NewProjectWizardPage("SelectType");
		addPage(projectPage);
		
		applicationPage = new ApplicationWizardPage("NewApp");
		addPage(applicationPage);
		
		modulePage = new ModuleWizardPage("NewApp");
		addPage(modulePage);
		
//		bundlePropsPage = new BundlePropertiesWizardPage("");
//		addPage(bundlePropsPage);
	}
	
	@Override
	public boolean performFinish() {
		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				protected void execute(IProgressMonitor monitor) {
//					OobiumPlugin.getInstance().execute(ConsoleView.ID, "create application -f " + projectPage.getProjectName());
					
					if(monitor == null) {
						monitor = new NullProgressMonitor();
					}
					
					File location = projectPage.getLocationPath().toFile();
					String name = projectPage.getProjectName();
					File file = new File(location, name);

					boolean isApp = projectPage.getType() == Type.Application;
//					Map<String, String> properties = bundlePropsPage.getProperties();

					Workspace workspace = OobiumPlugin.getWorkspace();
					Module module;
					if(isApp) {
						module = workspace.createApplication(file, new HashMap<String, String>(0));
					} else {
						module = workspace.createModule(file, new HashMap<String, String>(0));
					}

					openProject(module, location, monitor);
					
					if(isApp ? applicationPage.createMigration() : modulePage.createMigration()) {
						Bundle bundle = workspace.createMigrator(module);
						openProject(bundle, location, monitor);
					}

					if(isApp ? applicationPage.createTestSuite() : modulePage.createTestSuite()) {
						Bundle bundle = workspace.createTestSuite(module);
						openProject(bundle, location, monitor);
					}
				}
			});
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	private void openProject(Bundle bundle, File location, IProgressMonitor monitor) {
		try {
			String name = bundle.file.getName();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IProjectDescription description = root.getWorkspace().newProjectDescription(name);
			if(location.equals(root.getLocation().toFile())) {
				description.setLocationURI(null); // default
			} else {
				description.setLocationURI(new File(location, name).toURI());
			}

			IProject project = root.getProject(name);
			project.create(description, monitor);
			project.open(monitor);

//			String path = bundle.file.getAbsolutePath();
//			path = bundle.activator.getAbsolutePath().substring(path.length()); // activator path relative to project
//			
//			final IFile file = project.getFile(path);
//			file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
//			Display.getDefault().asyncExec(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
//					    IEditorPart editor = IDE.openEditor(window.getActivePage(), file, true);
//						editor.setFocus();
//					} catch(PartInitException e) {
//						e.printStackTrace();
//					}
//				}
//			});
		} catch(CoreException e) {
			e.printStackTrace();
		}
	}
	
}
