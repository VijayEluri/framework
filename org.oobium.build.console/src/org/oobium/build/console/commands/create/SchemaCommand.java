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

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Application;
import org.oobium.utils.Config.Mode;

public class SchemaCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}
	
	@Override
	public void run() {
		Application app = getApplication();
		app.createSchema(getWorkspace(), Mode.DEV);
		BuilderConsoleActivator.sendRefresh(app.getMigratorFile(), 100);
	}

}
