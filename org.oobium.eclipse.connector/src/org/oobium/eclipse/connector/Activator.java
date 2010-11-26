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
package org.oobium.eclipse.connector;

import static org.oobium.http.HttpRequest.Type.*;

import org.oobium.app.AppService;
import org.oobium.app.server.routing.Router;
import org.oobium.eclipse.connector.controllers.CommandController;
import org.oobium.utils.Config;

public class Activator extends AppService {

	@Override
	public void addRoutes(Config config, Router router) {
		router.add("commands").asRoute("commands/{command:.+}", CommandController.class, POST);
	}

}
