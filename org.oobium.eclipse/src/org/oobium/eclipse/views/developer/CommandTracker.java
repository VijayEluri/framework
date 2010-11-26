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
package org.oobium.eclipse.views.developer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.oobium.console.Command;
import org.oobium.console.Console;
import org.oobium.eclipse.CommandProvider;
import org.oobium.eclipse.OobiumPlugin;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class CommandTracker extends ServiceTracker {

	private final Console console;
	private final Command root;
	
	public CommandTracker(Console console) {
		super(OobiumPlugin.getInstance().getBundle().getBundleContext(), CommandProvider.class.getName(), null);
		this.console = console;
		this.root = console.getRootCommand();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		CommandProvider provider = (CommandProvider) OobiumPlugin.getContext().getService(reference);
		Map<String, Command> commands = provider.getCommands();
		for(Entry<String, Command> entry : commands.entrySet()) {
			root.add(entry.getKey(), entry.getValue());
		}
		ResourceBundle resourceBundle = provider.getResources(console.getLocale());
		if(resourceBundle != null) {
			console.addResourceStrings(resourceBundle);
		}
		return provider;
	}
	@Override
	public void modifiedService(ServiceReference reference, Object service) {
	}
	@Override
	public void removedService(ServiceReference reference, Object service) {
//		CommandProvider provider = (CommandProvider) service;
//		Map<String, Command> commands = provider.getCommands();
//		for(Entry<String, Command> entry : commands.entrySet()) {
//			root.remove(entry.getKey());
//		}
	}

}
