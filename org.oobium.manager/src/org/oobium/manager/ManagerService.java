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
package org.oobium.manager;

import static org.oobium.app.server.controller.Action.destroy;
import static org.oobium.app.server.controller.Action.show;
import static org.oobium.app.server.controller.Action.showAll;
import static org.oobium.app.server.controller.Action.update;
import static org.oobium.http.HttpRequest.Type.POST;

import org.oobium.app.AppService;
import org.oobium.app.persist.MemoryPersistService;
import org.oobium.app.server.routing.Router;
import org.oobium.manager.controllers.BundleController;
import org.oobium.manager.models.Bundle;
import org.oobium.utils.Config;
import org.osgi.framework.BundleContext;

public class ManagerService extends AppService {

	public static BundleContext context() {
		return getActivator(ManagerService.class).getContext();
	}

	
	public ManagerService() {
		setPersistService(new MemoryPersistService());
	}
	
	@Override
	public void addRoutes(Config config, Router router) {
		router.addRoutes(Bundle.class);
		
		router.addRoute("{models}/{name:[\\w\\.]+}_{version:[\\w\\d\\.-]+}", Bundle.class, destroy);
		router.addRoute("{models}/{name:[\\w\\.]+}", Bundle.class, destroy);
		router.addRoute("{models}", Bundle.class, destroy);

		router.addRoute("{models}/{name:[\\w\\.]+}_{version:[\\w\\d\\.-]+}", Bundle.class, update);
		router.addRoute("{models}/{name:[\\w\\.]+}", Bundle.class, update);
		router.addRoute("{models}", Bundle.class, update);

		router.addRoute("{models}/{name:[\\w\\.]+}_{version:[\\w\\d\\.-]+}", Bundle.class, show);

		router.addRoute("{models}/{name:[\\w\\.]+}", Bundle.class, showAll);
		
		router.add("refresh").asRoute(BundleController.class, POST);
	}

}
