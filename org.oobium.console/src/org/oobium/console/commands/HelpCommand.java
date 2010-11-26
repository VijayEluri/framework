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
package org.oobium.console.commands;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.oobium.console.Command;


public class HelpCommand extends Command {

	private Command command;
	
	public HelpCommand(Command command) {
		this.command = command;
	}

	@Override
	public void configure() {
		maxParams = 0;
		name = "help";
	}
	
	private void append(int lvl, StringBuilder sb, Command subcommand, String defaultCommand, int len) {
		if(lvl > 0) {
			sb.append('\n');
		}
		for(int i = 0; i < lvl*5; i++) {
			sb.append(' ');
		}
		
		String name = subcommand.getName();
		String fullName = subcommand.getFullName();
		
		if(defaultCommand == name) {
			sb.append('*');
		} else {
			sb.append(' ');
		}
		
		if(lvl == 0 && !subcommand.hasSubCommands() && !flag('a') && !"help".equals(name)) {
			sb.append("<a href=\"").append(fullName).append(" help\">").append(name).append("</a>");
		} else {
			sb.append(name);
		}
		
		for(int i = name.length(); i < len; i++) {
			sb.append(' ');
		}
		
		if(!flag('a') || flag('l')) {
			sb.append(" - ");
			sb.append(subcommand.getDescription());
		}
		
		if(subcommand.hasSubCommands()) {
			if(flag('a')) {
				String defaultCommand2 = subcommand.getDefaultCommand();
				Map<String, Command> subcommands = subcommand.getSubCommands();
				Set<String> names = subcommands.keySet();
				int len2 = getMaxLen(names);
				if(defaultCommand2 != null) {
					len2++;
				}
				for(String name2 : names) {
					append(lvl+1, sb, subcommands.get(name2), defaultCommand2, len2);
				}
			} else {
				sb.append(' ').append('{').append(' ');
				Set<String> subs = subcommand.getSubCommands().keySet();
				subs.remove("help");
				for(Iterator<String> iter = subs.iterator(); iter.hasNext(); ) {
					String sub = iter.next();
					sb.append("<a href=\"").append(fullName).append(' ').append(sub).append(" help\">").append(sub).append("</a>");
					if(iter.hasNext()) {
						sb.append(' ').append('|').append(' ');
					}
				}
				sb.append(' ').append('}');
			}
		}
	}
	
	@Override
	public void run() {
		StringBuilder sb = new StringBuilder();
		if(command.hasSubCommands()) {
			String defaultCommand = command.getDefaultCommand();
			Map<String, Command> subcommands = command.getSubCommands();
			Set<String> names = subcommands.keySet();
			int len = getMaxLen(names);
			if(defaultCommand != null) {
				len++;
			}
			for(Iterator<String> iter = names.iterator(); iter.hasNext(); ) {
				String name = iter.next();
				append(0, sb, subcommands.get(name), defaultCommand, len);
				if(iter.hasNext()) {
					sb.append('\n');
				}
			}
			if(defaultCommand != null) {
				sb.append('\n');
				for(int i = 0; i < len; i++) {
					sb.append(' ');
				}
				sb.append("* indicates the default command\n");
			}
			if(!flag('a') || flag('l')) {
				sb.append("\n ");
				for(int i = 0; i < len; i++) {
					sb.append('-');
				}
				sb.append("\n <Tab> auto-completes (double-tab to show a list of possible completions)");
				sb.append("\n <Ctrl-Space> opens content-assist window to show possible completions in greater detail");
			}
		} else {
			sb.append(command.getDescription());
			String usage = command.getUsage();
			if(usage != null && usage.length() > 0) {
				sb.append("\n  usage: ").append(usage);
			}
		}
		console.out.println(sb.toString());
	}
	
	private int getMaxLen(Collection<String> strs) {
		int len = 0;
		for(String str : strs) {
			len = Math.max(len, str.length());
		}
		return len;
	}
	
}
