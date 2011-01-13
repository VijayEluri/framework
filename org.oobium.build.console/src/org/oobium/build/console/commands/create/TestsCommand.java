package org.oobium.build.console.commands.create;

import java.io.File;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.events.BuildEvent;
import org.oobium.build.events.BuildListener;
import org.oobium.build.events.BuildEvent.Type;
import org.oobium.build.gen.TestGenerator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;
import org.oobium.build.workspace.Workspace;

public class TestsCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Workspace ws = getWorkspace();
		Module module = getModule();
		TestSuite testSuite = ws.getTestSuiteFor(module);
		if(testSuite == null) {
			String confirm = flag('f') ? "Y" : ask("There is no test suite for " + module + ". Create? [Y/N] ");
			if(!confirm.equalsIgnoreCase("Y")) {
				console.out.println("operation cancelled");
				return;
			}
		}

		TestGenerator gen = new TestGenerator(testSuite);
		if(!flag('f')) {
			gen.addListener(new BuildListener() {
				@Override
				public void handleEvent(BuildEvent event) {
					if(event.type == Type.FileExists) {
						String name = ((File) event.data).getName();
						name = name.substring(0, name.length() - 5);
						String confirm = ask(name + " already exists. Overwrite? [Y/N] ");
						if(!confirm.equalsIgnoreCase("Y")) {
							console.out.println("skipped " + name);
							event.doIt = false;
						}
					}
				}
			});
		}
		List<File> files = gen.createTests(module);
		
		for(File file : files) {
			String name = file.getName();
			name = name.substring(0, name.length() - 5);
			console.out.println("created test case <a href=\"open file " + file + "\">" + name + "</a>");
		}

		BuilderConsoleActivator.sendRefresh(testSuite, 100);
	}
	
}
