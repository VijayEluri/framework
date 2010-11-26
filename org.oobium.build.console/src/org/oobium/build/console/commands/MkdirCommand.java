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

import org.oobium.build.console.BuilderCommand;

public class MkdirCommand extends BuilderCommand {

	@Override
	public void configure() {
		maxParams = 2;
		minParams = 1;
	}

	@Override
	protected void run() {
		String flags = null;
		if(paramCount() == 2) {
			if(param(0).charAt(0) != '-') {
				console.err.println("unknown flags: " + param(0) + ". Useage: mkdir [-flags] filename");
				return;
			}
			flags = param(0).substring(1);
		} else {
			flags = "";
		}
		
		String name = param(paramCount()-1);
		File file = new File(name);
		if(!file.isAbsolute()) {
			file = new File(getPwd(), param(paramCount()-1));
		}
		
		if(file.exists()) {
			console.err.println("mkdir: cannot create directory '" + name + "': File exists");
		} else {
			if(flags.indexOf('p') == -1) {
				if(file.mkdir()) {
					if(flags.indexOf('v') != -1) {
						console.err.println("mkdir: created directory '" + name + "'");
					}
				} else {
					console.err.println("mkdir: cannot create directory '" + name + "': Parent directory does not exist");
				}
			} else {
				if(file.mkdirs()) {
					if(flags.indexOf('v') != -1) {
						console.err.println("mkdir: created directory '" + name + "'");
					}
				} else {
					console.err.println("mkdir: cannot create directory '" + name + "'");
				}
			}
		}
	}
	
}
