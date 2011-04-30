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

import java.io.File;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Module;

public class MigrationCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}

	protected String getMigrationName() {
		return param(0);
	}
	
	@Override
	public void run() {
		Module module = getModule();
		Migrator migrator = getWorkspace().getMigratorFor(module);
		if(migrator == null) {
			String s = flag('f') ? "Y" : ask("migrator project does not exist. Create? [Y/N] ");
			if("Y".equalsIgnoreCase(s)) {
				try {
					migrator = getWorkspace().createMigrator(module);
					if(migrator != null) {
						console.out.println("successfully created migrator: " + migrator.name);
						Eclipse.importProject(migrator.name, migrator.file);
					} else {
						console.err.println("failed to create migrator for " + module.name);
						return;
					}
				} catch(Exception e) {
					console.err.println("failed to create migrator: " + e.getMessage());
					return;
				}
			} else {
				console.err.println("cannot create a migration without a migrator project; exiting.");
				return;
			}
		}

		String name = getMigrationName();
		File migration = migrator.getMigration(name);
		if(migration.exists()) {
			String s = flag('f') ? "Y" : ask("migration \"" + name + "\" already exists. Overwrite? [Y/N] ");
			if(!"Y".equalsIgnoreCase(s)) {
				return;
			}
			migration.delete();
		}
		
		migration = migrator.createMigration(name);
		console.out.println("created migration <a href=\"open migration " + name + "\">" + name + "</a>");
		
		if(migrator.addMigration(name)) {
			console.out.println("updated <a href=\"open migrator\">migrator</a>");
		}
		
		Eclipse.refreshProject(migrator.name);
	}

}
