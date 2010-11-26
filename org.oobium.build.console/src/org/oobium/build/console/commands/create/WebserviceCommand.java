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

import org.oobium.build.workspace.Module;

public class WebserviceCommand extends ApplicationCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
	}

	@Override
	protected Module createModule(File file) {
		return getWorkspace().createWebservice(file, paramMap());
	}
	
}
