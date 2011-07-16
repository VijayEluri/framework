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

import java.io.File;

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
		retainFlags('f', 'm', 'v', 'c', 'n');
		if(!hasFlags() || (flagCount() == 1 && flag('f'))) {
			setFlag('m', 'v', 'c', 'n');
		}
		
		Module module = getModule();
		File model = module.getModel(param(0));
		String modelName = module.getModelName(model);
		
		if(flag('m')) {
			if(model.exists()) {
				String confirm = flag('f') ? "y" : ask("Permanently delete the " + modelName + " model from the file system?[Y/N] ");
				if(!"Y".equalsIgnoreCase(confirm)) {
					console.out.println("operation cancelled");
					return;
				}
				module.destroyModel(modelName);
			}
		}
		
		if(flag('v')) {
			File viewsFolder = module.getViewsFolder(modelName);
			if(viewsFolder.exists()) {
				String confirm = flag('f') ? "y" : ask("Permanently delete the " + modelName + " views folder, and all contents, from the file system?[Y/N] ");
				if(!"Y".equalsIgnoreCase(confirm)) {
					console.out.println("operation cancelled");
					return;
				}
				FileUtils.delete(viewsFolder);
			}
		}

		if(flag('c')) {
			File controller = module.getControllerFor(modelName);
			if(controller.exists()) {
				String controllerName = module.getControllerName(controller);
				String confirm = flag('f') ? "y" : ask("Permanently delete the " + controllerName + " from the file system?[Y/N] ");
				if(!"Y".equalsIgnoreCase(confirm)) {
					console.out.println("operation cancelled");
					return;
				}
				FileUtils.delete(controller);
			}
		}

		if(flag('n')) {
			File notifier = module.getNotifier(modelName);
			if(notifier.exists()) {
				String notifierName = notifier.getName();
				notifierName = notifierName.substring(0, name.length() - 5);
				String confirm = flag('f') ? "y" : ask("Permanently delete the " + notifierName + " from the file system?[Y/N] ");
				if(!"Y".equalsIgnoreCase(confirm)) {
					console.out.println("operation cancelled");
					return;
				}
				FileUtils.delete(notifier);
			}
		}

		Eclipse.refreshProject(module.name);
	}
	
}
