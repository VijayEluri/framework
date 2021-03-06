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
import org.oobium.build.console.commands.open.ControllerCacheCommand;
import org.oobium.build.console.commands.open.ActivatorCommand;
import org.oobium.build.console.commands.open.ConfigurationCommand;
import org.oobium.build.console.commands.open.ControllerCommand;
import org.oobium.build.console.commands.open.ControllerForCommand;
import org.oobium.build.console.commands.open.FileCommand;
import org.oobium.build.console.commands.open.LayoutCommand;
import org.oobium.build.console.commands.open.LayoutForCommand;
import org.oobium.build.console.commands.open.MailerCommand;
import org.oobium.build.console.commands.open.ManifestCommand;
import org.oobium.build.console.commands.open.MigratorCommand;
import org.oobium.build.console.commands.open.ModelCommand;
import org.oobium.build.console.commands.open.CreateSchemaCommand;
import org.oobium.build.console.commands.open.NotifierCommand;
import org.oobium.build.console.commands.open.ObserverCommand;
import org.oobium.build.console.commands.open.RouteTestsCommand;
import org.oobium.build.console.commands.open.MigrationCommand;
import org.oobium.build.console.commands.open.ScriptCommand;
import org.oobium.build.console.commands.open.StyleCommand;
import org.oobium.build.console.commands.open.TypeCommand;
import org.oobium.build.console.commands.open.ViewCommand;

public class OpenCommand extends BuilderCommand {

	@Override
	public void configure() {
		set(new FileCommand());
		add(new ControllerCacheCommand());
		add(new ActivatorCommand());
		add(new ConfigurationCommand());
		add(new ControllerCommand());
		add(new ControllerForCommand());
		add(new CreateSchemaCommand());
		add(new LayoutCommand());
		add(new LayoutForCommand());
		add(new MailerCommand());
		add(new ManifestCommand());
		add(new MigratorCommand());
		add(new ModelCommand());
		add(new NotifierCommand());
		add(new ObserverCommand());
		add(new RouteTestsCommand());
		add(new MigrationCommand());
		add(new ScriptCommand());
		add(new StyleCommand());
		add(new TypeCommand());
		add(new ViewCommand());
	}
	
}
