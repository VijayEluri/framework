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
package org.oobium.build.console.commands.set;

import java.io.File;

import org.oobium.build.console.BuilderCommand;

public class BundleReposCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		String repos = (paramCount() == 0) ? null : param(0);
		if(repos != null) {
			String[] sa = repos.split(";");
			File[] fa = new File[sa.length];
			for(int i = 0; i < sa.length; i++) {
				fa[i] = new File(sa[i]);
			}
			getWorkspace().setBundleRepositories(fa);
			console.out.println("bundle repositories set successfully");
		} else {
			console.err.println("failed to set project");
		}
	}

}
