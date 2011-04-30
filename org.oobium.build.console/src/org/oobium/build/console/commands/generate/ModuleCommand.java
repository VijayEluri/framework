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
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;

public class ModuleCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}
	
	@Override
	public void run() {
		Module module = getModule();
		
		long start = System.currentTimeMillis();
		
		if(flag('c')) {
			module.clean();
		}
		
		module.generate(getWorkspace());

		if(flag('v')) {
			console.out.println("compiled " + module.name + " in " + (System.currentTimeMillis() - start) + "ms");
		}
		
		Eclipse.refreshProject(module.name);
	}
	
}
