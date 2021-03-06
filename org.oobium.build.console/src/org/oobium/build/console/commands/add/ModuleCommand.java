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
package org.oobium.build.console.commands.add;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;

public class ModuleCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Module module = getModule();
		Module moduleToAdd = getWorkspace().getModule(param(0));
		if(moduleToAdd != null) {
			module.addModule(moduleToAdd);
			Eclipse.refreshProject(module.name);
		} else {
			console.err.println("module " + param(0) + " does not exist");
		}
	}

}
