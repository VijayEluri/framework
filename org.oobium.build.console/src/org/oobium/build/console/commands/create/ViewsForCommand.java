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

import static org.oobium.utils.StringUtils.blank;

import java.io.File;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.console.Suggestion;

public class ViewsForCommand extends BuilderCommand {

	private static final String ALL_MODELS = "all_models";

	
	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 2;
		minParams = 1;
	}

	private void createViewsFor(Module module, File model, String name) {
		File folder = module.getViewsFolder(name);
		if(folder.exists()) {
			String confirm = flag('f') ? "Y" : ask("Views folder for " + name + " already exists. Overwrite standard Views?[Y/N] ");
			if(!confirm.equalsIgnoreCase("Y")) {
				console.out.println("skipped views folder for " + name);
				return;
			}
		}

		File[] files = module.createForModel(getWorkspace(), model, Module.VIEW);
		for(File file : files) {
			if(module.getType(file) == Module.VIEW) {
				module.generateView(file);
				String vname = module.getViewName(file);
				console.out.println("created view <a href=\"open view " + vname + "\">" + vname + "</a>");
			}
		}
		Eclipse.refreshProject(module.name);
	}
	
	@Override
	public void run() {
		if(ALL_MODELS.equals(param(0))) {
			Module module = getModule();
			for(File model : module.findModels()) {
				String name = module.getModelName(model);
				createViewsFor(module, model, name);
			}
		}
		else {
			File model;
			String name;
			
			String[] sa = param(0).split("#");
			if(sa.length == 1) {
				Module module = getModule();
				model = module.getModel(sa[0]);
				name = module.getModelName(model);
			} else {
				Module module = getWorkspace().getModule(sa[0]);
				if(module == null) {
					console.err.println("module " + sa[0] + " does not exist");
					return;
				}
				if(module.isJar) {
					console.err.println("jarred modules not yet supported");
					return;
				}
				model = module.getModel(sa[1]);
				name = module.getModelName(model);
			}
			
			Module module = getModule();
			
			if(!model.isFile()) {
				console.err.println("model " + name + " does not exist");
				return;
			}
			
			createViewsFor(module, model, name);
		}
	}
	
	@Override
	protected Suggestion[] suggest(String cmd, Suggestion[] suggestions) {
		if(blank(cmd)) {
			return new Suggestion[] { new Suggestion(ALL_MODELS, "create views for all models in the active project"), suggestions[0] };
		}
		if(ALL_MODELS.startsWith(cmd.trim())) {
			return new Suggestion[] { new Suggestion(ALL_MODELS, "create views for all models in the active project") };
		}
		return super.suggest(cmd, suggestions);
	}
	
}
