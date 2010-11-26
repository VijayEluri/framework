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
import org.oobium.build.workspace.Module;


public class ModuleCommand extends BundleCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 0;
	}

	@Override
	public void run() {
		Module module;
		if(paramCount() == 1) {
			module = getWorkspace().getModule(param(0));
			if(module == null) {
				console.err.println(param(0) + " does not exist, or is not a Module");
				return;
			}
		} else {
			module = getModule();
			if(module == null) {
				console.err.println("Project is not set to a Module");
				return;
			}
		}

		remove(module);
		
		BuilderConsoleActivator.sendRefresh(module, 100);
	}

}
