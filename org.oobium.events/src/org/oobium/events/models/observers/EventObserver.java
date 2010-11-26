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
package org.oobium.events.models.observers;

import static org.oobium.client.Client.client;
import static org.oobium.utils.literal.Map;
import static org.oobium.utils.literal.e;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.oobium.client.ClientCallback;
import org.oobium.client.ClientResponse;
import org.oobium.events.models.Event;
import org.oobium.events.models.Listener;
import org.oobium.persist.Model;
import org.oobium.persist.Observer;
import org.oobium.persist.PersistService;
import org.oobium.utils.json.JsonUtils;

public class EventObserver extends Observer implements ClientCallback {

	static { addObserver(EventObserver.class, Event.class); }

	@Override
	protected void afterCreate(Model model) {
		Event event = (Event) model;

		System.out.println("afterCreate event: " + event.asString());
		
		try {
			// notify local listeners
			Listener.handleEvent(event);

			// notify remote listeners
			PersistService persistor = event.getPersistor();
			String host = event.getHost();
			String service = event.getService();
			String name = event.getEventName();
			List<Listener> listeners = persistor.findAll(Listener.class, "where host=? and service=? and eventName=?", host, service, name);
			for(Listener listener : listeners) {
				try {
					Map<String, String> map = Map(e("event", JsonUtils.toJson(event.getAll(), "data")), e("listener", String.valueOf(listener.getId())));
					client(listener.getCallback()).aPost(map, this);
				} catch(MalformedURLException e) {
					logger.warn(e);
				}
				if(listener.isOneShot()) {
					listener.setPersistor(persistor);
					listener.destroy();
				}
			}
		} catch(SQLException e) {
			logger.error(e);
		}
	}

	@Override
	public void handleCallback(ClientResponse response) {
		if(!response.isSuccess()) {
			if(response.exceptionThrown()) {
				logger.info("listener notification failed", response.getException());
			} else {
				logger.info("listener notification failed: " + response.getBody());
			}
		}
	}

}
