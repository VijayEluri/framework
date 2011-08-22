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
package org.oobium.app.dev.controllers;

import org.oobium.app.AppService;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.dev.AppDevActivator;
import org.oobium.events.models.Event;
import org.oobium.manager.ManagerService;
import org.oobium.utils.json.JsonUtils;

public class NotifyController extends HttpController {

	@Override
	public void handleRequest() throws Exception {
		AppService app = AppService.getActivator(ManagerService.class);
		Event.create(app, AppDevActivator.ID, "openType", JsonUtils.toJson(params()));
		renderOK();
	}
	
}
