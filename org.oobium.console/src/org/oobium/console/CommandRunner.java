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
package org.oobium.console;

class CommandRunner extends Thread {

	private Command command;
	private String options;
	
	CommandRunner(Command rootCommand, String command) {
		this.command = rootCommand;
		String[] sa = command.split(" +");
		for(int i = 0; i < sa.length; i++) {
			if(options != null) {
				options += (" " + sa[i]);
			} else {
				Command c = this.command.get(sa[i]);
				if(c != null) {
					this.command = c;
				} else {
					this.options = sa[i];
				}
			}
		}
		if(this.command == rootCommand) {
			this.command = null;
			rootCommand.console.err.println("Unknown command: \"" + sa[0] + "\".  Use \"<a href=\"help\">help</a>\" to see a list of commands.");
		}
	}
	
	@Override
	public void run() {
		if(command != null) {
			try {
				command.execute(options);
			} catch(Exception e) {
				command.console.err.print(e);
			}
		}
	}

}
