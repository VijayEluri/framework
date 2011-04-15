package org.oobium.persist.migrate.controllers;

import java.sql.SQLException;

import org.oobium.app.server.controller.Controller;
import org.oobium.persist.migrate.MigratorService;

public class RedoController extends Controller {

	public void handleRequest() throws SQLException {
		MigratorService service = MigratorService.instance();
		String response = service.migrateRedo("all".equals(param("step")) ? -1 : param("step", 1));
		logger.info(response);
		render(response);
	};
	
}