package org.oobium.persist.migrate.controllers;

import java.sql.SQLException;

import org.oobium.app.server.controller.Controller;
import org.oobium.persist.migrate.MigratorService;

public class MigrateController extends Controller {

	public void handleRequest() throws SQLException {
		MigratorService service = MigratorService.instance();
		if(hasParam("dir")) {
			render(service.migrate(param("name"), "up".equals(param("dir"))));
		} else {
			render(service.migrate(param("name")));
		}
	};
	
}