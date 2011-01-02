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
package org.oobium.events;

import static org.oobium.app.server.controller.Action.showAll;

import org.oobium.app.ModuleService;
import org.oobium.app.server.routing.Router;
import org.oobium.events.models.Event;
import org.oobium.events.models.Listener;
import org.oobium.utils.Config;

public class Activator extends ModuleService {

	@Override
	public void addRoutes(Config config, Router router) {
		router.addResources(Event.class);
		router.addResource("{models}/{event:[\\w_]+}", Event.class, showAll);
		
		router.addResources(Listener.class);
		router.addResource("{models}/{service:[\\w\\._]+}", Listener.class, showAll);
	}

}
