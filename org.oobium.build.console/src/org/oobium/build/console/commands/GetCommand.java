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
package org.oobium.build.console.commands;

import org.oobium.build.console.BuilderCommand;

public class GetCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	public void run() {
		if("application".equals(param(0))) {
			if(hasApplication()) {
				console.out.println(getApplication());
			} else {
				console.err.println("application has not been set");
			}
		} else if("bundle".equals(param(0))) {
			if(hasBundle()) {
				console.out.println(getProjectName());
			} else {
				console.out.println("project has not been set");
			}
		} else {
			console.out.println(System.getProperty(param(0)));
		}
	}

}
