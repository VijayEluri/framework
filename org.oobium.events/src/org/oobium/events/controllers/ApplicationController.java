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
package org.oobium.events.controllers;

import java.util.Map;

import org.oobium.app.server.controller.Controller;
import org.oobium.http.HttpRequest;

public class ApplicationController extends Controller {

	public ApplicationController(HttpRequest request, Map<String, Object> routeParams) {
		super(request, routeParams);
	}

}
