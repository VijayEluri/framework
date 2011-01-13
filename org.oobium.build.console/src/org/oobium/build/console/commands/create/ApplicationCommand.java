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

import static org.oobium.build.util.ProjectUtils.isProject;
import static org.oobium.utils.FileUtils.delete;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;

public class ApplicationCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
	}

	protected Module createModule(File file) {
		return getWorkspace().createApplication(file, paramMap());
	}

	@Override
	public void run() {
		File d = new File(getPwd());
		File[] files = d.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && ".project".equals(file.getName());
			}
		});
		
		if(files != null && files.length > 0) {
			console.err.println("cannot create a project inside an existing project");
			return;
		}
		
		File project = new File(param(0));
		if(!project.isAbsolute()) {
			project = new File(getPwd(), param(0));
		}
		if(project.exists()) {
			String confirm;
			if(flag('f')) {
				confirm = "y";
			} else if(isProject(project)) {
				confirm = ask("  a project already exists with the name " + param(0) + " - overwrite? [Y/N] ");
			} else {
				confirm = ask("  a directory already exists with the name " + param(0) + " - overwrite? [Y/N] ");
			}
			if(confirm.equalsIgnoreCase("y")) {
				delete(project);
			} else {
				console.out.println("operation cancelled");
				return;
			}
		}
		
		List<File> importList = new ArrayList<File>();
		
		Module module = createModule(project);
		if(module != null) {
			console.out.print("successfully created \"").print(module).println("\"");
			importList.add(project);
			
			String confirm = "Y"; //flag('f') ? (flag('m') ? "y" : "n") : (flag('m') ? "y" : ask("Also create the migration project? [Y/N] "));
			if(confirm.equalsIgnoreCase("y")) {
				boolean create = true;
				project = module.migrator;
				if(project.exists()) {
					if(flag('f')) {
						confirm = "y";
					} else if(isProject(project)) {
						confirm = ask("  a bundle already exists with the name " + project.getName() + " - overwrite? [Y/N] ");
					} else {
						confirm = ask("  a directory already exists with the name " + project.getName() + " - overwrite? [Y/N] ");
					}
					if(confirm.equalsIgnoreCase("y")) {
						delete(project);
					} else {
						create = false;
						console.out.println("operation cancelled");
					}
				}
				if(create) {
					Migrator migration = getWorkspace().createMigrator(module);
					console.out.print("successfully created \"").print(migration).println("\"");
					importList.add(migration.file);
				}
			}

			confirm = "Y"; //flag('f') ? (flag('m') ? "y" : "n") : (flag('m') ? "y" : ask("Also create the test suite project? [Y/N] "));
			if(confirm.equalsIgnoreCase("y")) {
				boolean create = true;
				project = module.testSuite;
				if(project.exists()) {
					if(flag('f')) {
						confirm = "y";
					} else if(isProject(project)) {
						confirm = ask("  a bundle already exists with the name " + project.getName() + " - overwrite? [Y/N] ");
					} else {
						confirm = ask("  a directory already exists with the name " + project.getName() + " - overwrite? [Y/N] ");
					}
					if(confirm.equalsIgnoreCase("y")) {
						delete(project);
					} else {
						create = false;
						console.out.println("operation cancelled");
					}
				}
				if(create) {
					TestSuite testSuite = getWorkspace().createTestSuite(module);
					console.out.print("successfully created \"").print(testSuite).println("\"");
					importList.add(testSuite.file);
				}
			}

			for(File file : importList) {
				BuilderConsoleActivator.sendImport(file);
			}
		} else {
			console.err.println("failed creating " + project.getName());
		}
	}
	
}
