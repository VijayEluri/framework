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

import java.io.File;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
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
		if(model.isFile()) {
			List<File> modified = module.generateModel(getWorkspace(), model);
			if(!modified.isEmpty()) {
				for(File file : modified) {
					Eclipse.refresh(module.file, file);
				}
			}
		} else {
			console.err.println("model does not exist: " + param(0));
		}
	}
	
}
