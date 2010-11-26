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
import org.oobium.build.console.commands.show.BundlesCommand;
import org.oobium.build.console.commands.show.ConfigurationCommand;
import org.oobium.build.console.commands.show.ControllersCommand;
import org.oobium.build.console.commands.show.MailersCommand;
import org.oobium.build.console.commands.show.ManifestCommand;
import org.oobium.build.console.commands.show.MigrationsCommand;
import org.oobium.build.console.commands.show.ModelsCommand;
import org.oobium.build.console.commands.show.ModulesCommand;
import org.oobium.build.console.commands.show.NaturesCommand;
import org.oobium.build.console.commands.show.TestSuitesCommand;
import org.oobium.build.console.commands.show.ViewsCommand;
import org.oobium.build.console.commands.show.WorkspaceCommand;

public class ShowCommand extends BuilderCommand {

	@Override
	public void configure() {
		add(new BundlesCommand());
		add(new ConfigurationCommand());
		add(new ControllersCommand());
		add(new MailersCommand());
		add(new ManifestCommand());
		add(new MigrationsCommand());
		add(new ModelsCommand());
		add(new ModulesCommand());
		add(new NaturesCommand());
		add(new TestSuitesCommand());
		add(new ViewsCommand());
		add(new WorkspaceCommand());
	}
	
}
