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
		
		try {
			BlazeProjectGenerator blaze = new BlazeProjectGenerator(workspace, module);
			blaze.setExportFlex(true);
			blaze.setForce(true);
			blaze.create();
			
			File blazeProject = blaze.getProject();
			console.out.println("created blaze client project <a href=\"open file " + blazeProject + "\">" + blazeProject.getName() + "</a>");

			File flexProject = blaze.getFlexProject();
			console.out.println("created blaze flex client project <a href=\"open file " + flexProject + "\">" + flexProject.getName() + "</a>");
			
			Eclipse.importProjects(blazeProject, flexProject);
		} catch(Exception e) {
			console.err.print(e);
		}
	}

}
