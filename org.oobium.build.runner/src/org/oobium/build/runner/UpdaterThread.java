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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.oobium.build.runner.RunEvent.Type;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.utils.FileUtils;
import org.oobium.utils.Config.Mode;

class UpdaterThread extends Thread {

	private final Workspace workspace;
	private final Application application;
	private final String domain;
	private final int port;

	private Map<Bundle, Long> bundles;
	private Map<Bundle, Bundle> exported;

	private volatile boolean running;
	private volatile boolean paused;

	private Object waitLock;
	private Map<Bundle, Set<File>> waitFor;
	

	UpdaterThread(Workspace workspace, Application application, Mode mode) {
		super(application + ":" + mode + " updater");
		setDaemon(true);
		this.waitLock = new Object();
		this.workspace = workspace;
		this.application = application;
		this.domain = "localhost";
		this.port = 5050;
		this.bundles = new HashMap<Bundle, Long>();
		this.exported = new HashMap<Bundle, Bundle>();
		this.bundles.put(application, getLastModified(application));
		for(Bundle bundle : application.getDependencies(workspace, mode)) {
			this.bundles.put(bundle, getLastModified(bundle));
		}
	}
	
	synchronized void cancel() {
		running = false;
		interrupt();
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
	
	public void paused(boolean paused) {
		this.paused = paused;
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
					Bundle update = workspace.export(bundle);
					bundles.put(bundle, lastModified);
					update(domain, port, bundle, "file:" + update.file.getAbsolutePath());
					RunnerService.notifyListeners(Type.Update, application, update);
					Bundle previous = exported.put(bundle, update);
					if(previous != null) {
						previous.delete();
					}
					break; // just update the first one, we'll be back soon if there's more...
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
