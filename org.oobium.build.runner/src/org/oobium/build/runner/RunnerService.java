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
package org.oobium.build.runner;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.app.AppService;
import org.oobium.app.persist.MemoryPersistService;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.server.Websocket;
import org.oobium.build.runner.RunEvent.Type;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Workspace;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.json.JsonUtils;

public class RunnerService extends AppService {

	public static final String ID = RunnerService.class.getPackage().getName();

	private static RunnerService instance;

	public static void addListener(RunListener listener) {
		synchronized(instance.listeners) {
			if(!instance.listeners.contains(listener)) {
				instance.listeners.add(listener);
			}
		}
	}

	public static RunnerService getInstance() {
		return instance;
	}
	
	public static List<String> getPaths(Application app) {
		synchronized(instance.runners) {
			if(isRunning(app)) {
				try {
					int port = app.getPort(Mode.DEV);
					ClientResponse response = Client.client("http://localhost:" + port).get("/" + app.getName() + "/paths");
					if(response.isSuccess()) {
						String prefix = "http://localhost:" + port;
						List<String> paths = JsonUtils.toStringList(response.getBody());
						for(int i = 0; i < paths.size(); i++) {
							paths.set(i, prefix + paths.get(i));
						}
						return paths;
					}
				} catch(MalformedURLException e) {
					// should never happen
					throw new IllegalStateException();
				}
			}
			return new ArrayList<String>(0);
		}
	}

	public static Application getRunningApp(String appName) {
		synchronized(instance.runners) {
			return instance.apps.get(appName);
		}
	}

	public static Runner getRunner(Application app) {
		synchronized(instance.runners) {
			return instance.runners.get(app);
		}
	}
	
	public static Runner getRunner(String appName) {
		synchronized(instance.runners) {
			Application app = instance.apps.get(appName);
			return instance.runners.get(app);
		}
	}
	
	public static boolean isRunning(Application app) {
		synchronized(instance.runners) {
			Runner runner = instance.runners.get(app);
			if(runner != null) {
				return runner.isRunning();
			}
			return false;
		}
	}
	
	public static boolean isRunning(String appName) {
		synchronized(instance.runners) {
			Application app = instance.apps.get(appName);
			Runner runner = instance.runners.get(app);
			if(runner != null) {
				return runner.isRunning();
			}
			return false;
		}
	}
	
	private static void notifyListeners(RunEvent event) {
		synchronized(instance.listeners) {
			if(!instance.listeners.isEmpty()) {
				for(RunListener listener : instance.listeners.toArray(new RunListener[instance.listeners.size()])) {
					listener.handleEvent(event);
				}
			}
		}
	}
	
	static void notifyListeners(Type type, Application app, Bundle...bundles) {
		RunEvent event = new RunEvent(type, app, bundles);
		notifyListeners(event);
	}
	
	static void notifyListeners(Type type, Application app, String message) {
		RunEvent event = new RunEvent(type, app);
		event.setMessage(message);
		notifyListeners(event);
	}
	
	static void notifyListeners(Type type, Application app, String message, Object details) {
		RunEvent event = new RunEvent(type, app);
		event.setMessage(message);
		event.setDetails(details);
		notifyListeners(event);
	}
	
	public static void pauseUpdaters() {
		synchronized(instance.runners) {
			for(Runner runner : instance.runners.values()) {
				runner.pauseUpdater();
			}
		}
	}
	
	public static void waitFor(Bundle bundle, Collection<File> files) {
		synchronized(instance.runners) {
			for(Runner runner : instance.runners.values()) {
				runner.pauseUpdater(bundle, files);
			}
		}
	}
	
	public static boolean removeListener(RunListener listener) {
		synchronized(instance.listeners) {
			return instance.listeners.remove(listener);
		}
	}

	public static Runner start(Workspace workspace, Application app, Mode mode, Map<String, String> properties) {
		synchronized(instance.runners) {
			Runner runner = instance.runners.get(app);
			if(runner == null) {
				runner = new Runner(workspace, app, mode, properties);
				if(runner.start()) {
					instance.apps.put(app.name, app);
					instance.runners.put(app, runner);
					notifyListeners(Type.Start, app);
				}
			}
			return runner;
		}
	}
	
	public static void stop(Application app) {
		synchronized(instance.runners) {
			Runner runner = instance.runners.remove(app);
			if(runner != null) {
				instance.apps.remove(app.name);
				Websocket ws = instance.getRouter().getWebsocket(app.name);
				runner.stop(ws);
				notifyListeners(Type.Stop, app);
			}
		}
	}

	public static void unpauseUpdaters() {
		synchronized(instance.runners) {
			for(Runner runner : instance.runners.values()) {
				runner.unpauseUpdater();
			}
		}
	}

	
	private final Map<String, Application> apps;
	private final Map<Application, Runner> runners;
	private final List<RunListener> listeners;
	
	public RunnerService() {
		instance = this;
		apps = new HashMap<String, Application>();
		runners = new HashMap<Application, Runner>();
		listeners = new ArrayList<RunListener>();
		setPersistService(new MemoryPersistService());
	}

	@Override
	public void addRoutes(Config config, AppRouter router) {
		router.addWebsocket("/tether/{id:[\\w\\.]+}", RunnerController.class);
	}
	
}
