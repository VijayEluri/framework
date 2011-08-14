package org.oobium.persist.migrate.controllers;

import java.sql.SQLException;

import org.oobium.app.controllers.HttpController;
import org.oobium.persist.PersistException;
import org.oobium.persist.migrate.MigratorService;

public class RollbackController extends HttpController {

	public void handleRequest() throws PersistException {
		if(hasParam("log")) {
			System.setProperty("org.oobium.persist.db.logging", param("log"));
		}
		try {
			MigratorService service = MigratorService.instance();
			String response;
			try {
				response = service.migrateRollback("all".equals(param("step")) ? -1 : param("step", 1));
				logger.info(response);
			} catch(SQLException e) {
				response = e.getLocalizedMessage();
				logger.info(response);
			}
			render(response);
		} finally {
			System.clearProperty("org.oobium.persist.db.logging");
		}
	};
	
}