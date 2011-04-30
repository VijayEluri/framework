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
		File mailer = module.getMailer(param(0));
		if(mailer == null) {
			console.err.println(param(0) + " does not exist in " + module);
			return;
		}

		String confirm = flag('f') ? "Y" : ask("This will permanently delete the Mailer. Are you sure?[Y/N] ");
		if(!"Y".equalsIgnoreCase(confirm)) {
			console.out.println("operation cancelled");
			return;
		}

		mailer.delete();
		
		if(flag('t')) {
			confirm = flag('f') ? "Y" : ask("This will permanently delete the Mailer's Templates folder. Are you sure?[Y/N] ");
			if(!"Y".equalsIgnoreCase(confirm)) {
				console.out.println("operation cancelled");
				return;
			}
			File templates = module.getMailerTemplate(param(0));
			FileUtils.delete(templates);
		}
		
		if(flag('l')) {
			confirm = flag('f') ? "Y" : ask("This will permanently delete the Mailer's Layout. Are you sure?[Y/N] ");
			if(!"Y".equalsIgnoreCase(confirm)) {
				console.out.println("operation cancelled");
				return;
			}
			File layout = module.getMailerLayout(param(0));
			layout.delete();
		}

		Eclipse.refresh(module.file, module.mailers);
	}
	
}
