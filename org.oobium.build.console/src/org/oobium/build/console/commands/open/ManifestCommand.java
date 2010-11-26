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

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Bundle;

public class ManifestCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}
	
	@Override
	public void run() {
		Bundle bundle = getBundle();
		if(bundle.manifest.exists()) {
//			console.out.println("opening " + bundle.manifest);
			BuilderConsoleActivator.sendOpen(bundle.file, bundle.manifest);
		} else {
			console.err.println("model \"" + param(0) + "\" does not exist");
		}
	}

}
