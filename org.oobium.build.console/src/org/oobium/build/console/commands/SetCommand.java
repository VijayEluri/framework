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
import org.oobium.build.console.commands.set.ApplicationCommand;
import org.oobium.build.console.commands.set.BundleCommand;
import org.oobium.build.console.commands.set.PromptCommand;

public class SetCommand extends BuilderCommand {

	@Override
	public void configure() {
		add(new ApplicationCommand());
		add(new BundleCommand());
		add(new PromptCommand());
	}
	
}
