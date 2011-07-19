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
package org.oobium.build.console.commands.deploy;

import java.io.File;

import org.oobium.build.clients.blazeds.BlazeProjectGenerator;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.utils.FileUtils;

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
			
			// Obviously, the file path needs to be changed :)
			File blazeZip = new File("c:\\Users\\jeremyd\\BlazeDS\\blazeds-turnkey-3.3.0.20931.zip");
			if(!blazeZip.isFile()) {
				console.err.println("blaze turnkey zip cannot be found");
				return;
			}

			File wd = new File(workspace.getWorkingDirectory(), "blaze-server");
			if(wd.exists()) {
				FileUtils.deleteContents(wd);
			}

			FileUtils.extract(blazeZip, wd);
			
			File tomcat = new File(wd, "tomcat");
			File installDir = new File(tomcat, "webapps");
			
			// bundle up the blaze project
			// module.createJar(jar, version);
			
			// put it in install directory
			
		} catch(Exception e) {
			console.err.print(e);
		}
	}

}
