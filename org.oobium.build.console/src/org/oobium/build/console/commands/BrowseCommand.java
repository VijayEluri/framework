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

import org.eclipse.swt.program.Program;
import org.oobium.build.console.BuilderCommand;

public class BrowseCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
		runInDisplayThread = true;
	}
	
	@Override
	public void run() {
		String url = param(0).startsWith("http://") ? param(0) : ("http://" + param(0));
		Program.launch(url);
	}

}
