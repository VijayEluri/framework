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
import org.oobium.build.events.BuildEvent;
import org.oobium.build.events.BuildEvent.Type;
import org.oobium.build.events.BuildListener;
import org.oobium.build.gen.TestGenerator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;

public class RouteTestsCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}
	
	@Override
	public void run() {
		Module module = getModule();
		TestSuite testSuite = getWorkspace().getTestSuiteFor(module);
		if(testSuite != null) {
			TestGenerator gen = new TestGenerator(testSuite);
			gen.addListener(new BuildListener() {
				@Override
				public void handleEvent(BuildEvent event) {
					if(event.type == Type.FileExists) {
						event.doIt = false;
					}
				}
			});
			File tests = gen.createRouteTests(module);
			Eclipse.openFile(testSuite.file, tests);
		} else {
			console.err.println("test suite does not exist");
		}
	}

}
