package org.oobium.persist.migrate.mongo;

import org.oobium.logging.Logger;
import org.oobium.persist.PersistService;
import org.oobium.persist.migrate.AbstractMigrationService;
import org.oobium.persist.mongo.MongoPersistService;

public class MongoMigrationService extends AbstractMigrationService {

	private MongoPersistService persistor;
	
	public MongoMigrationService() {
		super();
	}
	
	public MongoMigrationService(String client, Logger logger) {
		super(client, logger);
	}

	@Override
	public void createDatastore() throws Exception {
		logger.info("Creating database... nothing to do.");
	}

	@Override
	public void dropDatastore() throws Exception {
		logger.info("Dropping database...");
		persistor.dropDatabase(client);
	}

	@Override
	public void setPersistService(PersistService service) {
		if(service instanceof MongoPersistService) {
			this.persistor = (MongoPersistService) service;
		} else {
			throw new IllegalStateException("Migration cannot run without a MongoPersistService: " + service);
		}
		
	}

}
