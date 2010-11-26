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
		
		File mailer = module.getMailer(param(0));
		if(mailer.isFile()) {
			List<File> modified = module.generateMailer(mailer);
			if(!modified.isEmpty()) {
				for(File file : modified) {
					BuilderConsoleActivator.sendRefresh(module, file, 1000);
				}
			}
		} else {
			console.err.println("mailer does not exist: " + param(0));
		}
	}
	
}
