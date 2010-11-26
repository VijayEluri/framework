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

import static org.oobium.utils.StringUtils.camelCase;

import java.io.File;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Module;

public class MailerCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}

	@Override
	public void run() {
		Module module = getModule();
		String name = camelCase(param(0));
		if(!name.endsWith("Mailer")) {
			name = name + "Mailer";
		}
		
		File mailer = module.getMailer(name);
		if(mailer.exists()) {
			String confirm = ask(name + " already exists. Overwrite?[Y/N] ");
			if(!confirm.equalsIgnoreCase("Y")) {
				console.out.println("operation cancelled");
				return;
			}
		}
		
		module.createMailer(name, getMethods());
		List<File> files = module.createMailerTemplates(name);
		files.add(module.createMailerLayout(name));
		if(!module.getMailerLayout().exists()) {
			files.add(module.createMailerLayout());
		}
		
		if(flag('v')) {
			console.out.print("created ").println(name, "open mailer " + name);
			for(File file : files) {
				String tname = file.getName();
				tname = tname.substring(0, tname.length()-4);
				console.out.print("created ").println(tname, "open file " + file.getAbsolutePath());
			}
		}
		
		BuilderConsoleActivator.sendRefresh(module, 100);
	}
	
	private String[] getMethods() {
		if(hasParam("methods")) {
			return param("methods").split(",");
		} else {
			return new String[0];
		}
	}
	
}
