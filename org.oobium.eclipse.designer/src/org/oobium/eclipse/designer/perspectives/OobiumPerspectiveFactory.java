package org.oobium.eclipse.designer.perspectives;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.progress.IProgressConstants;
import org.oobium.eclipse.wizards.OobiumWizards;

public class OobiumPerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();

		IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder.addView(JavaUI.ID_PACKAGES);
		folder.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
		folder.addPlaceholder("org.eclipse.ui.views.ResourceNavigator");
		folder.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout outlineFolder = layout.createFolder("right", IPageLayout.RIGHT, (float)0.75, editorArea); //$NON-NLS-1$
		outlineFolder.addView(IPageLayout.ID_OUTLINE);

		IFolderLayout monitorFolder = layout.createFolder("monitor", IPageLayout.BOTTOM, (float)0.65, "right"); //$NON-NLS-1$
		monitorFolder.addView("org.oobium.eclipse.designer.monitor.MonitorView");
		
		IFolderLayout outputfolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.65, editorArea); //$NON-NLS-1$
		outputfolder.addView("org.oobium.eclipse.views.developer.ConsoleView");
		outputfolder.addView("org.oobium.eclipse.designer.data.DataView");
		outputfolder.addPlaceholder("org.oobium.eclipse.views.server.ServerView");
		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		outputfolder.addPlaceholder(JavaUI.ID_JAVADOC_VIEW);
		outputfolder.addPlaceholder(JavaUI.ID_SOURCE_VIEW);
		outputfolder.addPlaceholder("org.eclipse.search.ui.views.SearchView");
		outputfolder.addPlaceholder("org.eclipse.ui.console.ConsoleView");
		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);


		layout.addActionSet("org.eclipse.debug.ui.launchActionSet");
		layout.addActionSet(JavaUI.ID_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

		// views - java
		layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
		layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
		layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);


		// views - search
		layout.addShowViewShortcut("org.eclipse.search.ui.views.SearchView");

		// views - debugging
		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut("org.eclipse.ui.views.ResourceNavigator");
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$

		// new actions - Java project creation wizard
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.oobium.eclipse.wizards.project.NewProjectWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.oobium.eclipse.designer.wizards.project.NewBackendServiceWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
		layout.addNewWizardShortcut(OobiumWizards.ID_NEW_MODEL);
		layout.addNewWizardShortcut(OobiumWizards.ID_NEW_VIEW);
		layout.addNewWizardShortcut(OobiumWizards.ID_NEW_CONTROLLER);
		layout.addNewWizardShortcut(OobiumWizards.ID_NEW_OBSERVER);
		layout.addNewWizardShortcut(OobiumWizards.ID_NEW_MAILER);
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

		// 'Window' > 'Open Perspective' contributions
		layout.addPerspectiveShortcut(JavaUI.ID_PERSPECTIVE);
		layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective");
	}

}
