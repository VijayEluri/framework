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

public class ControllerForCommand extends BuilderCommand {

	private Module modelModule;
	
	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}
	
	private File findModel(Workspace ws, String name) {
		modelModule = getModule();
		File localModel = modelModule.getModel(name);
		if(!localModel.isFile()) {
			for(String s : modelModule.getModules()) {
				modelModule = ws.getModule(s);
				if(modelModule != null) {
					File remoteModel = modelModule.getModel(name);
					if(remoteModel.isFile()) {
						String mName = modelModule.getModelName(remoteModel);
						String confirm = flag('f') ? "Y" : ask("use the model \"" + mName + "\" found in " + modelModule.name + "? [Y/N] ");
						if("Y".equalsIgnoreCase(confirm)) {
							return remoteModel;
						}
					}
				}
			}
		}
		return localModel;
	}
	
	@Override
	public void run() {
		File model;
		String name;
		
		Workspace ws = getWorkspace();

		String[] sa = param(0).split("#");
		if(sa.length == 1) {
			model = findModel(ws, sa[0]);
			name = modelModule.getModelName(model);
		} else {
			modelModule = ws.getModule(sa[0]);
			if(modelModule == null) {
				console.err.println("module " + sa[0] + " does not exist");
				return;
			}
			if(modelModule.isJar) {
				console.err.println("jarred modules not yet supported");
				return;
			}
			model = modelModule.getModel(sa[1]);
			name = modelModule.getModelName(model);
		}
		
		Module module = getModule();
		
		if(!model.isFile()) {
			console.err.println("model " + name + " does not exist in the active module");
			return;
		}
		
		File controller = module.getControllerFor(name);
		if(controller.exists()) {
			String confirm = flag('f') ? "Y" : ask("Controller for " + name + " already exists. Overwrite?[Y/N] ");
			if(!confirm.equalsIgnoreCase("Y")) {
				console.out.println("operation cancelled");
				return;
			}
		}

		File[] files = module.createForModel(ws, model, Module.CONTROLLER);
		for(File file : files) {
			if(module.getType(file) == Module.CONTROLLER) {
				String cname = module.getControllerName(file);
				console.out.println("created controller <a href=\"open controller " + cname + "\">" + cname + "</a>");
			}
		}

		if(modelModule != module) {
			String packageName = modelModule.packageName(modelModule.models);
			if(module.addImportPackage(packageName)) {
				console.out.println("added import \"" + packageName + "\" to <a href=\"open manifest\">Manifest</a>");
			}
		}
		
		Eclipse.refreshProject(module.name);
		
		modelModule = null;
		
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
			console.out.println("created test case <a href=\"open file \"" + test + "\"\">" + testName + "</a>");
			Eclipse.refreshProject(testSuite.name);
		}
	}

}
