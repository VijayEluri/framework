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

import java.util.ArrayList;
import java.util.List;

class CommandRunner extends Thread {

	private Command command;
	private String options;
	
	CommandRunner(Command rootCommand, String command) {
		this.command = rootCommand;
		List<String> cmds = split(command.trim());
		for(String cmd : cmds) {
			if(options != null) {
				options += (" " + cmd);
			} else {
				Command c = this.command.get(cmd);
				if(c != null) {
					this.command = c;
				} else {
					this.options = cmd;
				}
			}
		}
		if(this.command == rootCommand) {
			this.command = null;
			rootCommand.console.err.println("Unknown command: \"" + cmds.get(0) + "\".  Use \"<a href=\"help\">help</a>\" to see a list of commands.");
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
	
	private List<String> split(String command) {
		List<String> commands = new ArrayList<String>();
		boolean instr = false;
		int s1 = 0, s2 = 0;
		for( ; s2 < command.length(); s2++) {
			char c = command.charAt(s2);
			if(c == '"') {
				instr = !instr;
			}
			else if(!instr && c == ' ') {
				commands.add(command.substring(s1,s2));
				while(++s2 < command.length() && command.charAt(s2) == ' ');
				s1 = s2;
				if(command.charAt(s2) == '"') instr = true;
			}
		}
		if(s2 > s1) {
			commands.add(command.substring(s1,s2));
		}
		return commands;
	}

}
