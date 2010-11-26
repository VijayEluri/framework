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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Module;

public class ModelsCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Module module = getModule();
		List<File> models = module.findModels();
		if(!models.isEmpty()) {
			Collections.sort(models);
			int beginIndex = module.models.getAbsolutePath().length() + 1;
			for(File model : models) {
				String s = model.getAbsolutePath();
				s = s.substring(beginIndex, s.length()-5);
				console.out.println(s, "open model " + s);
			}
		} else {
			console.out.println("project has no models");
		}
	}

}
