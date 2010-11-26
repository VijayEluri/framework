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

public class Suggestion implements Comparable<Suggestion> {

	public static final int NONE	= 0;
	public static final int DEFAULT	= 1 << 0;
	public static final int COMMAND	= 1 << 1;
	
	private final String name;
	private final String description;
	private final String htmlDescription;
	private final int style;

	public Suggestion(Command command, boolean defaultCommand) {
		this(command.getName(), command.getDescription(false), command.getDescription(true), defaultCommand);
	}
	
	public Suggestion(String name, String help) {
		this(name, help, NONE);
	}
	
	public Suggestion(String name, String help, int type) {
		this.name = name;
		this.description = help;
		this.htmlDescription = null;
		this.style = type;
	}
	
	public Suggestion(String name, String description, String htmlDescription, boolean defaultCommand) {
		if(name == null || name.length() == 0) {
			throw new IllegalArgumentException("name cannot be blank");
		}
		this.name = name;
		this.description = description;
		this.htmlDescription = htmlDescription;
		this.style = defaultCommand ? (DEFAULT | COMMAND) : COMMAND;
	}

	@Override
	public int compareTo(Suggestion suggestion) {
		return name.compareTo(suggestion.name);
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getHtmlDescription() {
		return (htmlDescription != null) ? htmlDescription : description;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isCommand() {
		return (style & COMMAND) != 0;
	}
	
	public boolean isDefault() {
		return (style & DEFAULT) != 0;
	}
	
	public int length() {
		return name.length();
	}
	
	public boolean startsWith(String s) {
		return name.startsWith(s);
	}

	@Override
	public String toString() {
		return name;
	}
	
}
