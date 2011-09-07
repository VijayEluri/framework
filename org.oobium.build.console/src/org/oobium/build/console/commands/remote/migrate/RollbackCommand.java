package org.oobium.build.console.commands.remote.migrate;

import org.oobium.build.console.commands.remote.MigrateCommand;

public class RollbackCommand extends MigrateCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = 1;
	}
	
	@Override
	protected String getAction() {
		if(hasParam("step")) {
			if("all".equals(param("step"))) {
				return "migrate/rollback/all";
			} else {
				return "migrate/rollback/" + param("step", int.class);
			}
		} else {
			return "migrate/rollback";
		}
	}
	
}
