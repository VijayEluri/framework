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
package org.oobium.build.console.commands.add;

import java.io.File;

import org.oobium.build.console.BuilderCommand;

public class RepositoryCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		File file = new File(param(0));
		if(!file.isAbsolute()) {
			file = new File(getPwd(), param(0));
		}
		if(file.isDirectory()) {
			getWorkspace().addRepository(file);
		} else {
			console.err.println(file + " is not a directory");
		}
	}

}
