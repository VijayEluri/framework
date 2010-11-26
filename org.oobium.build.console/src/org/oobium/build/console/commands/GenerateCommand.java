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
package org.oobium.build.console.commands;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.commands.generate.ApplicationCommand;
import org.oobium.build.console.commands.generate.AssetListCommand;
import org.oobium.build.console.commands.generate.MailerCommand;
import org.oobium.build.console.commands.generate.MailerTemplateCommand;
import org.oobium.build.console.commands.generate.ModelCommand;
import org.oobium.build.console.commands.generate.ModuleCommand;
import org.oobium.build.console.commands.generate.ScriptCommand;
import org.oobium.build.console.commands.generate.StyleCommand;
import org.oobium.build.console.commands.generate.ViewCommand;


public class GenerateCommand extends BuilderCommand {

	@Override
	public void configure() {
		set(new ModuleCommand());
		add(new AssetListCommand());
		add(new ApplicationCommand());
		add(new MailerCommand());
		add(new MailerTemplateCommand());
		add(new ModelCommand());
		add(new ScriptCommand());
		add(new StyleCommand());
		add(new ViewCommand());
	}
	
}
