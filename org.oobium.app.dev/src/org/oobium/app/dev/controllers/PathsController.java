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

import static org.jboss.netty.handler.codec.http.HttpMethod.DELETE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpMethod.PUT;
import static org.oobium.app.http.MimeType.CSS;
import static org.oobium.app.http.MimeType.HTML;
import static org.oobium.app.http.MimeType.JS;
import static org.oobium.utils.StringUtils.when;
import static org.oobium.utils.json.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.List;

import org.oobium.app.AppService;
import org.oobium.app.ModuleService;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.dev.AppDevActivator;
import org.oobium.app.http.MimeType;
import org.oobium.app.routing.Router;
import org.oobium.persist.PersistException;

public class PathsController extends HttpController {

	private List<String> filter(List<String> paths) {
		List<String> filtered = new ArrayList<String>();
		for(String path : paths) {
			int ix = path.lastIndexOf('.');
			if(ix != -1) {
				MimeType type = MimeType.getFromExtension(path.substring(ix + 1), HTML);
				if(type == CSS || type == HTML || type == JS) {
					filtered.add(path);
				}
			} else {
				filtered.add(path);
			}
		}
		return filtered;
	}
	
	@Override
	public void handleRequest() throws PersistException {
		ModuleService activator = AppDevActivator.getActivator(param("app"));
		if(activator instanceof AppService) {
			Router router = ((AppService) activator).getRouter();

			switch(when(param("method"), "get", "post", "put", "delete"))
			{
				case 0:  render(toJson(filter(router.getPaths(GET))));		break;
				case 1:  render(toJson(filter(router.getPaths(POST))));		break;
				case 2:  render(toJson(filter(router.getPaths(PUT))));		break;
				case 3:  render(toJson(filter(router.getPaths(DELETE))));	break;
				default: render(toJson(filter(router.getPaths(GET))));		break;
			}
		} else {
			render("[]");
		}
	}

}
