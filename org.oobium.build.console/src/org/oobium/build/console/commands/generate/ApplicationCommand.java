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
package org.oobium.build.console.commands.generate;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Application;

public class ApplicationCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}
	
	@Override
	public void run() {
		Application app = getApplication();
		
		long start = System.currentTimeMillis();
		
		if(flag('c')) {
			app.clean();
		}
		
		app.generate(getWorkspace());

		if(flag('v')) {
			console.out.println("compiled " + app.name + " in " + (System.currentTimeMillis() - start) + "ms");
		}
		
		BuilderConsoleActivator.sendRefresh(app, 1000);
	}
	
}
