package org.oobium.persist.migrate.controllers;

import org.oobium.app.controllers.HttpController;
import org.oobium.persist.migrate.MigratorService;

public class PurgeController extends HttpController {

	public void handleRequest() throws Exception {
		if(hasParam("log")) {
			System.setProperty("org.oobium.persist.db.logging", param("log"));
		}
		try {
			MigratorService service = MigratorService.instance();
			String response;
			try {
				response = service.migratePurge();
				logger.info(response);
			} catch(Exception e) {
				response = e.getLocalizedMessage();
				logger.warn(response);
			}
			render(response);
		} finally {
			System.clearProperty("org.oobium.persist.db.logging");
		}
	};
	
}