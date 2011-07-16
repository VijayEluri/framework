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
import java.util.ArrayList;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.gen.TestGenerator;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;
import org.oobium.build.workspace.Workspace;

public class ModelCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		maxParams = 1;
		minParams = 1;
	}

	@Override
	public void run() {
		RunnerService.pauseUpdaters();
		
		List<File> waitFor = new ArrayList<File>();

		Module module = getModule();

		try {
			retainFlags('m', 'v', 'c', 'n', 't', 'r', 'f');
			if(!hasFlags() || (flagCount() == 1 && flag('f'))) {
				setFlag('m', 'c', 'n', 't', 'r');
				if(!module.isWebservice()) {
					setFlag('v');
				}
			}
			
			File model;
			int ix = param(0).indexOf('#');
			if(ix != -1) {
				String s = param(0);
				String mname = s.substring(0, ix);
				Module m = getWorkspace().getModule(mname);
				if(m != null) {
					model = m.getModel(s.substring(ix+1, s.length()));
				} else {
					console.err.println("module " + mname + " does not exist");
					return;
				}
			} else {
				model = module.getModel(param(0));
			}
			
			if(flag('m')) {
				if(model.exists()) {
					String confirm = flag('f') ? "y" : ask(param(0) + " already exists. Overwrite?[Y/N] ");
					if(confirm.equalsIgnoreCase("Y")) {
						model.delete();
					} else {
						console.out.println("operation cancelled");
						return;
					}
				}
				
				File file = module.createModel(param(0), paramMap());
				waitFor.add(file);
				
				String name = module.getModelName(model);
				console.out.println("created model <a href=\"open model " + name + "\">" + name + "</a>");
			}

			if(!model.exists()) {
				console.err.println("model does not exist");
				return;
			}
			
			int genflags = 0;
			if(flag('v')) {
				genflags |= Module.VIEW;
			}
			if(flag('c')) {
				genflags |= Module.CONTROLLER;
			}

			File[] files = module.createForModel(getWorkspace(), model, genflags);
			for(File file : files) {
				int type = module.getType(file);
				if(type != Module.VIEW) {
					waitFor.add(file);
				}
				switch(type) {
				case Module.VIEW:
					String view = module.getViewName(file);
					console.out.println("created view <a href=\"open view " + view + "\">" + view + "</a>");
					break;
				case Module.CONTROLLER:
					String controller = module.getControllerName(file);
					console.out.println("created controller <a href=\"open controller " + controller + "\">" + controller + "</a>");
					break;
				case Module.MANIFEST:
					console.out.println("modified <a href=\"open manifest\">MANIFEST.MF</a>");
					break;
				default:
					console.out.println("modified file <a href=\"open file " + file + "\">" + file.getName() + "</a>");
				}
			}
			
			if(flag('n')) {
				File notifier = module.getNotifier(model);
				if(notifier.exists()) {
					String name = notifier.getName();
					name = name.substring(0, name.length() - 5);
					String confirm = flag('f') ? "Y" : ask(name + " already exists. Overwrite?[Y/N] ");
					if(!confirm.equalsIgnoreCase("Y")) {
						console.out.println("operation cancelled");
						return;
					}
				}

				File file = module.createNotifier(model);
				String name = file.getName();
				name = name.substring(0, name.length()-5);
				console.out.println("created notifier <a href=\"open notifier " + name + "\">" + name + "</a>");
			}

			if(flag('t')) {
				Workspace ws = getWorkspace();
				TestSuite testSuite = ws.getTestSuiteFor(module);
				if(testSuite == null) {
					String confirm = flag('f') ? "y" : ask("Test suite project does not exist. Create?[Y/N] ");
					if(confirm.equalsIgnoreCase("Y")) {
						testSuite = ws.createTestSuite(module);
					}
				}
				if(testSuite != null) {
					List<File> tests = new ArrayList<File>();
					TestGenerator gen = new TestGenerator(testSuite);
					tests.add(gen.createModelTests(module, model));
					File controller = module.getControllerFor(model);
					if(controller.isFile()) {
						tests.add(gen.createControllerTests(module, controller));
					}
					for(File test : tests) {
						String name = test.getName();
						name = name.substring(0, name.length() - 5);
						console.out.println("created test case <a href=\"open file " + test + "\">" + name + "</a>");
					}
					Eclipse.refreshProject(testSuite.name);
				}
			}
			
			if(flag('r')) {
				if(module.addModelRoutes(param(0))) {
					waitFor.add(module.activator);
					console.out.println("added model routes to <a href=\"open activator\">" + module.activator.getName() + "</a>");
				}
			}
			
			waitFor.addAll(module.generate(getWorkspace()));

			Eclipse.refreshProject(module.name);
		} finally {
			if(!waitFor.isEmpty()) {
				RunnerService.waitFor(module, module.getBinFiles(waitFor));
			}
			RunnerService.unpauseUpdaters();
		}
	}
}
