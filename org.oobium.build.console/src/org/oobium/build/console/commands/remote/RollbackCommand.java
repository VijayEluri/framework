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

import org.oobium.build.exceptions.OobiumException;
import org.oobium.build.util.SSH;
import org.oobium.build.workspace.Application;
import org.oobium.console.ConsolePrintStream;
import org.oobium.utils.Config.Mode;

public class RollbackCommand extends RemoteCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}

	private void rollback(Application app) throws OobiumException, IOException {
		Mode mode = hasParam("mode") ? Mode.parse(param("mode")) : Mode.PROD;

		if(mode == Mode.PROD) {
			String r = flag('f') ? "Y" : ask("Mode is set to PROD!!! Continue anyway? [Y/N] ");
			if(!"Y".equalsIgnoreCase(r)) {
				console.out.println("operation cancelled");
				return;
			}
		}
		
		RemoteConfig config = getRemoteConfig(app, mode);
		if(config == null) {
			return;
		}
		
		SSH ssh = new SSH(config.host, config.username, config.password);
		ssh.setOut(new ConsolePrintStream(console.out));
		ssh.setErr(new ConsolePrintStream(console.err));
		
		String name = app.name;

		String remote = name;
		if(config.dir != null) {
			remote = config.dir + "/" + remote;
		}

		String[] installs = getInstallations(ssh, remote);
		if(installs != null) {
			int to;
			if("all".equalsIgnoreCase(param("step"))) {
				to = installs.length - 1;
			} else {
				to = coerce(param("step")).from(1);
				if(to > installs.length - 1) {
					to = installs.length - 1;
				}
			}
			
			ssh.setSudo(config.sudo);
			ssh.sudo("./stop.sh", installs[0]);
			ssh.sudo("nohup ./start.sh", installs[to]);
			ssh.setSudo(false);
			ssh.exec("ps aux | grep felix");

			ssh.setSudo(config.sudo);
			for(int i = 0; i < to; i++) {
				ssh.sudo("rm -r " + installs[i]);
			}
		}
	}

	@Override
	public void run() {
		Application app = getApplication();

		if(flag('v')) {
			console.capture();
		}
		
		try {
			rollback(app);
		} catch(Exception e) {
			console.err.print(e);
		} finally {
			if(flag('v')) {
				console.release();
			}
		}
	}
	
}
