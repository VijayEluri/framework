package org.oobium.persist.migrate.db;

import org.oobium.logging.Logger;
import org.oobium.persist.migrate.Migration;
import org.oobium.persist.migrate.MigrationService;


public class CreateDatabase implements Migration {

	private Logger logger;
	private DbMigrationService service;
	
	@Override
	public void up() throws Exception {
		service.createDatastore();
	}

	@Override
	public void down() throws Exception {
		service.dropDatastore();
	}

	@Override
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void setService(MigrationService service) {
		if(service instanceof DbMigrationService) {
			this.service = (DbMigrationService) service;
		} else {
			logger.warn("service must be of type DbMigrationService, not " + ((service == null) ? "null" : service.getClass()));
			throw new IllegalArgumentException();
		}
	}

}
