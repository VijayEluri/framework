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

import static org.oobium.utils.StringUtils.join;
import static org.oobium.utils.StringUtils.underscored;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.oobium.console.commands.HelpCommand;

public abstract class Command {

	private static class PlaceHolder extends Command {
		
		String name;

		public PlaceHolder(String name) {
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
	}
	
	protected Command parent;
	protected Console console;
	
	protected String name;
	protected Command helpCommand;
	
	protected Command defaultCommand;
	protected Map<String, Command> subcommands;
	
	/**
	 * The raw options passed into the command.<br>
	 * Only valid while the command is being executed.
	 */
	public String options;
	private Parameters params;

	protected boolean optionsRequired;
	protected int maxParams;
	protected int minParams;
	protected String[] requiredParams;
	protected boolean parseFlags;
	
	protected boolean runInDisplayThread;
	
	private List<CommandListener> listeners;
	
	public Command() {
		if(!(this instanceof HelpCommand || this instanceof PlaceHolder)) {
			helpCommand = new HelpCommand(this);
		}
		parseFlags = true;
	}

	public Command(String name) {
		this();
		this.name = name;
	}
	
	/**
	 * Add the given subcommand to this command's map of subcommands.
	 * If another subcommand exists with the same name, then it will
	 * be replaced with the given subcommand.
	 * @param subcommand
	 */
	public void add(Command subcommand) {
		subcommand.parent = this;
		subcommand.console = console;
		subcommand.configure();
		if(subcommand.helpCommand != null) {
			subcommand.helpCommand.console = console;
			subcommand.helpCommand.configure();
		}
		if(subcommands == null) {
			subcommands = new TreeMap<String, Command>();
		}
		subcommands.put(subcommand.getName(), subcommand);
	}
	
	/**
	 * Add the given subcommand to the parent command indicated by
	 * the given parentPath.
	 * If a command is missing from the hierarchy given by the path, then
	 * a {@link PlaceHolder} command will be created and the method
	 * will still complete successfully.
	 * @param parentPath a whitespace delimited list of command names leading to
	 * the parent command, to which the subcommand shall be added.
	 * @param subcommand
	 */
	public void add(String parentPath, Command subcommand) {
		String[] segments = parentPath.split("\\s+");
		if(subcommands == null) {
			subcommands = new TreeMap<String, Command>();
		}
		Command parent = subcommands.get(segments[0]);
		if(parent == null) {
			parent = new PlaceHolder(segments[0]);
			subcommands.put(segments[0], parent);
		}
		if(segments.length == 1) {
			parent.add(subcommand);
		} else {
			parent.add(join(Arrays.copyOfRange(segments, 1, segments.length), " "), subcommand);
		}
	}
	
	public void addListener(CommandListener listener) {
		if(listeners == null) {
			listeners = new ArrayList<CommandListener>();
		}
		listeners.add(listener);
	}

	protected String ask(String message) {
		console.out.print(message);
		return console.in.readLine();
	}
	
	protected String ask(String message, boolean password) {
		console.out.print(message);
		boolean prevState = console.in.password;
		console.in.password = password;
		try {
			return console.in.readLine();
		} finally {
			console.in.password = prevState;
		}
	}
	
	protected boolean canExecute() {
		if(optionsRequired && (options == null || options.length() == 0)) {
			console.err.println("missing required options string");
			return false;
		} else if(maxParams != -1 && params.list.size() > maxParams) {
			console.err.println("too many parameters (" + maxParams + ((maxParams == 1) ? " is " : " are ") + "permitted)");
			return false;
		} else if(params.list.size() < minParams) {
			console.err.println("too few parameters (" + minParams + ((minParams == 1) ? " is " : " are ") + "required)");
			return false;
		} else if(missingRequiredParam()) {
			console.err.println("missing required parameter(s)");
			return false;
		}
		return true;
	}
	
	/**
	 * Only valid during command execution
	 */
	public void clearFlags() {
		params.clearFlags();
	}

	protected void configure() {
		// subclasses to override if necessary
	}
	
	public void execute() {
		execute(null);
	}

	public void execute(String options) {
		if(parent == null) {
			// this is a root command - skip to the subcommands
			
		}
		this.options = options;
		params = new Parameters(options, parseFlags);
		try {
			if(preExecute()) {
				try {
					if(canExecute()) {
						try {
							if(runInDisplayThread) {
								console.getDisplay().syncExec(new Runnable() {
									@Override
									public void run() {
										Command.this.run();
										notifyListeners(CommandEvent.COMPLETE);
									}
								});
							} else {
								run();
								notifyListeners(CommandEvent.COMPLETE);
							}
						} catch(Exception e) {
							console.err.println(e);
						}
					}
				} finally {
					postExecute();
				}
			}
		} finally {
			options = null;
			params = null;
		}
	}
	
	public void execute(String command, String options) {
		Command cmd = get(command);
		if(cmd != null) {
			cmd.execute(options);
		}
	}

	/**
	 * Only valid during command execution
	 */
	public boolean flag(char f) {
		return params.isSet(f);
	}
	
	public int flagCount() {
		return params.flagCount();
	}
	
	public Command get(String path) {
		String[] segments = path.split("\\s+");
		if(subcommands != null) {
			Command parent = subcommands.get(segments[0]);
			if(parent != null) {
				if(segments.length == 1) {
					return parent;
				} else {
					return parent.get(join(Arrays.copyOfRange(segments, 1, segments.length), " "));
				}
			}
		}
		if("help".equals(path)) {
			return helpCommand;
		}
		return null;
	}

	public Console getConsole() {
		return console;
	}

	public String getDefaultCommand() {
		if(defaultCommand != null) {
			return defaultCommand.getName();
		}
		return null;
	}
	
	public String getDescription() {
		return getDescription(false);
	}
	
	public String getDescription(boolean html) {
		String key = getPropertyKey("description");
		if(html) {
			String description = console.resources.getString(key + ".html", false);
			if(description != null && description.length() > 0) {
				return description;
			} // else - fall through and return the short description
		}
		return console.resources.getString(key, true);
	}
	
	public String getName() {
		if(name == null) {
			name = getClass().getSimpleName();
			name = underscored(name.substring(0, name.length()-7));
		}
		return name;
	}
	
	public Command getParent() {
		return parent;
	}
	
	public String getFullName() {
		StringBuilder sb = new StringBuilder();
		Command cmd = this;
		while(true) {
			sb.insert(0, cmd.getName());
			cmd = cmd.parent;
			if(cmd != null && cmd.parent != null) {
				sb.insert(0, ' ');
			} else {
				break;
			}
		}
		return sb.toString();
	}
	
	private String getPropertyKey(String...postFixes) {
		StringBuilder sb = new StringBuilder();
		Command cmd = hasDefaultCommand() ? defaultCommand : this;
		while(true) {
			sb.insert(0, cmd.getName());
			cmd = cmd.parent;
			if(cmd != null && cmd.parent != null) {
				sb.insert(0, '.');
			} else {
				break;
			}
		}
		sb.insert(0, "command.");
		for(String postFix : postFixes) {
			sb.append('.').append(postFix);
		}
		return sb.toString();
	}
	
	public Command getRoot() {
		if(parent == null) {
			return this;
		} else {
			return parent.getRoot();
		}
	}
	
	public Command getSubCommand(String name) {
		if("help".equals(name)) {
			return helpCommand;
		}
		if(subcommands != null) {
			return subcommands.get(name);
		}
		return null;
	}
	
	public Map<String, Command> getSubCommands() {
		if(this instanceof HelpCommand) {
			return subcommands;
		} else {
			if(subcommands == null) {
				return null;
			} else {
				Map<String, Command> subcommands = new TreeMap<String, Command>(this.subcommands);
				subcommands.put(helpCommand.getName(), helpCommand);
				return subcommands;
			}
		}
	}
	
	public Suggestion[] getSuggestions() {
		return getSuggestions(null);
	}
	
	public Suggestion[] getSuggestions(String cmd) {
		List<Command> cmds = new ArrayList<Command>();
		if(this instanceof HelpCommand) {
			if(subcommands != null) {
				for(Command scmd : subcommands.values()) {
					cmds.add(scmd);
				}
			}
		} else if(subcommands == null) {
			if(helpCommand != null) {
				cmds.add(helpCommand);
			}
		} else {
			if(subcommands != null) {
				for(Command scmd : subcommands.values()) {
					cmds.add(scmd);
				}
			}
			cmds.add(helpCommand);
		}
		Suggestion[] suggestions = new Suggestion[cmds.size()];
		for(int i = 0; i < suggestions.length; i++) {
			boolean isDefault = (cmds.get(i) == defaultCommand);
			suggestions[i] = new Suggestion(cmds.get(i), isDefault);
		}
		
		if(cmd == null) {
			return suggestions;
		} else {
			return suggest(cmd, suggestions);
		}
	}
	
	public String getUsage() {
		String key = getPropertyKey("usage");
		return console.resources.getString(key, false);
	}
	
	public boolean hasDefaultCommand() {
		return defaultCommand != null;
	}
	
	public boolean hasFlags() {
		return params.hasFlags();
	}
	
	/**
	 * Only valid during command execution
	 */
	public boolean hasParam(String name) {
		return params.map.containsKey(name);
	}

	/**
	 * Only valid during command execution
	 */
	public boolean hasParam(int index) {
		return index >= 0 && index < params.list.size();
	}

	public boolean hasSubCommands() {
		return subcommands != null && !subcommands.isEmpty();
	}
	
	private boolean missingRequiredParam() {
		if(requiredParams != null && requiredParams.length > 0) {
			for(String s : requiredParams) {
				if(!params.map.containsKey(s)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void notifyListeners(int type) {
		if(listeners != null) {
			CommandEvent event = new CommandEvent(type, this);
			for(CommandListener listener : listeners.toArray(new CommandListener[listeners.size()])) {
				listener.handleEvent(event);
			}
		}
	}
	
	/**
	 * Only valid during command execution
	 */
	public String param(int index) {
		if(index >= 0 && index < params.list.size()) {
			return params.list.get(index);
		}
		return null;
	}
	
	/**
	 * Only valid during command execution
	 */
	public String param(String name) {
		return params.map.get(name);
	}
	
	/**
	 * Only valid during command execution
	 */
	public <T> T param(String name, Class<T> type) {
		return coerce(params.map.get(name), type);
	}
	
	/**
	 * Only valid during command execution
	 */
	public int paramCount() {
		return params.list.size();
	}

	/**
	 * Only valid during command execution
	 */
	public List<String> paramList() {
		return params.list;
	}
	
	/**
	 * Only valid during command execution
	 */
	public Map<String, String> paramMap() {
		return params.map;
	}
	
	protected void postExecute() {
		// subclasses to implement if necessary
	}
	
	protected boolean preExecute() {
		// subclasses to override if necessary
		return true;
	}
	
	public void printList(List<List<Object>> lists) {
		int columns = lists.get(0).size();
		int[] widths = new int[columns];

		for(List<Object> list : lists) {
			for(int i = 0; i < columns; i++) {
				Object o = list.get(i);
				int w = (o == null) ? 4 : o.toString().length();
				widths[i] = Math.max(w, widths[i]);
			}
		}

		boolean isHeader = true;
		for(List<Object> list : lists) {
			StringBuilder sb = new StringBuilder();
			sb.append(' ');
			for(int i = 0; i < columns; i++) {
				Object o = list.get(i);
				int w = (o == null) ? 4 : o.toString().length();
				sb.append(o);
				for(int j = w; j < widths[i]; j++) {
					sb.append(' ');
				}
				if(i < columns - 1) {
					sb.append(" | ");
				}
			}
			sb.append(' ');
			console.out.println(sb.toString());
			if(isHeader) {
				isHeader = false;
				sb = new StringBuilder();
				sb.append('-');
				for(int i = 0; i < columns; i++) {
					for(int j = 0; j < widths[i]; j++) {
						sb.append('-');
					}
					if(i < columns - 1) {
						sb.append('-').append('-').append('-');
					}
				}
				sb.append('-');
				console.out.println(sb.toString());
			}
		}
	}

	public void removeListener(CommandListener listener) {
		if(listeners != null) {
			listeners.remove(listener);
		}
	}

	public void retainFlags(char...f) {
		params.retainFlags(f);
	}

	/**
	 * Default implementation calls {@link #run()} on the 
	 * defaultCommand, if one is set (after first setting its
	 * <code>params</code> variable).<br/>
	 * Subclasses to override if necessary.
	 */
	protected void run() {
		if(defaultCommand != null) {
			defaultCommand.params = params;
			if(defaultCommand.canExecute()) {
				defaultCommand.run();
			}
		}
		// subclasses to override if necessary
	}

	public void set(Command subcommand) {
		this.defaultCommand = subcommand;
		add(subcommand);
	}
	
	/**
	 * Only valid during command execution
	 */
	public void setFlag(char...f) {
		for(char c : f) {
			params.setFlag(c);
		}
	}

	/**
	 * Default implementation simply return the given array of suggestions.
	 * Subclasses should override to modify this behavior and add their own suggestions.
	 * @param suggestions
	 * @return
	 */
	protected Suggestion[] suggest(String cmd, Suggestion[] suggestions) {
		return suggestions;
	}
	
	@Override
	public String toString() {
		return super.toString() + " { " + getName() + " }";
	}
	
	/**
	 * Only valid during command execution
	 */
	public void unsetFlag(char...f) {
		for(char c : f) {
			params.unsetFlag(c);
		}
	}
	
}
