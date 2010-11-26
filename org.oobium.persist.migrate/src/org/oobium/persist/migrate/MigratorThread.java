///*******************************************************************************
// * Copyright (c) 2010 Oobium, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
// ******************************************************************************/
//package org.oobium.persist.migrate;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//
//import org.oobium.events.models.Event;
//import org.oobium.logging.Logger;
//import org.oobium.manager.ManagerService;
//import org.oobium.persist.Model;
//import org.oobium.persist.PersistServices;
//
//class MigratorThread extends Thread {
//
//	protected final Logger logger;
//	private final MigratorService migrator;
//	
//	public MigratorThread(MigratorService migrator) {
//		logger = Logger.getLogger(migrator.getClass());
//		this.migrator = migrator;
//	}
//
//	@Override
//	public void run() {
//		Throwable throwable = null;
//		
//		PersistServices persistServices = migrator.getPersistServices();
//		try {
//			persistServices.openSession(migrator.getName());
//			Model.setLogger(logger);
//			Model.setPersistServices(persistServices);
//
//			String migrateTo = migrator.getMigrateTo();
//			List<String> migrated = migrator.getMigrated();
//			if(migrated == null) {
//				MigrationService migrationService = migrator.getMigrationService();
//				migrator.startMigrations();
//				for(Migration migration : migrator.getMigrations()) {
//					String name = migration.getClass().getName();
//					migrator.startMigration(name);
//					migration.setService(migrationService);
//					migration.up();
//					migrator.endMigration(name);
//					if(name.equals(migrateTo)) {
//						break;
//					}
//				}
//				migrator.endMigrations();
//			} else {
//				LinkedHashMap<String, Migration> migrations = new LinkedHashMap<String, Migration>();
//				for(Migration migration : migrator.getMigrations()) {
//					migrations.put(migration.getClass().getName(), migration);
//				}
//			}
//			
//			if(to == -1) {
//				to = migrations.size() - 1;
//			}
//			
//			if(from != to) {
//				if(from < to) {
//					for(int i = from; i <= to; i++) {
//						migrator.startMigration(i);
//						migrations.get(i).up();
//						migrator.endMigration(i);
//					}
//				}
//				if(to < from) {
//					for(int i = from; i >= to; i--) {
//						migrator.startMigration(i);
//						migrations.get(i).down();
//						migrator.endMigration(i);
//					}
//				}
//				migrator.endMigrations();
//			}
//		} catch(Throwable t) {
//			throwable = t;
//			logger.error(t);
//		}
//		
//		persistServices.closeSession();
//		Model.setPersistServices(null);
//		Model.setLogger(null);
//		logger.info("migration complete.");
//
//		if(throwable == null) {
//			Event.create(ManagerService.class, "migrated", "status:success");
//		} else {
//			Event.create(ManagerService.class, "migrated", "status:failed, message:" + throwable.getLocalizedMessage());
//		}
//	}
//	
//}
