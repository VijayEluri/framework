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
package org.oobium.build.console.commands.destroy;

import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Migrator;


public class MigratorCommand extends BundleCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Migrator migration = getWorkspace().getMigratorFor(getModule());
		if(migration == null) {
			console.err.println("The active Module does not have a Migrator");
			return;
		}

		remove(migration);
		
		Eclipse.refreshProject(migration.name);
	}

}
