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
import org.oobium.build.console.commands.create.ActionCacheCommand;
import org.oobium.build.console.commands.create.ApplicationCommand;
import org.oobium.build.console.commands.create.ControllerCommand;
import org.oobium.build.console.commands.create.ControllerForCommand;
import org.oobium.build.console.commands.create.ControllerTestsCommand;
import org.oobium.build.console.commands.create.MailerCommand;
import org.oobium.build.console.commands.create.MailerTemplateCommand;
import org.oobium.build.console.commands.create.MailerTemplatesForCommand;
import org.oobium.build.console.commands.create.MigratorCommand;
import org.oobium.build.console.commands.create.ModelCommand;
import org.oobium.build.console.commands.create.ModuleCommand;
import org.oobium.build.console.commands.create.ObserverCommand;
import org.oobium.build.console.commands.create.SchemaCommand;
import org.oobium.build.console.commands.create.TestSuiteCommand;
import org.oobium.build.console.commands.create.TestsCommand;
import org.oobium.build.console.commands.create.ViewCommand;
import org.oobium.build.console.commands.create.ViewsForCommand;
import org.oobium.build.console.commands.create.WebserviceCommand;


public class CreateCommand extends BuilderCommand {

	@Override
	public void configure() {
		add(new ActionCacheCommand());
		add(new ApplicationCommand());
		add(new ControllerCommand());
		add(new ControllerForCommand());
		add(new ControllerTestsCommand());
		add(new MailerCommand());
		add(new MailerTemplateCommand());
		add(new MailerTemplatesForCommand());
		add(new MigratorCommand());
		add(new ModelCommand());
		add(new ModuleCommand());
		add(new ObserverCommand());
		add(new SchemaCommand());
		add(new TestsCommand());
		add(new TestSuiteCommand());
		add(new ViewCommand());
		add(new ViewsForCommand());
		add(new WebserviceCommand());
	}

}
