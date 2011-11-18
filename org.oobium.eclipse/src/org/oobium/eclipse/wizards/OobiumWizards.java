package org.oobium.eclipse.wizards;

import org.oobium.eclipse.wizards.controller.NewControllerWizard;
import org.oobium.eclipse.wizards.mailer.NewMailerWizard;
import org.oobium.eclipse.wizards.model.NewModelWizard;
import org.oobium.eclipse.wizards.observer.NewObserverWizard;
import org.oobium.eclipse.wizards.view.NewViewWizard;

public interface OobiumWizards {

	public static final String ID_NEW_MODEL = NewModelWizard.ID;
	
	public static final String ID_NEW_VIEW = NewViewWizard.ID;
	
	public static final String ID_NEW_CONTROLLER = NewControllerWizard.ID;
	
	public static final String ID_NEW_OBSERVER = NewObserverWizard.ID;
	
	public static final String ID_NEW_MAILER = NewMailerWizard.ID;
	
}
