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

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.utils.FileUtils;

public class ModelCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}

	@Override
	public void run() {
		retainFlags('f', 'm', 'v', 'c');
		if(!hasFlags() || (flagCount() == 1 && flag('f'))) {
			setFlag('m', 'v', 'c');
		}
		
		Module module = getModule();
		String modelName = param(0);
		
		if(flag('m')) {
			String confirm = flag('f') ? "y" : ask("Permanently delete the Model from the file system?[Y/N] ");
			if(!"Y".equalsIgnoreCase(confirm)) {
				console.out.println("operation cancelled");
				return;
			}
			module.destroyModel(modelName);
		}
		
		if(flag('v')) {
			String confirm = flag('f') ? "y" : ask("Permanently delete the Views folder, and all contents, from the file system?[Y/N] ");
			if(!"Y".equalsIgnoreCase(confirm)) {
				console.out.println("operation cancelled");
				return;
			}
			FileUtils.delete(module.getViewsFolder(modelName));
		}

		if(flag('c')) {
			String confirm = flag('f') ? "y" : ask("Permanently delete the Controller from the file system?[Y/N] ");
			if(!"Y".equalsIgnoreCase(confirm)) {
				console.out.println("operation cancelled");
				return;
			}
			FileUtils.delete(module.getControllerFor(modelName));
		}

		Eclipse.refreshProject(module.name);
	}
	
}
