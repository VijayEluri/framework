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

import static org.oobium.utils.literal.*;
import static org.oobium.utils.StringUtils.*;

import org.oobium.app.AppService;
import org.oobium.app.controllers.HttpController;
import org.oobium.manager.ManagerService;

public class EventController extends HttpController {

	public enum EventType { OpenType }
	
	@Override
	public void handleRequest() throws Exception {
		switch(EventType.valueOf(camelCase(param("eventType")))) {
		case OpenType:
			ManagerService manager = AppService.getActivator(ManagerService.class);
			manager.send("open", Map(e("type", param("type")), e("line", param("line", int.class))), null);
			renderOK();
			break;
		}
	}
	
}
