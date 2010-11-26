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

import java.io.File;
import java.util.Date;

import org.oobium.build.console.BuilderCommand;

public class TouchCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 2;
		minParams = 1;
	}

	@Override
	protected void run() {
		String flags;
		if(paramCount() == 2) {
			if(param(0).charAt(0) != '-') {
				console.err.println("unknown flags: " + param(0) + ". Useage: rm [-flags] filename");
				return;
			}
			flags = param(0);
		} else {
			flags = "";
		}
		
		String name = param(paramCount()-1);
		File file = new File(name);
		if(!file.isAbsolute()) {
			file = new File(getPwd(), param(paramCount()-1));
		}
		
		if(file.exists()) {
			Date date = new Date();
			file.setLastModified(date.getTime());
			if(flags.indexOf('v') != -1) {
				console.out.println("touch: updated last modified of '" + name + "' to " + date);
			}
		} else {
			try {
				if(file.createNewFile()) {
					if(flags.indexOf('v') != -1) {
						console.out.println("touch: created '" + name + "'");
					}
					return;
				}
			} catch(Exception e) {
				console.err.print(e);
			}
			console.err.println("touch: failed to create '" + name + "'");
		}
	}
	
}
