package org.oobium.persist.migrate.db;

import org.oobium.persist.PersistService;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.migrate.MigratorService;
import org.oobium.utils.Config;

public abstract class DbMigratorService extends MigratorService {

	@Override
	public DbPersistService getPersistService() {
		PersistService service = super.getPersistService();
		if(service instanceof DbPersistService) {
			return (DbPersistService) service;
		}
		throw new IllegalStateException("Migration cannot run without a DbPersistService: " + appConfig.get(Config.PERSIST));
	}

}
