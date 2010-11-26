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
package org.oobium.build.console.commands.destroy;

import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Application;


public class ApplicationCommand extends BundleCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 0;
	}

	@Override
	public void run() {
		Application application;
		if(paramCount() == 1) {
			application = getWorkspace().getApplication(param(0));
			if(application == null) {
				console.err.println(param(0) + " does not exist, or is not an Application");
				return;
			}
		} else {
			application = getApplication();
			if(application == null) {
				console.err.println("Application is not set");
				return;
			}
		}

		remove(application);
		
		BuilderConsoleActivator.sendRefresh(application, 100);
	}

}
