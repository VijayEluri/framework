package org.oobium.build.console.commands.create;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;
import org.oobium.build.workspace.Workspace;

public class ControllerTestsCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
		minParams = 0;
		maxParams = 1;
	}

	@Override
	public void run() {
		Workspace ws = getWorkspace();
		Module module = getModule();
		TestSuite testSuite = ws.getTestSuiteFor(module);
		if(testSuite != null) {
			if(hasParam(0)) {
				testSuite.createControllerTests(module, param(0));
			} else {
//				testSuite.createControllerTests(module);
			}
		}
	}
	
}
