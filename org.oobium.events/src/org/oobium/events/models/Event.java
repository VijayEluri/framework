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
package org.oobium.events.models;

import static org.oobium.utils.StringUtils.blank;

import org.oobium.app.AppService;
import org.oobium.app.routing.AppRouter;
import org.oobium.persist.Attribute;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Validate;

@ModelDescription(
	attrs = {
		@Attribute(name="host", type=String.class),		// the host that triggered the event
		@Attribute(name="service", type=String.class),	// the service that triggered the event
		@Attribute(name="eventName", type=String.class),// the name of the event
		@Attribute(name="data", type=String.class)		// any data required to describe the event
	},
	validations = {
		@Validate(field="host,service,eventName,data", isNotBlank=true)
	},
	timestamps = true,
	allowDelete = false,
	allowUpdate = false
)
public class Event extends EventModel {
	
	public static Event create(AppService app, String eventName) {
		return create(app.getClass(), null, eventName, null);
	}
	
	public static Event create(Class<? extends AppService> appClass, String eventName) {
		return create(appClass, null, eventName, null);
	}
	
	public static Event create(AppService app, String eventName, String data) {
		return create(app.getClass(), null, eventName, data);
	}
	
	public static Event create(Class<? extends AppService> appClass, String eventName, String data) {
		return create(appClass, null, eventName, data);
	}

	public static Event create(AppService app, String service, String eventName, String data) {
		return create(app.getClass(), service, eventName, data);
	}
	
	public static Event create(Class<? extends AppService> appClass, String service, String eventName, String data) {
		AppService app = AppService.getActivator(appClass);
		if(app == null) {
			throw new IllegalStateException("failed to create event - no app: " + appClass);
		}
		
		AppRouter router = app.getRouter();
		
		Event event = new Event();
		event.setPersistor(app.getPersistService(Event.class));
		event.setHost(router.getHost() + ":" + router.getPort());
		if(blank(service)) {
			event.setService(app.getSymbolicName());
		} else {
			event.setService(service);
		}
		event.setEventName(eventName);
		if(blank(data)) {
			event.setData("{}");
		} else {
			event.setData(data);
		}
		event.save();
		return event;
	}
	
	
	@Override
	protected void validateUpdate() {
		addError("Events cannot be updated");
	}
	
}
