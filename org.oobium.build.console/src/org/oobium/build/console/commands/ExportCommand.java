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
package org.oobium.build.console.commands;

import static org.oobium.utils.Config.Mode.DEV;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Application;

public class ExportCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = 1;
	}
	
	@Override
	public void run() {
		Application app = getApplication();
		
		if(flag('v')) {
			console.capture();
		}

		try {
			long start = System.currentTimeMillis();
			if(flag('c')) {
				app.cleanExport(getWorkspace());
			}
			app.export(getWorkspace(), DEV);
			if(flag('v')) {
				console.out.println("exported " + app.name() + " in " + (System.currentTimeMillis() - start) + "ms");
			}
		} catch(Exception e) {
			console.err.print(e);
		} finally {
			if(flag('v')) {
				console.release();
			}
		}
	}
	
}
