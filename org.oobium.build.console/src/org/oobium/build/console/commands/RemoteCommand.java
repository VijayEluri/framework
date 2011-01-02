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
package org.oobium.build.console.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.commands.remote.DeployCommand;
import org.oobium.build.console.commands.remote.RedeployCommand;
import org.oobium.build.console.commands.remote.RestartCommand;
import org.oobium.build.console.commands.remote.RollbackCommand;
import org.oobium.build.exceptions.OobiumException;
import org.oobium.build.util.SSH;


public class RemoteCommand extends BuilderCommand {

	public static String[] getInstallations(SSH ssh, String remote) throws IOException, OobiumException {
		String response = ssh.exec("ls -d " + remote + "_*/");
		if(response.contains("No such file or directory")) {
			return null;
		}
		
		String[] installs = response.split("\\s+");
		if(installs.length == 0) {
			return null;
		}

		Arrays.sort(installs, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
		});
		
		return installs;
	}


	@Override
	public void configure() {
		add(new DeployCommand());
		add(new RedeployCommand());
		add(new RestartCommand());
		add(new RollbackCommand());
	}
	
}
