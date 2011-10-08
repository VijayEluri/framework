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

public class StyleCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 2;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Module module = getModule();
		File style = module.getStyleSheet(param(0));
		String name = module.getStyleName(style);
		if(style.exists()) {
			String confirm = ask(param(0) + " already exists. Overwrite?[Y/N] ");
			if(!confirm.equalsIgnoreCase("Y")) {
				console.out.println("operation cancelled");
				return;
			}
		}
		
		module.createStyleSheet(param(0), (paramCount() > 1) ? param(1) : "// TODO auto-generated style sheet");
		console.out.println("created style sheet <a href=\"open style " + name + "\">" + name + "</a>");

		if(module.addScriptRoute(style)) {
			console.out.println("added style route to <a href=\"open activator\">" + module.activator.getName() + "</a>");
		}

		Eclipse.refreshProject(module.name);
	}

}
