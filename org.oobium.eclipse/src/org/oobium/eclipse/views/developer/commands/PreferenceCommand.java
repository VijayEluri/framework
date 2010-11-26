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
package org.oobium.eclipse.views.developer.commands;

import java.util.Map.Entry;

import org.eclipse.jface.preference.IPreferenceStore;
import org.oobium.build.console.BuilderCommand;
import org.oobium.eclipse.OobiumPlugin;

public class PreferenceCommand extends BuilderCommand {

	@Override
	public void configure() {
		parseFlags = false;
	}
	
	@Override
	public void run() {
		for(Entry<String, String> entry : paramMap().entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			IPreferenceStore preferences = OobiumPlugin.getInstance().getPreferenceStore();
			preferences.setValue(name, value);
		}
	}

}
