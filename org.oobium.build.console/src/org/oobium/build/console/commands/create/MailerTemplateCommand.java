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

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;

public class MailerTemplateCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		requiredParams = new String[] { "mailer", "name" };
	}

	@Override
	public void run() {
		Module module = getModule();
		String mailerName = camelCase(param("mailer"));
		if(!mailerName.endsWith("Mailer")) {
			mailerName = mailerName + "Mailer";
		}
		
		File mailer = module.getMailer(mailerName);
		if(!mailer.exists()) {
			console.err.println("mailer " + mailerName + " does not exist");
			return;
		}
		
		String templateName = param("name");
		File template = module.createMailerTemplate(mailerName, templateName);
		
		Eclipse.refresh(module.file, mailer);
		Eclipse.refresh(module.file, template);
	}
	
}
