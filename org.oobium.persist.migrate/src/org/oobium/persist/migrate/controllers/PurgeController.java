package org.oobium.persist.migrate.controllers;

import java.sql.SQLException;

import org.oobium.app.controllers.Controller;
import org.oobium.persist.migrate.MigratorService;

public class PurgeController extends Controller {

	public void handleRequest() throws SQLException {
		if(hasParam("log")) {
			System.setProperty("org.oobium.persist.db.logging", param("log"));
		}
		try {
			MigratorService service = MigratorService.instance();
			String response;
			try {
				response = service.migratePurge();
				logger.info(response);
			} catch(SQLException e) {
				response = e.getLocalizedMessage();
				logger.warn(response);
			}
			render(response);
		} finally {
			System.clearProperty("org.oobium.persist.db.logging");
		}
	};
	
}