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
package org.oobium.build.console.commands.open;

import java.io.File;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Module;

public class ModelCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Module module = getModule();
		File model = module.getModel(param(0));
		if(model != null && model.isFile()) {
//			console.out.println("opening " + file);
			BuilderConsoleActivator.sendOpen(module, model);
//			if(response.isSuccess()) {
//				console.out.println("response: " + response);
//			} else {
//				console.err.println("response: " + response);
//			}
		} else {
			console.err.println("model \"" + param(0) + "\" does not exist");
		}
	}

}
