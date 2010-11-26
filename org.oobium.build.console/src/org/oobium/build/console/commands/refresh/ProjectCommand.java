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
package org.oobium.build.console.commands.refresh;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;


public class ProjectCommand extends BuilderCommand {

	@Override
	public void configure() {
		bundleRequired = true;
		maxParams = 0;
		minParams = 0;
	}
	
	@Override
	public void run() {
		BuilderConsoleActivator.sendRefresh(getBundle().file, 0);
	}

}
