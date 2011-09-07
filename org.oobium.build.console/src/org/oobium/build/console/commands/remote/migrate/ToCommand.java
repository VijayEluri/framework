package org.oobium.build.console.commands.remote.migrate;

import org.oobium.build.console.commands.remote.MigrateCommand;

public class ToCommand extends MigrateCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = 1;
		minParams = 1;
	}
	
	@Override
	protected String getAction() {
		return "migrate/to/" + param(0);
	}

}
