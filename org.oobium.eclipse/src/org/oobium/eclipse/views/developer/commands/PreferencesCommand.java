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

import org.eclipse.jface.preference.IPreferenceStore;
import org.oobium.build.console.BuilderCommand;
import org.oobium.console.Region;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.developer.ConsoleView;

public class PreferencesCommand extends BuilderCommand {

	private ConsoleView view;
	
	public PreferencesCommand(ConsoleView view) {
		this.view = view;
	}
	
	@Override
	public void run() {
		IPreferenceStore preferences = OobiumPlugin.getInstance().getPreferenceStore();
		for(String name : view.getPreferences()) {
			console.out.print("  ");
			console.out.print(name);
			console.out.print('=');
			String preference = preferences.getString(name);
			if(preference.length() > 0) {
				console.out.print(preference, Region.BLUE);
			}
			console.out.println();
		}
	}

}
