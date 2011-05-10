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

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.gen.TestGenerator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;
import org.oobium.build.workspace.Workspace;

public class ControllerCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		Workspace ws = getWorkspace();
		Module module = getModule();
		File controller = module.getController(param(0));
		String name = module.getControllerName(controller);
		if(controller.exists()) {
			String confirm = ask(name + " already exists. Overwrite?[Y/N] ");
			if(!confirm.equalsIgnoreCase("Y")) {
				console.out.println("operation cancelled");
				return;
			}
		}

		long manifestMod = module.manifest.lastModified();
		
		module.createController(controller);
		console.out.println("created controller <a href=\"open controller " + name + "\">" + name + "</a>");

		TestSuite testSuite = ws.getTestSuiteFor(module);
		if(testSuite == null) {
			String confirm = flag('f') ? "y" : ask("Test suite project does not exist. Create?[Y/N] ");
			if(confirm.equalsIgnoreCase("Y")) {
				testSuite = ws.createTestSuite(module);
			}
		}
		if(testSuite != null) {
			TestGenerator gen = new TestGenerator(testSuite);
			File test = gen.createControllerTests(module, controller);
			String testName = test.getName();
			testName = testName.substring(0, testName.length() - 5);
			console.out.println("created test case <a href=\"open file " + test + "\">" + testName + "</a>");
			Eclipse.refreshProject(testSuite.name);
		}
		
		Eclipse.refresh(module.file, module.controllers);
		if(manifestMod != module.manifest.lastModified()) {
			Eclipse.refresh(module.file, module.manifest);
		}
	}

}
