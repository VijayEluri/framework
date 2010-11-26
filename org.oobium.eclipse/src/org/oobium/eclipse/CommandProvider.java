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
package org.oobium.eclipse;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.oobium.console.Command;

public interface CommandProvider {

	public abstract Map<String, Command> getCommands();
	
	public abstract ResourceBundle getResources(Locale locale);
	
}
