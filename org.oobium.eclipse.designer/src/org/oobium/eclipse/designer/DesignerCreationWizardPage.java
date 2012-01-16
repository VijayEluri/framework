package org.oobium.eclipse.designer;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.oobium.eclipse.designer.editor.models.SiteElement;

public class DesignerCreationWizardPage extends WizardNewFileCreationPage {

	private final IWorkbench workbench;
	
	public DesignerCreationWizardPage(IWorkbench workbench, IStructuredSelection selection) {
		super("designerCreationPage", selection);
		this.workbench = workbench;
		setTitle("Create a new .models file");
		setDescription("Create a new .models file");
	}

	public SiteElement createDefaultContent() {
		return new SiteElement(null);
	}
	
	boolean finish() {
		IFile newFile = createNewFile();
		
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		if(newFile != null && page != null) {
			try {
				IDE.openEditor(page, newFile, true);
			} catch(PartInitException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	protected InputStream getInitialContents() {
		// TODO
		return super.getInitialContents();
	}
	
	private boolean validateFilename() {
		String name = getFileName();
		if(name != null && name.endsWith(".models")) {
			return true;
		}
		setErrorMessage("File name must end with .models");
		return false;
	}
	
	@Override
	protected boolean validatePage() {
		return super.validatePage() && validateFilename();
	}
	
}
