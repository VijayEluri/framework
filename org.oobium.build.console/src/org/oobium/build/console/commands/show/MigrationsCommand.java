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

import java.util.Arrays;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Bundle;

public class MigrationsCommand extends BuilderCommand {

	@Override
	public void run() {
		Bundle[] bundles = getWorkspace().getBundles();
		Arrays.sort(bundles);
		for(Bundle bundle : bundles) {
			if(bundle.isMigrator()) {
				console.out.println(bundle.name, "set project " + bundle.name);
			}
		}
	}

}
