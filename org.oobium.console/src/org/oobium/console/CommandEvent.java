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

public class CommandEvent {

	public static final int COMPLETE = 0;
	
	public int type;
	public Command command;
	
	public CommandEvent(int type, Command command) {
		this.type = type;
		this.command = command;
	}
	
}
