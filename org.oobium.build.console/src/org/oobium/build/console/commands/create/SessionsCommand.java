package org.oobium.build.console.commands.create;

public class SessionsCommand extends MigrationCommand {

	@Override
	public void configure() {
		maxParams = 0;
		minParams = 0;
	}
	
	@Override
	protected String getMigrationName() {
		return "CreateSessions";
	}
	
}
