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

import static org.oobium.client.Client.client;
import static org.oobium.http.constants.Action.create;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.literal.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.app.AppService;
import org.oobium.client.ClientResponse;
import org.oobium.events.models.ListenerModel;
import org.oobium.persist.Attribute;
import org.oobium.persist.Indexes;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.PersistService;
import org.oobium.persist.Validate;

@ModelDescription(
	attrs = {
		@Attribute(name="host", type=String.class),		// the host to listen to for events (domain & port)
		@Attribute(name="service", type=String.class),	// the service to listen to for events
		@Attribute(name="eventName", type=String.class),// the name of the event to listen for
		@Attribute(name="callback", type=String.class),	// the URL to notify when an event occurs (send events here)
		@Attribute(name="remoteUrl", type=String.class),// if this is a local listener: the URL for listeners on the remote server
		@Attribute(name="remoteId", type=int.class),	// if this is a local listener: the id of the remote listener, if it has been created
		@Attribute(name="oneShot", type=boolean.class)	// if true, automatically delete this listener after the first event has occurred
	},
	validations = {
		@Validate(field="host,service,eventName,callback", isNotBlank=true)
	},
	timestamps = true,
	allowUpdate = false
)
@Indexes("{Unique-host,service,eventName,callback}")
public class Listener extends ListenerModel {

	private static Map<PersistService, Map<String, List<Listener>>> localListeners = new HashMap<PersistService, Map<String,List<Listener>>>();
	
	public static Listener create(String url, String service, String eventName, Class<? extends AppService> clazz, EventHandler handler) {
		return create(url, service, eventName, clazz, handler, false);
	}

	private static Listener create(String url, String service, String eventName, Class<? extends AppService> appClass, EventHandler handler, boolean oneShot) {
		AppService app = AppService.getActivator(appClass);
		String callback = app.getRouter().urlTo(Event.class, create);

		Listener listener = new Listener();
		listener.setPersistor(app.getPersistService(Listener.class));
		listener.setService(service);
		listener.setEventName(eventName);
		listener.setCallback(callback);
		listener.setRemoteUrl(url);
		listener.setHandler(handler);
		listener.setOneShot(oneShot);
		listener.save();
		return listener;
	}
	
	public static Listener createOneShot(String url, String service, String eventName, Class<? extends AppService> clazz, EventHandler handler) {
		return create(url, service, eventName, clazz, handler, true);
	}

	public static void handleEvent(Event event) {
		synchronized(localListeners) {
			Map<String, List<Listener>> listenersMap = localListeners.get(event.getPersistor());
			if(listenersMap != null) {
				String key = event.getHost() + ":" + event.getService() + ":" + event.getEventName();
				List<Listener> listeners = listenersMap.get(key);
				if(listeners != null) {
					PersistService persistor = event.getPersistor();
					for(Listener listener : listeners.toArray(new Listener[listeners.size()])) {
						if(listener.hasHandler()) {
							listener.getHandler().handleEvent(event);
						}
						if(listener.isOneShot()) {
							listener.setPersistor(persistor);
							listener.destroy();
						}
					}
				}
			}
		}
	}
	
	
	private EventHandler handler;

	@Override
	public boolean destroy() {
		if(hasRemoteUrl() || hasHandler()) {
			synchronized(localListeners) {
				Map<String, List<Listener>> listenersMap = localListeners.get(getPersistor());
				if(listenersMap != null) {
					String key = getHost() + ":" + getService() + ":" + getEventName();
					List<Listener> listeners = listenersMap.get(key);
					if(listeners != null) {
						if(!listeners.remove(this)) {
							addError("Listener does not exist");
						}
					}
				}
			}
			
			if(hasRemoteUrl()) {
				try {
					client(getRemoteUrl() + "/" + getRemoteId()).aDelete();
				} catch(MalformedURLException e) {
					addError(e.getLocalizedMessage());
				}
			}
			return !hasErrors();
		} else {
			return super.destroy();
		}
	}
	
	@Override
	protected void validateUpdate() {
		addError("Listeners cannot be updated");
	}
	
	public EventHandler getHandler() {
		return handler;
	}
	
	public boolean hasHandler() {
		return handler != null;
	}
	
	@Override
	public boolean save() {
		if(hasRemoteUrl() || hasHandler()) {
			if(!canSave()) {
				return false;
			}
			if(hasHandler()) {
				synchronized(localListeners) {
					Map<String, List<Listener>> listenersMap = localListeners.get(getPersistor());
					if(listenersMap == null) {
						listenersMap = new HashMap<String, List<Listener>>();
						localListeners.put(getPersistor(), listenersMap);
					}
					String key = getHost() + ":" + getService() + ":" + getEventName();
					List<Listener> listeners = listenersMap.get(key);
					if(listeners == null) {
						listeners = new ArrayList<Listener>();
						listenersMap.put(key, listeners);
					}
					listeners.add(this);
				}
			}
			if(hasRemoteUrl()) {
				try {
					Map<String, String> map = Map(
							e("listener["+SERVICE+"]",	getService()),
							e("listener["+EVENT_NAME+"]",	getEventName()),
							e("listener["+CALLBACK+"]",	getCallback())
					);
					ClientResponse response = client(getRemoteUrl()).post(map);
					if(response.isSuccess()) {
						try {
							setRemoteId(Integer.parseInt(response.getHeader("id")));
						} catch(Exception e) {
							addError("could not parse remote id: " + response.getHeader("id"));
						}
					} else {
						if(response.hasBody()) {
							addError(response.getBody());
						} else if(response.exceptionThrown()) {
							addError(response.getException().getLocalizedMessage());
						} else {
							addError("unknown error");
						}
						return false;
					}
				} catch(MalformedURLException e) {
					addError(e.getLocalizedMessage());
					return false;
				}
			}
			return true;
		} else {
			return super.save();
		}
	}
	
	@Override
	public Listener setCallback(String callback) {
		if(!callback.startsWith("http://") && !callback.startsWith("https://")) {
			callback = "http://" + callback;
		}
		return super.setCallback(callback);
	}
	
	public void setHandler(EventHandler handler) {
		this.handler = handler;
	}

	@Override
	public Listener setRemoteUrl(String url) {
		if(blank(url)) {
			setHost(null);
		} else {
			int ix = 0;
			if(url.startsWith("http://")) {
				ix = 7;
			} else if(url.startsWith("https://")) {
				ix = 8;
			}
			if(ix == 0) {
				url = "http://" + url;;
				ix = 7;
			}
			String[] sa = url.substring(ix).split("/", 2);
			setHost(sa[0]);
		}
		return super.setRemoteUrl(url);
	}
	
}
