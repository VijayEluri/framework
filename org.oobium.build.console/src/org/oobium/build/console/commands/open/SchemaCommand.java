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
package org.oobium.build.console.commands.open;

import java.io.File;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Application;

public class SchemaCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}
	
	@Override
	public void run() {
		Application app = getApplication();
		File schema = app.getSchema();
		if(schema.exists()) {
			BuilderConsoleActivator.sendOpen(app.getMigratorFile(), schema);
		} else {
			console.err.println("schema file (create.sql) does not exist");
		}
	}

}
