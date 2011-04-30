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
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;

public class ObserverCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}

	@Override
	public void run() {
		File model;
		String modelName;
		String modelPackage;
		
		int ix = param(0).indexOf('#');
		if(ix != -1) {
			String s = param(0);
			String mname = s.substring(0, ix);
			Module module = getWorkspace().getModule(mname);
			if(module != null) {
				model = module.getModel(s.substring(ix+1, s.length()));
				modelName = module.getModelName(model);
				modelPackage = module.packageName(module.models);
			} else {
				console.err.println("module " + mname + " does not exist");
				return;
			}
		} else {
			Module module = getModule();
			model = module.getModel(param(0));
			modelName = module.getModelName(model);
			modelPackage = module.packageName(module.models);
		}

		if(!model.exists()) {
			console.err.println("model " + param(0) + " does not exist");
		} else {
			Module module = getModule();
			File observer = module.getObserver(modelName);
			if(observer.exists()) {
				String name = observer.getName();
				name = name.substring(0, name.length() - 5);
				String confirm = flag('f') ? "Y" : ask(name + " already exists. Overwrite?[Y/N] ");
				if(!confirm.equalsIgnoreCase("Y")) {
					console.out.println("operation cancelled");
					return;
				}
			}

			File file = module.createObserver(modelPackage, modelName);
			String name = file.getName();
			name = name.substring(0, name.length()-5);
			console.out.println("created observer <a href=\"open observer " + name + "\">" + name + "</a>");
			
			Eclipse.refreshProject(module.name);
		}
	}
	
}
