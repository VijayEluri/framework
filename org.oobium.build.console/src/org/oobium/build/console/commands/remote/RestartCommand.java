/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.build.console.commands.remote;

import static org.oobium.build.console.commands.RemoteCommand.getInstallations;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.IOException;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.exceptions.OobiumException;
import org.oobium.build.util.SSH;
import org.oobium.build.workspace.Application;
import org.oobium.console.ConsolePrintStream;
import org.oobium.utils.Config;

public class RestartCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}

	private void restart(Application app) throws OobiumException, IOException {
		Config config = Config.loadConfiguration(app.site);
		
		String host = config.getString("host");
		String path = config.getString("path");
		String username = config.getString("username");
		String password = config.getString("password");
		
		SSH ssh = new SSH(host, username, password);
		ssh.setOut(new ConsolePrintStream(console.out));
		ssh.setErr(new ConsolePrintStream(console.err));
		boolean sudo = coerce(config.get("sudo"), false);
		
		String name = config.getString("name", app.name);

		String remote = name;
		if(path != null) {
			remote = path + "/" + remote;
		}

		String[] installs = getInstallations(ssh, remote);
		if(installs != null) {
			ssh.setSudo(sudo);
			ssh.sudo("./stop.sh", installs[0]);
			ssh.sudo("nohup ./start.sh", installs[0]);
			ssh.setSudo(false);
			ssh.exec("ps aux | grep felix");
		}
	}

	@Override
	public void run() {
		Application app = getApplication();

		if(flag('v')) {
			console.capture();
		}
		
		try {
			restart(app);
		} catch(Exception e) {
			console.err.print(e);
		} finally {
			if(flag('v')) {
				console.release();
			}
		}
	}
	
}
