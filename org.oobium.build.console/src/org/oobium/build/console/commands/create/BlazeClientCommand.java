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

import org.oobium.build.clients.blazeds.BlazeProjectGenerator;
import org.oobium.build.clients.blazeds.FlexProjectGenerator;
import org.oobium.build.clients.blazeds.FlexTestProjectGenerator;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;

public class BlazeClientCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Workspace workspace = getWorkspace();
		Module module = getModule();
		
		if(!module.hasModels()) {
			console.err.println(module + " has no models... exiting.");
			return;
		}
		
		retainFlags('b', 'f', 't');
		if(!hasFlags()) {
			setFlag('b', 'f', 't');
		}
		
		try {
			if(flag('b')) {
				BlazeProjectGenerator blaze = new BlazeProjectGenerator(workspace, module);
				blaze.setForce(true);
				blaze.setServer(param("server"));
				blaze.create();
				
				File blazeProject = blaze.getProject();
				console.out.println("created blaze client project <a href=\"open file \"" + blazeProject + "\"\">" + blazeProject.getName() + "</a>");
				Eclipse.importProjects(blazeProject);
			}

			if(flag('f')) {
				FlexProjectGenerator flex = new FlexProjectGenerator(module);
				flex.setForce(true);
				flex.setFlexSdk(param("flexsdk"));
				flex.create();
				
				File flexProject = flex.getProject();
				console.out.println("created blaze flex client project <a href=\"open file \"" + flexProject + "\"\">" + flexProject.getName() + "</a>");
				Eclipse.importProjects(flexProject);
			}
			
			if(flag('t')) {
				FlexTestProjectGenerator test = new FlexTestProjectGenerator(workspace, module);
				test.setForce(true);
				test.setFlexSdk(param("flexsdk"));
				test.create();
	
				File testProject = test.getProject();
				console.out.println("created flex test project <a href=\"open file \"" + testProject + "\"\">" + testProject.getName() + "</a>");
				Eclipse.importProjects(testProject);
			}
		} catch(Exception e) {
			console.err.print(e);
		}
	}

}
