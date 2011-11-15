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

import org.oobium.app.server.Websocket;
import org.oobium.build.runner.RunEvent.Type;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Exporter;
import org.oobium.build.workspace.Workspace;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.Mode;

public class Runner {

	private final Logger logger;
	private final Workspace workspace;
	private final Application application;
	private final Mode mode;
	private final Map<String, String> properties;
	private volatile Process process;
	private StreamGobbler errGobbler;
	private StreamGobbler outGobbler;
	private UpdaterThread updater;
	private Thread shutdownHook;
	
	private String startString;
	
	private boolean debug;
	
	Runner(Workspace workspace, Application application, Mode mode, Map<String, String> properties) {
		this.logger = LogProvider.getLogger(RunnerService.class);
		this.workspace = workspace;
		this.application = application;
		this.mode = mode;
		this.properties = properties;
		
		this.properties.put("org.oobium.manager.url", "localhost:3030/tether/" + application.name);
		
		startString = "(INFO)  " + application.name + ": Application " + application.getName() + " started";
	}

	public void editing(File project, File file, boolean editing) {
		if(updater != null) {
			updater.editing(project, file, editing);
		}
	}
	
	public Application getApplication() {
		return application;
	}
	
	public boolean getDebug() {
		return debug;
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
	
	public String getStartString() {
		return startString;
	}
	
	void handleStarted() {
		RunnerService.notifyListeners(Type.Started, application);
		startString = null;
	}
	
	public boolean isAutoMigrating() {
		return updater.isAutoMigrating();
	}
	
	public boolean isRunning() {
		return process != null;
	}
	
	public void pauseMigratorUpdater() {
		if(updater != null) {
			updater.paused(true, true);
		}
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
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public boolean setError(PrintStream err) {
		if(errGobbler != null) {
			errGobbler.setError(err);
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
			shutdownHook = new Thread(application + " process shutdown hook") {
				public void run() {
					if(process != null) {
						process.destroy();
					}
				};
			};
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			
			File exportDir = null;
			Exporter exporter = null;
			try {
				exporter = new Exporter(workspace, application);
				exporter.setMode(mode);
				exporter.setClean(true);
				exporter.setProperties(properties);
				exporter.setIncludeMigrator(true);
				exporter.export();
			} catch(IOException e) {
				return false;
			}
			exportDir = exporter.getExportDir();
			
			updater = new UpdaterThread(workspace, application, exporter.getBundles());
			updater.start();

			ProcessBuilder builder = new ProcessBuilder();
			if(debug) {
//				builder.command("java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,address=127.0.0.1:8000,suspend=y", "-jar", "bin/felix.jar");
				builder.command("java", "-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,address=8000", "-jar", "bin/felix.jar");
			} else {
				builder.command("java", "-jar", "bin/felix.jar");
			}
			builder.directory(exportDir);
			
			try {
				process = builder.start();
			} catch(IOException e) {
				logger.warn(e.getMessage());
				return false;
			}
			try {
				int exitValue = process.exitValue();
				logger.warn("process exited with value: " + exitValue);
				return false;
			} catch(IllegalThreadStateException e) {
				// this means it hasn't exited yet, good - continue
			}
			
			errGobbler = new StreamGobbler(this, process.getErrorStream()).activate();
			outGobbler = new StreamGobbler(this, process.getInputStream()).activate();
		}
		return true;
	}
	
	public void stop(Websocket socket) {
		if(socket == null) {
			stop(true);
		} else {
			socket.write("shutdown");
			stop(false);
		}
	}
	
	private void stop(boolean now) {
		if(process != null) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			shutdownHook = null;
			
			updater.cancel();
			updater = null;
			
			final Process p = process;
			process = null;

			if(now) {
				terminate(p, -1);
			} else {
				new Thread("stopper") {
					public void run() {
						int exitValue = -1;
						for(int i = 0; i < 100; i++) { // 1/100 second intervals
							try {
								logger.debug("waiting for exit: {}", i);
								exitValue = p.exitValue();
								break;
							} catch(IllegalThreadStateException e) {
								try {
									Thread.sleep(100);
								} catch(InterruptedException e1) {
									// discard
								}
							}
						}
						terminate(p, exitValue);
					};
				}.start();
			}
		}
	}

	private void terminate(Process process, int exitValue) {
		try {
			if(exitValue == -1) {
				logger.info("terminating process");
				process.destroy();
				// TODO implement a check and display exitValue?
			}
			String msg = "process terminated (" + exitValue + ")";
			outGobbler.println(msg);
			logger.info(msg);
		} finally {
			try {
				process.getErrorStream().close();
			} catch(IOException e) { /*discard*/ }
			try {
				process.getInputStream().close();
			} catch(IOException e) { /*discard*/ }
			try {
				process.getOutputStream().close();
			} catch(IOException e) { /*discard*/ }
		}
	}
	
	public void unpauseUpdater() {
		if(updater != null) {
			updater.paused(false);
		}
	}

	public void unpauseMigratorUpdater() {
		if(updater != null) {
			updater.paused(false, true);
		}
	}

}
