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
import org.oobium.build.workspace.Bundle;

public class VersionCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
	}
	
	@Override
	public void run() {
		if(paramCount() == 1) {
			Bundle[] bundles = getWorkspace().getBundles(param(0));
			if(bundles.length == 0) {
				console.err.println("workspace does not contain bundle: " + param(0));
			} else {
				for(Bundle bundle : bundles) {
					console.out.println(bundle.version);
				}
			}
		} else {
			console.out.println(getBundle().version);
		}
	}

}
