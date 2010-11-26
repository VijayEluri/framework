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
package org.oobium.build.console.commands.show;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.commands.show.workspace.ApplicationsCommand;
import org.oobium.build.console.commands.show.workspace.BundlesCommand;
import org.oobium.build.console.commands.show.workspace.ModulesCommand;

public class WorkspaceCommand extends BuilderCommand {

	@Override
	protected void configure() {
		set(new BundlesCommand());
		add(new ApplicationsCommand());
		add(new ModulesCommand());
	}

}
