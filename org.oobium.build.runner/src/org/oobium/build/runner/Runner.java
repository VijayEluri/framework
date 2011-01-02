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
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.oobium.build.runner.RunEvent.Type;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Workspace;
import org.oobium.utils.FileUtils;
import org.oobium.utils.Config.Mode;

public class Runner {

	private final Workspace workspace;
	private final Application application;
	private final Mode mode;
	private final Map<String, String> properties;
	private volatile Process process;
	private StreamGobbler errGobbler;
	private StreamGobbler outGobbler;
	private UpdaterThread updater;
	private Thread shutdownHook;
	
	String startString;
	
	Runner(Workspace workspace, Application application, Mode mode, Map<String, String> properties) {
		this.workspace = workspace;
		this.application = application;
		this.mode = mode;
		this.properties = properties;
		startString = "(INFO)  " + application.name + ": Application " + application.getName() + " started";
	}

	public Application getApplication() {
		return application;
	}
	
	/**
	 * Returns a copy of the map of properties that this runner is using.
	 * @return a map of properties; never null
	 */
	public Map<String, String> getProperties() {
		if(properties == null) {
			return new LinkedHashMap<String, String>(0);
		}
		return new LinkedHashMap<String, String>(properties);
	}
	
	void handleStarted() {
		RunnerService.notifyListeners(Type.Started, application);
		startString = null;
//		Listener.create("localhost:5050/listeners", "org.oobium.manager", "updated", RunnerService.class, new EventHandler() {
//			@Override
//			public void handleEvent(Event event) {
//				RunnerService.notifyListeners(Type.Updated, application);
//			}
//		});
	}
	
	public boolean isRunning() {
		return process != null;
	}
	
	public void pauseUpdater() {
		if(updater != null) {
			updater.paused(true);
		}
	}
	
	public void pauseUpdater(Bundle bundle, Collection<File> files) {
		if(updater != null) {
			updater.waitFor(bundle, files);
		}
	}
	
	public boolean setError(PrintStream err) {
		if(errGobbler != null) {
			errGobbler.setOut(err);
			return true;
		}
		return false;
	}
	
	public boolean setOut(PrintStream out) {
		if(outGobbler != null) {
			outGobbler.setOut(out);
			return true;
		}
		return false;
	}
	
	public boolean start() {
		if(process == null) {
			File exportDir = null;
			try {
				exportDir = application.export(workspace, mode, true, properties);
			} catch(IOException e) {
				return false;
			}

			File felixCache = new File(exportDir, "felix-cache");
			if(felixCache.exists()) {
				FileUtils.delete(felixCache);
			}

			ProcessBuilder builder = new ProcessBuilder();
			builder.command("java", "-jar", "bin/felix.jar");
			builder.directory(exportDir);
			
			try {
				process = builder.start();
			} catch(IOException e) {
				return false;
			}
			
			errGobbler = new StreamGobbler(process.getErrorStream()).activate();
			outGobbler = new StreamGobbler(this, process.getInputStream()).activate();

			shutdownHook = new Thread(application + " process shutdown hook") {
				public void run() {
					if(process != null) {
						process.destroy();
					}
				};
			};
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			
			updater = new UpdaterThread(workspace, application, mode);
			updater.start();
		}
		return true;
	}
	
	public void stop() {
		if(process != null) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			shutdownHook = null;
			
			updater.cancel();
			updater = null;
			
			Process p = process;
			process = null;
			
			try {
				p.destroy();
			} finally {
				try {
					p.getErrorStream().close();
				} catch(IOException e) { /*discard*/ }
				try {
					p.getInputStream().close();
				} catch(IOException e) { /*discard*/ }
				try {
					p.getOutputStream().close();
				} catch(IOException e) { /*discard*/ }
			}
		}
	}

	public void unpauseUpdater() {
		if(updater != null) {
			updater.paused(false);
		}
	}

}
