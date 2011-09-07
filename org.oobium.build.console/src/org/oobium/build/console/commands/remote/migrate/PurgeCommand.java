package org.oobium.build.console.commands.remote.migrate;

import org.oobium.build.console.commands.remote.MigrateCommand;

public class PurgeCommand extends MigrateCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}
	
	@Override
	protected String getAction() {
		return "migrate/purge";
	}
	
}
