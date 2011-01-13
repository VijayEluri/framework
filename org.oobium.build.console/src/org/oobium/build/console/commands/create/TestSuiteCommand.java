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

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;
import org.oobium.utils.FileUtils;

public class TestSuiteCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Module module = getModule();
		TestSuite tests = getWorkspace().getTestSuiteFor(module);
		if(tests != null) {
			String confirm = flag('f') ? "Y" : ask("This will overwrite the existing tests. Are you sure?[Y/N] ");
			if("Y".equalsIgnoreCase(confirm)) {
				tests.delete();
			} else {
				console.out.println("operation cancelled");
				return;
			}
		} else {
			if(module.testSuite.exists()) {
				String confirm = flag('f') ? "Y" : ask("This will overwrite the existing tests. Are you sure?[Y/N] ");
				if("Y".equalsIgnoreCase(confirm)) {
					FileUtils.delete(module.testSuite);
				} else {
					console.out.println("operation cancelled");
					return;
				}
			}
		}
		
		try {
			tests = getWorkspace().createTestSuite(module);
			if(tests != null) {
				console.out.println("successfully created tests " + tests.name);
				BuilderConsoleActivator.sendImport(tests);
			} else {
				console.err.println("failed to create tests for " + module.name);
			}
		} catch(Exception e) {
			console.err.println("failed to create tests: " + e.getMessage());
		}
	}

}
