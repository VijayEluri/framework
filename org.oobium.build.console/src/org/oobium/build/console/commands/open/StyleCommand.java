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
import org.oobium.build.workspace.Module;

public class StyleCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Module module = getModule();
		File style = module.getStyleSheet(param(0));
		if(style != null && style.isFile()) {
			Eclipse.openFile(module.file, style);
		} else {
			console.err.println("style sheet does not exist");
		}
	}

}
