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
package org.oobium.persist.migrate;

import static org.oobium.http.HttpRequest.Type.POST;
import static org.oobium.utils.literal.Map;
import static org.oobium.utils.literal.e;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oobium.app.AppService;
import org.oobium.app.server.routing.Router;
import org.oobium.persist.PersistService;
import org.oobium.persist.migrate.controllers.MigrateController;
import org.oobium.persist.migrate.controllers.PurgeController;
import org.oobium.persist.migrate.controllers.RedoController;
import org.oobium.persist.migrate.controllers.RollbackController;
import org.oobium.utils.Config;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public abstract class MigratorService extends AppService {

	private static MigratorService instance;
	
	public static MigratorService instance() {
		return instance;
	}
	
	
	protected String appName;
	
	protected String appVersion;
	protected Config appConfig;
	protected String migName;

	protected String migVersion;
	protected String schema;
	protected Config migConfig;
	
	protected ServiceTracker msTracker;

	public MigratorService() {
		instance = this;
	}
	
	@Override
	public void addRoutes(Config config, Router router) {
		router.addRoute(POST, "/migrate", 							MigrateController.class);
		router.addRoute(POST, "/migrate/{name:\\w+}/{dir=up}", 		MigrateController.class);
		router.addRoute(POST, "/migrate/{name:\\w+}/{dir=down}", 	MigrateController.class);
		router.addRoute(POST, "/migrate/to/{name:\\w+}", 			MigrateController.class);
		router.addRoute(POST, "/migrate/rollback", 					RollbackController.class);
		router.addRoute(POST, "/migrate/rollback/{step=all}", 		RollbackController.class);
		router.addRoute(POST, "/migrate/rollback/{step:\\d+}", 		RollbackController.class);
		router.addRoute(POST, "/migrate/redo", 						RedoController.class);
		router.addRoute(POST, "/migrate/redo/{step=all}", 			RedoController.class);
		router.addRoute(POST, "/migrate/redo/{step:\\d+}", 			RedoController.class);
		router.addRoute(POST, "/migrate/purge", 					PurgeController.class);
	}

	private Bundle getBundle(ServiceReference ref, String symbolicName) {
		PackageAdmin admin = (PackageAdmin) getContext().getService(ref);
		Bundle[] bundles = admin.getBundles(symbolicName, null);
		if(bundles.length == 0) {
			throw new IllegalStateException("bundle " + symbolicName + " is not present - cannot run migration");
		} else if(bundles.length == 1) {
			return bundles[0];
		} else {
			throw new IllegalStateException("no more than 2 bundles of " + symbolicName + " may be present to run a migration");
		}
	}
	
	protected String getCurrentMigration() {
		try {
			return (String) getPersistService().executeQueryValue("select detail from system_attrs where name='migration.current'");
		} catch(SQLException e) {
			return null;
		}
	}
	
	protected List<String> getMigrated() {
		try {
			String sql = "select detail from system_attrs where name='migrated'";
			List<List<Object>> lists = getPersistService().executeQueryLists(sql);
			List<String> migrated = new ArrayList<String>();
			for(int i = 1; i < lists.size(); i++) {
				migrated.add((String) lists.get(i).get(0));
			}
			return migrated;
		} catch(SQLException e) {
			getMigrationService().initializeDatabase(null);
			return Collections.emptyList();
		}
	}
	
	public abstract List<? extends Migration> getMigrations();

	public MigrationService getMigrationService() {
		MigrationService service = (MigrationService) msTracker.getService();
		if(service != null) {
			service.setPersistService(getPersistService());
			return service;
		}
		throw new IllegalStateException("MigrationService is not present - Migration cannot proceed");
	}
	
	private List<String> getNames(List<? extends Migration> migrations) {
		List<String> names = new ArrayList<String>();
		for(Migration migration : migrations) {
			names.add(migration.getClass().getSimpleName());
		}
		return names;
	}
	
	@Override
	public String getPersistClientName() {
		return appName + "_" + appVersion;
	}
	
	public PersistService getPersistService() {
		PersistService service = super.getPersistService();
		if(service != null) {
			return service;
		}
		throw new IllegalStateException("Migration cannot run without a PersistService: " + appConfig.get(Config.PERSIST));
	}
	
	@Override
	protected void initializeServiceTrackers(Config config) throws Exception {
		msTracker = new ServiceTracker(getContext(), MigrationService.class.getName(), null);
		msTracker.open();
	}
	
	@Override
	protected Config loadConfiguration() {
		return new Config(Map(
				e(Config.PERSIST, appConfig.get(Config.PERSIST, String.class)),
				e(Config.HOST, "localhost"),
				e(Config.PORT, "5001")
			));
	}
	
	public synchronized String migrate(String to) throws SQLException {
		List<? extends Migration> migrations = getMigrations();
		List<String> names = getNames(migrations);
		List<String> migrated = getMigrated();
		String current = getCurrentMigration();
		int cix = names.indexOf(current);
		int tix = (to == null) ? (names.size() - 1) : names.indexOf(to);
		if(tix == cix) {
			if(to == null) {
				return "no migration to perform: already migrated to the last one";
			} else {
				return "no migration to perform: \"" + to + "\" is the current migration";
			}
		} else {
			MigrationService mservice = getMigrationService();
			getPersistService().setAutoCommit(false);
			try {
				if(cix < tix) { // migrate up
					for(int i = cix + 1; i <= tix; i++) {
						String name = names.get(i);
						if(!migrated.contains(name)) {
							migrations.get(i).setService(mservice);
							migrations.get(i).up();
							setMigrated(name, true);
						}
					}
					setCurrentMigration(names.get(tix));
				} else { // migrate down
					for(int i = cix; i >= tix; i--) {
						String name = names.get(i);
						if(migrated.contains(name)) {
							migrations.get(i).setService(mservice);
							migrations.get(i).down();
							setMigrated(name, false);
						}
					}
					setCurrentMigration((tix < 0) ? null : names.get(tix));
				}
				getPersistService().commit();
				return "migrated successfully";
			} catch(SQLException e1) {
				try {
					getPersistService().rollback();
				} catch(SQLException e2) {
					// discard
				}
				throw e1;
			}
		}
	}
	
	public synchronized String migrate(String name, boolean up) throws SQLException {
		for(Migration migration : getMigrations()) {
			if(name.equals(migration.getClass().getSimpleName())) {
				getPersistService().setAutoCommit(false);
				migration.setService(getMigrationService());
				if(up) {
					migration.up();
					getPersistService().commit();
					return "ran " + name + ".up() successfully";
				} else {
					migration.down();
					getPersistService().commit();
					return "ran " + name + ".down() successfully";
				}
			}
		}
		return "migration \"" + name + "\" does not exist";
	}
	
	public synchronized String migratePurge() throws SQLException {
		getMigrationService().dropDatabase();
		return "database purged";
	}
	
	public synchronized String migrateRedo(int step) throws SQLException {
		List<? extends Migration> migrations = getMigrations();
		List<String> names = getNames(migrations);
		List<String> migrated = getMigrated();
		String current = getCurrentMigration();
		int cix = names.indexOf(current);
		if(cix == -1) {
			return "already at the beginning of the migrations";
		} else {
			if(step == -1) step = names.size();
			MigrationService mservice = getMigrationService();
			try {
				getPersistService().setAutoCommit(false);
				int ix = cix;
				for(int i = 0; i < step && ix >= 0; i++, ix--) {
					String name = names.get(ix);
					if(migrated.contains(name)) {
						migrations.get(ix).setService(mservice);
						migrations.get(ix).down();
					}
				}
				for(int i = ix + 1; i <= cix; i++) {
					String name = names.get(i);
					migrations.get(i).setService(mservice);
					migrations.get(i).up();
					if(!migrated.contains(name)) {
						setMigrated(name, true);
					}
				}
				getPersistService().commit();
				return "migrated successfully";
			} catch(SQLException e1) {
				try {
					getPersistService().rollback();
				} catch(SQLException e2) {
					// discard
				}
				throw e1;
			}
		}
	}
	
	public synchronized String migrateRollback(int step) throws SQLException {
		List<? extends Migration> migrations = getMigrations();
		List<String> names = getNames(migrations);
		List<String> migrated = getMigrated();
		String current = getCurrentMigration();
		int cix = names.indexOf(current);
		if(cix == -1) {
			return "already at the beginning of the migrations";
		} else {
			if(step == -1) step = names.size();
			MigrationService mservice = getMigrationService();
			try {
				getPersistService().setAutoCommit(false);
				int ix = cix;
				for(int i = 0; i < step && ix >= 0; i++, ix--) {
					String name = names.get(ix);
					if(migrated.contains(name)) {
						migrations.get(ix).setService(mservice);
						migrations.get(ix).down();
						setMigrated(name, false);
					}
				}
				setCurrentMigration((ix == -1) ? null : names.get(ix));
				getPersistService().commit();
				return "migrated successfully";
			} catch(SQLException e1) {
				try {
					getPersistService().rollback();
				} catch(SQLException e2) {
					// discard
				}
				throw e1;
			}
		}
	}
	
	protected void setCurrentMigration(String current) throws SQLException {
		PersistService ps = getPersistService();
		if(current == null) {
			try {
				ps.executeUpdate("DELETE FROM system_attrs WHERE name='migration.current'");
			} catch(SQLException e) {
				// last migration will remove the table
			}
		} else {
			int r = ps.executeUpdate("UPDATE system_attrs SET detail='" + current + "' where name='migration.current'");
			if(r == 0) {
				ps.executeUpdate("INSERT INTO system_attrs (name, detail, data) VALUES ('migration.current', '" + current + "', NULL)");
			}
		}
	}
	
	protected void setMigrated(String name, boolean migrated) throws SQLException {
		if(migrated) {
			getPersistService().executeUpdate("INSERT INTO system_attrs (name, detail, data) VALUES ('migrated', '" + name + "', NULL)");
		} else {
			try {
				getPersistService().executeUpdate("DELETE FROM system_attrs WHERE name='migrated' AND detail='" + name + "'");
			} catch(SQLException e) {
				// last migration will remove the table
			}
		}
	}

	@Override
	protected void setName(BundleContext context) throws Exception {
		Bundle migBundle = context.getBundle();
		migConfig = loadConfiguration(migBundle);
		migName = migBundle.getSymbolicName();
		Version version = migBundle.getVersion();
		version = new Version(version.getMajor(), version.getMinor(), version.getMicro());
		migVersion = version.toString();

		name = migName + "_" + migVersion;
		
		Bundle appBundle;
		ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
		if(ref == null) {
			throw new IllegalStateException("Package Admin service must be present to run a migration");
		} else {
			String symbolicName = migBundle.getSymbolicName();
			symbolicName = symbolicName.substring(0, symbolicName.lastIndexOf('.'));
			appBundle = getBundle(ref, symbolicName);
		}

		appConfig = loadConfiguration(appBundle);
		appName = appBundle.getSymbolicName();
		version = appBundle.getVersion();
		version = new Version(version.getMajor(), version.getMinor(), version.getMicro());
		appVersion = version.toString();
	}

	@Override
	protected void teardown() {
		instance = null;
	}
	
}
