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
package org.oobium.app.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.oobium.persist.Model;
import org.oobium.persist.NullPersistService;
import org.oobium.persist.PersistService;
import org.oobium.persist.PersistServiceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class PersistServices implements PersistServiceProvider {

	private final ThreadLocal<String> threadSessionName = new ThreadLocal<String>();
	private final ThreadLocal<List<PersistService>> threadOpenServices = new ThreadLocal<List<PersistService>>();

	/**
	 * a list of the names of services being tracked
	 */
	private List<String> serviceNames;
	
	/**
	 * The primary service or service tracker.<br>
	 * This is either the actual service, or a tracker for the service...<br>
	 * may be null
	 */
	private Object primaryService;
	
	/**
	 * Secondary services or service trackers, mapped by the name of the class that wants to use them:<br>
	 * class name -> (service or tracker)<br>
	 * may be null
	 */
	private Map<String, Object> classServices; // class name -> persistor
	
	
	
	public PersistServices() {
		this(null, null);
	}
	
	// obj can be:
	//	null (use the NullPersistorService)
	//	String (a single persistor)
	//	List (Strings and Maps - 1st String is the default, Maps are secondaries that are mapped to classes)
	//	Map (mapped to classes; no default means use the NullPersistService for any classes not in the Map)
	//		(Maps -> { service:"persistor", models: [ "Model1", "Model2", "etc." ] }
	//		(not mapped to classes: the map is the default)
	public PersistServices(BundleContext context, Object obj) throws IllegalArgumentException {
		if(obj == null) {
			primaryService = new NullPersistService();
			addService(primaryService.getClass().getName());
		} else if(obj instanceof String) {
			init(context, (String) obj);
		} else if(obj instanceof List<?>) {
			init(context, (List<?>) obj);
		} else if(obj instanceof Map<?,?>) {
			init(context, (Map<?,?>) obj);
		}
	}
	
	public PersistServices(PersistService service) {
		this.primaryService = service;
	}

	private void addService(String service) {
		if(serviceNames == null) {
			serviceNames = new  ArrayList<String>();
		}
		serviceNames.add(service);
	}
	
	public void close() {
		if(primaryService instanceof ServiceTracker) {
			((ServiceTracker) primaryService).close();
		}
		if(classServices != null) {
			for(Object obj : classServices.values()) {
				if(obj instanceof ServiceTracker) {
					((ServiceTracker) obj).close();
				}
			}
		}
	}
	
	@Override
	public void closeSession() {
		closeSessions();
		this.threadSessionName.set(null);
	}

	private synchronized void closeSessions() {
		List<PersistService> openServices = threadOpenServices.get();
		if(openServices != null) {
			for(PersistService service : openServices) {
				service.closeSession();
			}
			openServices.clear();
			threadOpenServices.set(null);
		}
	}
	
	@Override
	public PersistService getFor(Class<? extends Model> clazz) {
		if(classServices != null && clazz != null) {
			Object o = classServices.get(clazz.getName());
			if(o != null) {
				return getService(o);
			}
		}
		return getService(primaryService);
	}
	
	@Override
	public PersistService getFor(String className) {
		if(classServices != null && className != null && className.length() > 0) {
			Object o = classServices.get(className);
			if(o != null) {
				return getService(o);
			}
		}
		return getService(primaryService);
	}
	
	public List<PersistService> getOpenServices() {
		return threadOpenServices.get();
	}
	
	@Override
	public PersistService getPrimary() {
		return getService(primaryService);
	}

	private PersistService getService(Object o) {
		if(o instanceof PersistService) {
			PersistService ps = (PersistService) o;
			openSession(ps);
			return ps;
		} else if(o instanceof ServiceTracker) {
			PersistService ps = (PersistService) ((ServiceTracker) o).getService();
			if(ps != null) {
				openSession(ps);
				return ps;
			}
		}
		// falls through to here if service tracker returns a null, or if "o" is null or something weird :)
		return new NullPersistService(threadSessionName.get()); // TODO create new every time, or main a static instance?
	}

	public List<String> getServiceNames() {
		if(serviceNames == null) {
			return new ArrayList<String>(0);
		} else {
			return new ArrayList<String>(serviceNames);
		}
	}

	public List<PersistService> getServices() {
		List<PersistService> services = new ArrayList<PersistService>();
		services.add(getService(primaryService));
		if(classServices != null) {
			for(Object o : classServices.values()) {
				services.add(getService(o));
			}
		}
		return services;
	}
	
	private void init(BundleContext context, List<?> list) {
		for(Object obj : list) {
			if(obj instanceof String) {
				init(context, (String) obj);
			} else if(obj instanceof Map<?,?>) {
				init(context, (Map<?,?>) obj);
			} else {
				throw new IllegalArgumentException(obj + " can only be a String or a Map");
			}
		}
	}
	
	private void init(BundleContext context, Map<?, ?> map) {
		Properties properties = new Properties();
		for(Entry<?, ?> entry : map.entrySet()) {
			properties.put(entry.getKey(), entry.getValue());
		}
		String serviceName = properties.getProperty(PersistService.SERVICE);
		
		String str = "(&(" + Constants.OBJECTCLASS + "=" + PersistService.class.getName() + ")" +
						"(" + PersistService.SERVICE + "=" + serviceName + "))";
		Filter filter;
		try {
			filter = context.createFilter(str);
		} catch(InvalidSyntaxException e) {
			throw new IllegalArgumentException("invalid syntax for the filter: " + str, e);
		}

		ServiceTracker tracker = new ServiceTracker(context, filter, null);
		
		Object classes = properties.get(PersistService.MODELS);
		if(classes == null) {
			primaryService = tracker;
		} else {
			if(classes instanceof String) {
				put((String) classes, tracker);
			} else if(classes instanceof List<?>) {
				for(Object o : (List<?>) classes) {
					put(String.valueOf(o), tracker);
				}
			} else {
				throw new IllegalArgumentException(classes + " is not a valid \"" + PersistService.MODELS + "\" property value");
			}
		}

		addService(serviceName);
		tracker.open();
	}
	
	private void init(BundleContext context, String serviceName) {
		Properties properties = new Properties();
		properties.put(PersistService.SERVICE, serviceName);

		String str = "(&(" + Constants.OBJECTCLASS + "=" + PersistService.class.getName() + ")" +
						"(" + PersistService.SERVICE + "=" + serviceName + "))";
		Filter filter;
		try {
			filter = context.createFilter(str);
		} catch(InvalidSyntaxException e) {
			throw new IllegalArgumentException("invalid syntax for the filter: " + str, e);
		}
		
		ServiceTracker tracker = new ServiceTracker(context, filter, null);
		primaryService = tracker;
		
		addService(serviceName);
		tracker.open();
	}

	private synchronized void openSession(PersistService ps) {
		String sessionName = threadSessionName.get();
		if(sessionName != null && !ps.isSessionOpen()) {
			List<PersistService> openServices = threadOpenServices.get();
			if(openServices == null) {
				openServices = new ArrayList<PersistService>();
				ps.openSession(sessionName);
				openServices.add(ps);
				threadOpenServices.set(openServices);
			} else if(!openServices.contains(ps)) {
				openServices.add(ps);
				ps.openSession(sessionName);
			}
		}
	}
	
	@Override
	public void openSession(String name) {
		closeSessions();
		threadSessionName.set(name);
	}

	@Override
	public Object put(Class<? extends Model> clazz, PersistService service) {
		return put(clazz.getName(), service);
	}
	
	private Object put(String className, Object service) {
		if(classServices == null) {
			classServices = new HashMap<String, Object>();
		}
		return classServices.put(className, service);
	}

	@Override
	public Object remove(Class<? extends Model> clazz) {
		if(classServices != null) {
			Object old = classServices.remove(clazz);
			if(classServices.isEmpty()) {
				classServices = null;
			}
			return old;
		}
		return null;
	}
	
	@Override
	public Object set(PersistService service) {
		Object old = this.primaryService;
		this.primaryService = service;
		return old;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("default: ").append(primaryService);
		if(classServices != null) {
			sb.append('\n');
			sb.append("mapped:\n");
			for(Entry<String, Object> entry : classServices.entrySet()) {
				sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
			}
		}
		return sb.toString();
	}
	
}
