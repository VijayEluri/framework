package org.oobium.build.console.commands.remote.migrate;

import org.oobium.build.console.commands.remote.MigrateCommand;

public class RedoCommand extends MigrateCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}
	
	@Override
	protected String getAction() {
		if(hasParam("step")) {
			if("all".equals(param("step"))) {
				return "migrate/redo/all";
			} else {
				return "migrate/redo/" + param("step", int.class);
			}
		} else {
			return "migrate/redo";
		}
	}
	
}
