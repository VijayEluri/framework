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
import org.oobium.build.workspace.Bundle;

public class CreateSchemaCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}

	@Override
	public void run() {
		File file = null;
		Application application = getApplication();
		Bundle migration = getWorkspace().getMigratorFor(application);
		if(migration != null) {
			if(migration.isJar) {
				// TODO ???
				file = migration.file;
			} else {
				file = new File(migration.file, "sql/create.sql");
			}
		}

		if(file != null && file.exists()) {
			Eclipse.openFile(migration.file, file);
		} else {
			console.err.println("schema file does not exist");
		}
	}

}
