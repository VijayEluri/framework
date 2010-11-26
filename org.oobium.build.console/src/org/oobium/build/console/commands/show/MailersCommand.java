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
package org.oobium.build.console.commands.show;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Module;

public class MailersCommand extends BuilderCommand {

	@Override
	public void configure() {
		moduleRequired = true;
	}

	@Override
	public void run() {
		Module module = getModule();
		List<File> mailers = module.findMailers();
		if(!mailers.isEmpty()) {
			Collections.sort(mailers);
			int beginIndex = module.mailers.getAbsolutePath().length() + 1;
			for(File mailer : mailers) {
				String s = mailer.getAbsolutePath();
				s = s.substring(beginIndex, s.length()-5);
				console.out.println(s, "open mailer " + s);
			}
		} else {
			console.out.println("project has no mailers");
		}
	}

}
