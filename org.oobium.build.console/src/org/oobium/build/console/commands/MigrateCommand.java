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

import static org.oobium.client.Client.client;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.commands.migrate.PurgeCommand;
import org.oobium.build.console.commands.migrate.RedoCommand;
import org.oobium.build.console.commands.migrate.RollbackCommand;
import org.oobium.build.console.commands.migrate.ToCommand;
import org.oobium.build.runner.RunnerService;
import org.oobium.client.ClientResponse;

public class MigrateCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = 2;
		
		add(new ToCommand());
		add(new RollbackCommand());
		add(new RedoCommand());
		add(new PurgeCommand());
	}

	@Override
	protected boolean canExecute() {
		return hasApplication() && (
				(paramCount() == 0) ||
				((paramCount() == 2) && ("up".equals(param(1)) || "down".equals(param(1))))
			);
	}
	
	protected String getPath() {
		if(paramCount() == 2) {
			return "/migrate/" + param(0) + "/" + param(1);
		}
		return "/migrate";
	}
	
	@Override
	public void run() {
		try {
			RunnerService.pauseUpdaters();

			console.out.println("running migrator...");
			
			ClientResponse response = client("localhost", 5001).post(getPath());
			if(response.isSuccess()) {
				console.out.println(response.getBody());
			} else {
				if(response.exceptionThrown()) {
					console.err.print(response.getException().getLocalizedMessage());
				} else {
					console.err.println("migrator completed with errors");
				}
			}
		} finally {
			RunnerService.unpauseUpdaters();
		}
	}
	
}
