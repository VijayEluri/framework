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
import org.oobium.build.console.commands.refresh.BundlesCommand;
import org.oobium.build.console.commands.refresh.ModelCommand;
import org.oobium.build.console.commands.refresh.ProjectCommand;
import org.oobium.build.console.commands.refresh.ViewCommand;
import org.oobium.build.console.commands.refresh.ViewsCommand;


public class RefreshCommand extends BuilderCommand {

	@Override
	public void configure() {
		set(new ProjectCommand());
		add(new BundlesCommand());
		add(new ModelCommand());
		add(new ViewCommand());
		add(new ViewsCommand());
	}
	
}
