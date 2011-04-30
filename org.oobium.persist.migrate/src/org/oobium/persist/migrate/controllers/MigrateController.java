package org.oobium.persist.migrate.controllers;

import java.sql.SQLException;

import org.oobium.app.controllers.Controller;
import org.oobium.persist.migrate.MigratorService;

public class MigrateController extends Controller {

	public void handleRequest() throws SQLException {
		String response;
		MigratorService service = MigratorService.instance();
		if(hasParam("dir")) {
			response = service.migrate(param("name"), "up".equals(param("dir")));
		} else {
			response = service.migrate(param("name"));
		}
		logger.info(response);
		render(response);
	};
	
}