package org.oobium.persist.migrate.db;

import java.sql.SQLException;

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
		throw new IllegalStateException("Database Migration cannot run without a DbPersistService: " + appConfig.get(Config.PERSIST));
	}
	
	@Override
	protected void setAutoCommit(boolean autoCommit) throws SQLException {
		getPersistService().setAutoCommit(autoCommit);
	}

	@Override
	protected void commit() throws SQLException {
		getPersistService().commit();
	}

	@Override
	protected void rollback() throws SQLException {
		getPersistService().rollback();
	}

}
