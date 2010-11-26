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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.events.models.Event;
import org.oobium.events.models.EventHandler;
import org.oobium.events.models.Listener;
import org.oobium.utils.StringUtils;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.json.JsonUtils;

public class MigrateCommand_Bak extends BuilderCommand implements EventHandler {

	private static final int EXISTING_UNINSTALLED	= 1;
	private static final int MIGRATION_INSTALLED	= 2;
	private static final int APPLICATION_STOPPED	= 3;
	private static final int PACKAGES_REFRESHED		= 4;
	private static final int MIGRATION_RUN			= 5;
	private static final int MIGRATION_UNINSTALLED	= 6;
	private static final int APPLICATION_STARTED	= 7;

	private static final String LOCATION = "localhost:5050/listeners";
	private static final String SERVICE = "org.oobium.manager";
	private static final String DOMAIN = "localhost";
	private static final int 	PORT = 5050;
	
	
	private AtomicInteger step;
	private Bundle[] bundles;
	private Bundle deployedApp;
	private Bundle migration;
	
	public MigrateCommand_Bak() {
		step = new AtomicInteger();
	}
	
	@Override
	public void configure() {
		applicationRequired = true;
	}
	
	private boolean isSuccess(Event event) {
		Map<?,?> map = JsonUtils.toMap(event.getData());
		return "success".equals(map.get("status"));
	}

	private String getFailMessage(Event event) {
		Map<?,?> map = JsonUtils.toMap(event.getData());
		return (String) map.get("message");
	}
	
	private void abort(String message, Listener listener) {
		if(listener != null) {
			listener.destroy();
		}
		
		step.set(0);
		
		console.err.println(message);
		console.err.println("migration aborted");

		RunnerService.unpauseUpdaters();
	}

	/**
	 * uninstall old stuff
	 * install migration
	 * stop application
	 * refresh packages
	 * run migration
	 * uninstall migration
	 * start application
	 * @param event
	 */
	
	private void handleExistingUninstalled() {
		Listener listener = Listener.createOneShot(LOCATION, SERVICE, "installed", RunnerService.class, this);
		if(Bundle.install(DOMAIN, PORT, bundles)) {
			step.incrementAndGet();
		} else {
			abort("failed to install: " + StringUtils.asString(bundles), listener);
		}
	}
	
	private void handleMigrationInstalled() {
		Listener listener = Listener.createOneShot(LOCATION, SERVICE, "stopped", RunnerService.class, this);
		if(Bundle.stop(DOMAIN, PORT, deployedApp.name)) {
			step.incrementAndGet();
		} else {
			abort("failed to stop the " + deployedApp, listener);
		}
	}
	
	private void handleApplicationStopped() {
		Listener.createOneShot(LOCATION, SERVICE, "refreshed", RunnerService.class, this);
		Bundle.refresh(DOMAIN, PORT);
		step.incrementAndGet();
	}
	
	private void handlePackagesRefreshed() {
		Listener listener = Listener.createOneShot(LOCATION, SERVICE, "migrated", RunnerService.class, this);
		if(Bundle.start(DOMAIN, PORT, migration)) {
			step.incrementAndGet();
		} else {
			abort("failed to start the " + migration, listener);
		}
	}
	
	private void handleMigrated() {
		Listener listener = Listener.createOneShot(LOCATION, SERVICE, "uninstalled", RunnerService.class, this);
		if(Bundle.uninstall(DOMAIN, PORT, bundles)) {
			step.incrementAndGet();
		} else {
			abort("failed to start the " + migration, listener);
		}
	}
	
	private void handleMigrationUninstalled() {
		Listener listener = Listener.createOneShot(LOCATION, SERVICE, "started", RunnerService.class, this);
		if(Bundle.start(DOMAIN, PORT, deployedApp)) {
			step.incrementAndGet();
		} else {
			abort("failed to start the " + deployedApp, listener);
		}
	}
	
	private void handleApplicationStarted() {
		deployedApp = null;
		migration = null;
		step.set(0);
		RunnerService.unpauseUpdaters();
	}
	
	
	@Override
	public void handleEvent(Event event) {
		System.out.println("handleEvent: " + event.getEventName() + " @ " + step.get());

		if(isSuccess(event)) {
			switch(step.get()) {
			case EXISTING_UNINSTALLED:	handleExistingUninstalled();	break;
			case MIGRATION_INSTALLED:	handleMigrationInstalled();		break;
			case APPLICATION_STOPPED:	handleApplicationStopped();		break;
			case PACKAGES_REFRESHED:	handlePackagesRefreshed();		break;
			case MIGRATION_RUN:			handleMigrated();				break;
			case MIGRATION_UNINSTALLED:	handleMigrationUninstalled();	break;
			case APPLICATION_STARTED:	handleApplicationStarted();		break;
			}
		} else {
			abort(getFailMessage(event), null);
		}
	}

	@Override
	public void run() {
//		if(running) {
//			console.err.println("cannot start a migration while one is still running");
//			return;
//		}
		step.set(1);
		
		RunnerService.pauseUpdaters();
		
		Application app = getApplication();
		if(getWorkspace().getMigratorFor(app) == null) {
			abort("application does not have a migration", null);
			return;
		}

		if(!RunnerService.isRunning(app)) {
			abort("app is not running", null);
			return;
		}

		try {
			Mode mode = Mode.DEV;
			
			File schema = app.getSchema();
			if(!schema.isFile()) {
				app.createSchema(getWorkspace(), mode);
				BuilderConsoleActivator.sendRefresh(getWorkspace().getMigratorFor(app), 1000);
			}
			
			Collection<Bundle> exported = app.exportMigration(getWorkspace(), mode);
			bundles = exported.toArray(new Bundle[exported.size()]);

			deployedApp = app.getExportedBundle(getWorkspace());
			for(Bundle bundle : bundles) {
				if(bundle.isMigration()) {
					migration = bundle;
					break;
				}
			}

			String[] names = new String[bundles.length];
			for(int i = 0; i < names.length; i++) {
				names[i] = bundles[i].name;
			}
			
			Listener listener = Listener.createOneShot(LOCATION, SERVICE, "uninstalled", RunnerService.class, this);
			if(!Bundle.uninstall(DOMAIN, PORT, names)) {
				listener.destroy(); // nothing to uninstall, discard listener and continue
				handleExistingUninstalled();
			}
		} catch(Exception e) {
			console.err.print(e);
		}
	}
	
}
