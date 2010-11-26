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
package org.oobium.build.console.commands.create;

import java.io.File;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Module;

public class ControllerCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Module module = getModule();
		File controller = module.getController(param(0));
		if(controller.exists()) {
			String confirm = ask(param(0) + " already exists. Overwrite?[Y/N] ");
			if(!confirm.equalsIgnoreCase("Y")) {
				console.out.println("operation cancelled");
				return;
			}
		}

		module.createController(controller);
		String name = module.getControllerName(controller);
		console.out.println("created controller <a href=\"open controller " + name + "\">" + name + "</a>");
		BuilderConsoleActivator.sendRefresh(module, controller, 100);
	}

}
