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
package org.oobium.build.console.commands.migrate;

import org.oobium.build.console.commands.MigrateCommand;

public class RollbackCommand extends MigrateCommand {

	@Override
	public void configure() {
		applicationRequired = true;
		maxParams = 1;
	}
	
	@Override
	protected String getPath() {
		if(hasParam("step")) {
			if("all".equals(param("step"))) {
				return "/migrate/rollback/all";
			} else {
				return "/migrate/rollback/" + param("step", int.class);
			}
		} else {
			return "/migrate/rollback";
		}
	}
	
}
