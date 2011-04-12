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
import org.oobium.http.constants.Action;

public class ActionCacheCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Module module = getModule();
		String modelName = param("model");
		if(modelName == null) {
			console.err.print("model parameter not given (");
			console.out.print("help", "create action_cache help");
			console.err.println(")");
			return;
		} else {
			File model = module.getModel(modelName);
			if(model == null) {
				console.err.println("model " + modelName + " does not exist");
				return;
			} else {
				modelName = module.getModelName(model);
				String name = param("name");
				if(name == null || name.length() == 0) {
					name = modelName;
				}
				name += "Cache";
				File cache = module.getActionCache(name);
				if(cache.exists()) {
					String confirm = ask(name + " already exists. Overwrite?[Y/N] ");
					if(!confirm.equalsIgnoreCase("Y")) {
						console.out.println("operation cancelled");
						return;
					}
				}
				try {
					File file = module.createActionCache(name, modelName, getActions());
					BuilderConsoleActivator.sendRefresh(module, cache, 1000);
					
					name = file.getName();
					name = name.substring(0, name.length()-5);
					console.out.println("created action cache <a href=\"open action_cache " + name + "\">" + name + "</a>");

				} catch(IllegalArgumentException e) {
					console.err.println(e.getLocalizedMessage());
				}
			}
		}
	}
	
	private Action[] getActions() {
		if(hasParam("actions")) {
			String[] sa = param("actions").split(",");
			Action[] actions = new Action[sa.length];
			for(int i = 0; i < sa.length; i++) {
				actions[i] = Action.valueOf(sa[i]);
			}
			return actions;
		} else {
			return new Action[0];
		}
	}
	
}
