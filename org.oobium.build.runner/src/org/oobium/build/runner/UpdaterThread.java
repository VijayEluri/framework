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

import static org.oobium.build.workspace.Bundle.update;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.oobium.build.runner.RunEvent.Type;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.utils.FileUtils;

class UpdaterThread extends Thread {

	private class MigrateListener implements RunListener {
		@Override
		public void handleEvent(RunEvent event) {
			if(event.type == Type.Started) {
				try {
					Thread.sleep(50); // TODO VERY TEMPORARY FIX
				} catch(InterruptedException e) {
				}
				handleEvent("/migrate");
			}
		}
		protected void handleEvent(final String command) {
			RunnerService.removeListener(this);
			RunnerService.notifyListeners(Type.Migrate, application);
			new Thread() {
				public void run() {
					ClientResponse response = Client.client(migratorHost, migratorPort).post(command);
					if(response.isSuccess()) {
						logger.info(response.getBody());
					} else {
						if(response.exceptionThrown()) {
							logger.warn("migrator at {}:{} threw an exception: {}", migratorHost, migratorPort, response.getException().getLocalizedMessage());
						} else {
							logger.warn("migrator at {}:{} completed with errors: {}", migratorHost, migratorPort, response.getBody());
						}
					}
					RunnerService.notifyListeners(Type.Migrated, application);
				};
			}.start();
		}
	}

	private class ReMigrateListener extends MigrateListener {
		@Override
		public void handleEvent(RunEvent event) {
			if(event.type == Type.Updated) {
				handleEvent("/migrate/redo/all");
			}
		}
	}


	private final Logger logger;
	private final Workspace workspace;
	private final Application application;
	private final String managerHost;
	private final int managerPort;
	private final String migratorHost;
	private final int migratorPort;

	private Map<Bundle, Long> bundles;
	private Map<Bundle, Bundle> exported;

	private ReMigrateListener reMigListener;
	
	private volatile boolean running;
	private volatile boolean paused;
	private volatile boolean migratorPaused;

	private final Object waitLock;
	private Map<Bundle, Set<File>> waitFor;
	
	private Map<Bundle, List<File>> editing;
	

	UpdaterThread(Workspace workspace, Application application, List<Bundle> bundles) {
		super(application + " updater");
		setDaemon(true);
		this.logger = LogProvider.getLogger(RunnerService.class);
		this.waitLock = new Object();
		this.workspace = workspace;
		this.application = application;
		this.managerHost = "localhost";
		this.managerPort = 5050;
		this.migratorHost = "localhost";
		this.migratorPort = 5001;
		this.bundles = new HashMap<Bundle, Long>();
		this.exported = new HashMap<Bundle, Bundle>();
		this.bundles.put(application, getLastModified(application));
		for(Bundle bundle : bundles) {
			logger.debug("updater monitoring: {}", bundle);
			this.bundles.put(bundle, getLastModified(bundle));
		}
		RunnerService.addListener(new MigrateListener());
	}
	
	synchronized void cancel() {
		running = false;
		interrupt();
	}

	synchronized void editing(File project, File file, boolean editing) {
		Bundle bundle = workspace.getBundle(project);
		if(editing) {
			if(this.editing == null) {
				this.editing = new HashMap<Bundle, List<File>>();
				List<File> files = new ArrayList<File>();
				files.add(file);
				this.editing.put(bundle, files);
			} else {
				List<File> files = this.editing.get(bundle);
				if(files == null) {
					this.editing.put(bundle, files = new ArrayList<File>());
				}
				files.add(file);
			}
		} else {
			if(this.editing != null) {
				List<File> files = this.editing.get(bundle);
				if(files != null) {
					if(files.remove(file) && files.isEmpty()) {
						this.editing.remove(bundle);
					}
				}
			}
		}
	}
	
	private synchronized File[] getEditing(Module module) {
		if(editing != null) {
			List<File> files = editing.get(module);
			if(files != null) {
				return files.toArray(new File[files.size()]);
			}
		}
		return null;
	}
	
	private long getLastModified(Bundle bundle) {
		long modified;
		
		if(bundle.isJar) {
			modified = bundle.file.lastModified();
		} else {
			if(bundle instanceof Module) {
				Module module = (Module) bundle;
				File[] src = FileUtils.findFiles(bundle.src, ".esp", ".emt", ".ess", ".ejs");
				if(src.length == 0) {
					modified = FileUtils.getLastModified(module.bin, module.assets);
				} else {
					File[] bin = module.getBinEFiles(src);
					long binMod = FileUtils.getLastModified(bin);
					long mod = FileUtils.getLastModified(module.bin);
					if(mod > binMod) {
						modified = mod;
					} else {
						long srcMod = FileUtils.getLastModified(src);
						if(srcMod > binMod) {
							modified = 0; // hasn't been compiled yet - don't update!
						} else {
							// bin may be newer than the src because the src hasn't been saved,
							//  but the bin is saved on each edit
							modified = srcMod;
						}
					}
				}
			} else {
				modified = FileUtils.getLastModified(bundle.bin);
			}
		}

		return modified;
	}

	private long getLastModified1(Bundle bundle) {
		if(bundle.isJar) {
			return bundle.file.lastModified();
		}
		
		if(bundle instanceof Module) {
			Module module = (Module) bundle;

			File[] ea = getEditing(module);
			if(ea != null) {
				long mod1 = FileUtils.getLastModified(ea);
				long mod2 = FileUtils.getLastModifiedExcluding(module.bin, module.getBinEFiles(ea));
				return Math.max(mod1, mod2);
			}
		}

		return FileUtils.getLastModified(bundle.bin);
	}
	
	public boolean isAutoMigrating() {
		return !migratorPaused;
	}

	public void paused(boolean paused) {
		this.paused = paused;
	}

	public void paused(boolean paused, boolean migratorOnly) {
		if(migratorOnly) {
			this.migratorPaused = paused;
		} else {
			this.paused = true;
		}
	}

	public void waitFor(Bundle bundle, Collection<File> files) {
		if(bundles.containsKey(bundle)) {
			synchronized(waitLock) {
				if(waitFor == null) {
					waitFor = new HashMap<Bundle, Set<File>>();
				}
				Set<File> list = waitFor.get(bundle);
				if(list == null) {
					list = new HashSet<File>();
					waitFor.put(bundle, list);
				}
				list.addAll(files);
			}
		}
	}

	public void run() {
		running = true;
		while(running) {
			try {
				sleep(1000);
				if(!paused) {
					// long start = System.currentTimeMillis();
					if(waitFor == null) {
						updateBundles();
					} else {
						updateWaitFor();
					}
					// System.out.println("updater: " + (System.currentTimeMillis() - start));
				}
			} catch(InterruptedException e) {
				// restart
			}
		}
	}

	private void updateBundles() {
		for(Entry<Bundle, Long> entry : bundles.entrySet()) {
			Bundle bundle = entry.getKey();
			long modified = entry.getValue();
			long lastModified = getLastModified(bundle);
			if(modified < lastModified) {
				try {
					Bundle update = workspace.export(application, bundle);
					logger.debug("updating {} ({} < {})", update, modified, lastModified);
					bundles.put(bundle, lastModified);
					if(!migratorPaused && update.isMigrator() && reMigListener == null) {
						reMigListener = new ReMigrateListener();
						RunnerService.addListener(reMigListener);
					}
					update(managerHost, managerPort, bundle, "file:" + update.file.getAbsolutePath());
					RunnerService.notifyListeners(Type.Update, application, update);
					Bundle previous = exported.put(bundle, update);
					if(previous != null) {
						previous.delete();
					}
//					break; // just update the first one, we'll be back soon if there's more...
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updateWaitFor() {
		synchronized(waitLock) {
			if(waitFor != null) { // just in case we missed the change while not synchronized
				for(Iterator<Entry<Bundle, Set<File>>> i1 = waitFor.entrySet().iterator(); i1.hasNext(); ) {
					Entry<Bundle, Set<File>> entry = i1.next();
					long modified = bundles.get(entry.getKey());
					Set<File> files = entry.getValue();
					for(Iterator<File> i2 = files.iterator(); i2.hasNext(); ) {
						File file = i2.next();
						if(modified < file.lastModified()) {
							i2.remove();
							if(files.isEmpty()) {
								i1.remove();
							}
						}
					}
				}
				if(waitFor.isEmpty()) {
					waitFor = null;
					updateBundles(); // TODO
				}
			}
		}
	}
	
}
