package org.oobium.persist.migrate.controllers;

import java.sql.SQLException;

import org.oobium.app.controllers.Controller;
import org.oobium.persist.migrate.MigratorService;

public class RollbackController extends Controller {

	public void handleRequest() throws SQLException {
		MigratorService service = MigratorService.instance();
		String response = service.migrateRollback("all".equals(param("step")) ? -1 : param("step", 1));
		logger.info(response);
		render(response);
	};
	
}