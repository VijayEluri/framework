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

import java.util.Map;

import org.oobium.app.dev.AppDevActivator;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.runner.Runner;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Application;
import org.oobium.events.models.Event;
import org.oobium.events.models.EventHandler;
import org.oobium.events.models.Listener;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.json.JsonUtils;

public class StartCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = -1;
		parseFlags = false;
	}
	
	@Override
	public void run() {
		if(options == null) {
			options = "-D" + Logger.SYS_PROP_CONSOLE + "=debug";
		} else if(hasParam("log")) {
			options = "-D" + Logger.SYS_PROP_CONSOLE + "=" + param("log");
		} else if(!hasParam("-D" + Logger.SYS_PROP_CONSOLE)) {
			options += " -D" + Logger.SYS_PROP_CONSOLE + "=debug";
		}
		Mode mode = Mode.DEV; // TODO allow setting mode via options...?
		Application app = getApplication();
		Runner runner = RunnerService.start(getWorkspace(), app, mode, options.split(" "));
		
		if(runner.isRunning() && mode == Mode.DEV) {
			new Thread("test") {
				public void run() {
					try {
						sleep(5000);
						Listener.create("localhost:5050/listeners", AppDevActivator.ID, "openType", RunnerService.class, new EventHandler() {
							@Override
							public void handleEvent(Event event) {
								Map<String, String> map = JsonUtils.toStringMap(event.getData());
								BuilderConsoleActivator.sendOpenType(map.get("type"), map.get("line"));
							}
						});
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
	}
	
}
