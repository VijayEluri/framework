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

import java.io.IOException;

import org.oobium.build.exceptions.OobiumException;
import org.oobium.build.util.SSH;

public class RedeployCommand extends DeployCommand {

	@Override
	protected void finish(SSH ssh, String[] previous) throws IOException, OobiumException {
		if(previous != null) {
			console.out.print("Removing obsolete installation (" + previous[0] + ")...");
			ssh.exec("rm -r " + previous[0]);
			console.out.println(" done.");
		}
		console.out.println("Redeployment Complete.");
	}
	
}
