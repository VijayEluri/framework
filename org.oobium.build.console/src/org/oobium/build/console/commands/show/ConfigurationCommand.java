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
import org.oobium.build.workspace.Module;
import org.oobium.utils.FileUtils;

public class ConfigurationCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Module module = getModule();
		if(module.isJar) {
			console.out.println(FileUtils.readJarEntry(module.file, module.name.replaceAll("\\.", "/") + "configuration.js"));
		} else {
			console.out.println(FileUtils.readFile(module.config));
		}
	}
}
