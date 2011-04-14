package org.oobium.persist.migrate.db;

import java.sql.SQLException;

import org.oobium.persist.migrate.Migration;
import org.oobium.persist.migrate.MigrationService;


public class CreateDatabase implements Migration {

	private DbMigrationService service;
	
	@Override
	public void up() throws SQLException {
		service.createDatabase();
	}

	@Override
	public void down() throws SQLException {
		service.dropDatabase();
	}

	@Override
	public void setService(MigrationService service) {
		if(service instanceof DbMigrationService) {
			this.service = (DbMigrationService) service;
		} else {
			throw new IllegalArgumentException();
		}
	}

}
