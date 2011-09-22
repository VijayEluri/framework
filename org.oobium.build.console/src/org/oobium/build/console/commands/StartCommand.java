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
package org.oobium.build.console.commands;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Application;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.Mode;

public class StartCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = -1;
		parseFlags = false;
	}
	
	@Override
	public void run() {
		Mode mode = Mode.DEV;
		
		Map<String, String> properties = new LinkedHashMap<String, String>();
		if(options == null) {
			properties.put(Logger.SYS_PROP_CONSOLE, "debug");
		} else {
			for(Entry<String, String> entry : paramMap().entrySet()) {
				String key = entry.getKey();
				if(key.equals("log")) {
					properties.put(Logger.SYS_PROP_CONSOLE, entry.getValue());
				} else if(key.equals("mode")) {
					mode = Mode.parse(entry.getValue());
				} else {
					properties.put(key, entry.getValue());
				}
			}
		}
		
		Application app = getApplication();
		RunnerService.start(getWorkspace(), app, mode, properties);
	}
	
}
