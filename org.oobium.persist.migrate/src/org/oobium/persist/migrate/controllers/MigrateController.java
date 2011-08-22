package org.oobium.persist.migrate.controllers;

import org.oobium.app.controllers.HttpController;
import org.oobium.persist.migrate.MigratorService;

public class MigrateController extends HttpController {

	public void handleRequest() throws Exception {
		if(hasParam("log")) {
			System.setProperty("org.oobium.persist.db.logging", param("log"));
		}
		try {
			String response;
			MigratorService service = MigratorService.instance();
			try {
				if(hasParam("dir")) {
					response = service.migrate(param("name"), "up".equals(param("dir")));
				} else {
					response = service.migrate(param("name"));
				}
				logger.info(response);
			} catch(Exception e) {
				response = e.getLocalizedMessage();
				logger.info(response);
			}
			render(response);
		} finally {
			System.clearProperty("org.oobium.persist.db.logging");
		}
	};
	
}