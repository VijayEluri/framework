package org.oobium.eclipse.designer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class DesignerCreationWizard extends Wizard implements INewWizard {

	private DesignerCreationWizardPage page;

	@Override
	public void addPages() {
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new DesignerCreationWizardPage(workbench, selection);
	}

	@Override
	public boolean performFinish() {
		return page.finish();
	}

}
