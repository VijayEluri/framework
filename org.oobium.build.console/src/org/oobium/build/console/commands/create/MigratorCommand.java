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
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Module;
import org.oobium.utils.FileUtils;

public class MigratorCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		boolean isNew = true;
		Module module = getModule();
		Migrator migrator = getWorkspace().getMigratorFor(module);
		if(migrator != null) {
			String confirm = flag('f') ? "Y" : ask("This will overwrite the existing migrator. Are you sure?[Y/N] ");
			if("Y".equalsIgnoreCase(confirm)) {
				isNew = false;
				migrator.delete();
			} else {
				console.out.println("operation cancelled");
				return;
			}
		} else {
			if(module.migrator.exists()) {
				String confirm = flag('f') ? "Y" : ask("This will overwrite the existing migrator. Are you sure?[Y/N] ");
				if("Y".equalsIgnoreCase(confirm)) {
					FileUtils.delete(module.migrator);
				} else {
					console.out.println("operation cancelled");
					return;
				}
			}
		}
		
		try {
			migrator = getWorkspace().createMigrator(module);
			if(migrator != null) {
				console.out.println("successfully created migrator: " + migrator.name);
				if(isNew) {
					BuilderConsoleActivator.sendImport(migrator);
				} else {
					BuilderConsoleActivator.sendRefresh(migrator, 100);
				}
			} else {
				console.err.println("failed to create migrator for " + module.name);
			}
		} catch(Exception e) {
			console.err.println("failed to create migrator: " + e.getMessage());
		}
	}

}
