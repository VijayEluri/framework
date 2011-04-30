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
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Workspace;

public class MigrationCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = 1;
	}
	
	@Override
	public void run() {
		Workspace ws = getWorkspace();
		Application app = getApplication();
		Migrator migrator = ws.getMigratorFor(app);
		File migration = hasParam(0) ? migrator.getMigration(param(0)) : migrator.getInitialMigration();
		if(migration.exists()) {
			Eclipse.openFile(app.migrator, migration);
		} else {
			console.err.println("migration file (" + migration.getName() + ") does not exist");
		}
	}

}
