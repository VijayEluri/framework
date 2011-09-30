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
import org.oobium.build.console.commands.destroy.ControlerCacheCommand;
import org.oobium.build.console.commands.destroy.ApplicationCommand;
import org.oobium.build.console.commands.destroy.BundleCommand;
import org.oobium.build.console.commands.destroy.ControllerCommand;
import org.oobium.build.console.commands.destroy.ControllerForCommand;
import org.oobium.build.console.commands.destroy.MailerCommand;
import org.oobium.build.console.commands.destroy.MigratorCommand;
import org.oobium.build.console.commands.destroy.ModelCommand;
import org.oobium.build.console.commands.destroy.ModuleCommand;
import org.oobium.build.console.commands.destroy.NotifierCommand;
import org.oobium.build.console.commands.destroy.ObserverCommand;
import org.oobium.build.console.commands.destroy.ViewCommand;
import org.oobium.build.console.commands.destroy.ViewsForCommand;


public class DestroyCommand extends BuilderCommand {

	@Override
	public void configure() {
		add(new ControlerCacheCommand());
		add(new ApplicationCommand());
		add(new BundleCommand());
		add(new ControllerCommand());
		add(new ControllerForCommand());
		add(new MailerCommand());
		add(new MigratorCommand());
		add(new ModelCommand());
		add(new ModuleCommand());
		add(new NotifierCommand());
		add(new ObserverCommand());
		add(new ViewCommand());
		add(new ViewsForCommand());
	}
	
}
