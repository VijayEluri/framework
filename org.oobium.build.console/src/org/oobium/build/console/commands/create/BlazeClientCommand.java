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

import org.oobium.build.clients.blazeds.FlexProjectGenerator;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;

public class BlazeClientCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Module module = getModule();
		
		FlexProjectGenerator gen = new FlexProjectGenerator(module);
		gen.setForce(true);
		File project = gen.create();
		
		console.out.println("created blaze client <a href=\"open file " + project + "\">" + project.getName() + "</a>");

		Eclipse.importProject(project);
	}

}
