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

import java.util.List;

import org.oobium.app.http.Action;
import org.oobium.events.models.Event;

public class EventController extends ApplicationController {

	@Override
	public void beforeFilter(Action action) {
//		TODO security
	}

	@Override // POST/URL/[models]
	public void create() throws Exception {
		Event event = param("event", Event.class);
		if(event.save()) {
			renderCreated(event);
		} else {
			renderErrors(event);
		}
	}

	@Override // DELETE/URL/[models]/id
	public void destroy() throws Exception {
		Event event = Event.newInstance(getId(int.class));
		if(event.destroy()) {
			renderDestroyed(event);
		} else {
			renderErrors(event);
		}
	}

	@Override // GET/URL/[models]/id
	public void show() throws Exception {
		Event event = Event.find(getId(int.class));
		if(event != null) {
			render(event);
		}
	}

	@Override // GET/URL/[models]
	public void showAll() throws Exception {
		List<Event> events = Event.findAll();
		render(events);
	}

	@Override // PUT/URL/[models]/id
	public void update() throws Exception {
		Event event = param("event", Event.class);
		if(event.save()) {
			renderOK();
		} else {
			renderErrors(event);
		}
	}
	
}
