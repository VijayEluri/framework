package org.oobium.build.console.commands.remote;

import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.workspace.Application;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;

public class RemoteCommand extends BuilderCommand {

	public String ask(String field, boolean password) throws IllegalStateException {
		String msg = "  " + field + ": ";
		String s = super.ask(msg, password);
		checkQuit(s);
		if(blank(s)) {
			console.err.println(field + " cannot be blank - exiting");
			throw new IllegalStateException();
		}
		return s;
	}
	
	private void checkQuit(String s) {
		if("q".equals(s)) {
			console.out.println("exiting");
			throw new IllegalStateException();
		}
	}
	
	protected RemoteConfig getRemoteConfig(Application app, Mode mode) {
		RemoteConfig remoteConfig = new RemoteConfig();
		
		if(app.site != null && app.site.isFile()) {
			Config config = Config.loadConfiguration(app.site, mode);
			remoteConfig.host = config.getString("host");
			remoteConfig.dir = config.getString("path");
			remoteConfig.username = config.getString("username");
			remoteConfig.password = config.getString("password");
			remoteConfig.sudo = coerce(config.get("sudo"), false);
			boolean valid = true;
			if(blank(remoteConfig.host)) {
				console.err.println("site.js is missing the \"host\" property");
				valid = false;
			}
			if(blank(remoteConfig.username)) {
				console.err.println("site.js is missing the \"username\" property");
				valid = false;
			}
			if(blank(remoteConfig.password)) {
				console.err.println("site.js is missing the \"password\" property");
				valid = false;
			}
			if(!valid) {
				console.err.println("exiting");
				return null;
			}
		} else {
			console.out.println("No site.js file found.\nInput the following fields (press 'q' to quit):");
			try {
				String s = ask("host", false);
				remoteConfig.host = s;
				
				s = ask("  base dir [~]: ");
				checkQuit(s);
				remoteConfig.dir = s;
	
				s = ask("username", false);
				remoteConfig.username = s;
	
				s = ask("password", true);
				remoteConfig.password = s;
	
				s = ask("  use sudo? [false]: ");
				checkQuit(s);
				remoteConfig.sudo = coerce(s, false);
			} catch(IllegalStateException e) {
				return null;
			}
		}
		
		return remoteConfig;
	}
	
}
