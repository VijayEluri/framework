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

import java.util.Iterator;
import java.util.List;

import org.oobium.app.http.Action;
import org.oobium.events.models.Listener;

public class ListenerController extends ApplicationController {

	@Override
	public void beforeFilter(Action action) {
//		TODO security
	}

	@Override // POST/URL/[models]
	public void create() throws Exception {
		Listener listener = param("listener", Listener.class);
		listener.setHost(request.getHost() + ":" + request.getPort());
		if(listener.save()) {
			if(logger.isLoggingDebug()) {
				logger.debug("created: " + listener.asString());
			}
			renderCreated(listener);
		} else {
			renderErrors(listener);
		}
	}

	@Override // DELETE/URL/[models]/id
	public void destroy() throws Exception {
		Listener listener = Listener.newInstance(getId(int.class));
		if(listener.destroy()) {
			renderDestroyed(listener);
		} else {
			renderErrors(listener);
		}
	}

	@Override // GET/URL/[models]/id
	public void show() throws Exception {
		Listener listener = Listener.find(getId(int.class));
		if(listener != null) {
			render(listener);
		}
	}

	@Override // GET/URL/[models]
	public void showAll() throws Exception {
		List<Listener> listeners = Listener.findAll();
		if(!listeners.isEmpty()) {
			String service = param("service", String.class);
			if(service != null) {
				for(Iterator<Listener> iter = listeners.iterator(); iter.hasNext(); ) {
					if(!service.equals(iter.next().getService())) {
						iter.remove();
					}
				}
			}
		}
		render(listeners);
	}

	@Override // PUT/URL/[models]/id
	public void update() throws Exception {
		Listener listener = param("listener", Listener.class);
		if(listener.save()) {
			renderOK();
		} else {
			renderErrors(listener);
		}
	}

}
