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

import static org.oobium.utils.FileUtils.writeFile;

import java.io.File;

import org.oobium.build.clients.JQueryClientGenerator;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Application;

public class ClientCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}

	@Override
	public void run() {
		Application app = getApplication();
	
		JQueryClientGenerator generator = new JQueryClientGenerator(app);
		String source = generator.generate();

		File scripts = new File(app.assets, "scripts");
		File file = writeFile(scripts, "models.js", source);
		
		StringBuilder sb = new StringBuilder(file.getAbsolutePath());
		sb.setCharAt(app.file.getAbsolutePath().length(), '#');
		console.out.println("created client <a href=\"open file \"" + sb.toString() + "\"\">models.js</a>");

		Eclipse.refresh(app.file, scripts);
	}

}
