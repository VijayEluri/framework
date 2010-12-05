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
package org.oobium.logging;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator {

	public static Activator instance;

	public static LogService getLogService() {
		return (LogService) instance.logTracker.getService();
	}

	public static boolean hasLogTracker() {
		return instance != null && instance.logTracker != null;
	}

	public static boolean isBundle(Bundle bundle) {
		return instance.bundle == bundle;
	}

	private Bundle bundle;
	private ServiceTracker logTracker;
	private ServiceTracker logReaderTracker;
	
	private List<LogReaderService> services;
	private LogListener logListener;

	public Activator() {
		instance = this;
	}

	private void addService(LogReaderService service) {
		if(services == null) {
			services = new ArrayList<LogReaderService>();
		}
		services.add(service);
	}
	
	private void removeService(LogReaderService service) {
		if(services != null) {
			services.remove(service);
			if(services.isEmpty()) {
				services = null;
			}
		}
	}
	
	@Override
	public void start(final BundleContext context) throws Exception {
		bundle = context.getBundle();

		logTracker = new ServiceTracker(context, LogService.class.getName(), null);
		logTracker.open();

		logListener = new LogHandler();
		
		logReaderTracker = new ServiceTracker(context, LogReaderService.class.getName(), new ServiceTrackerCustomizer() {
			@Override
			public Object addingService(ServiceReference arg0) {
				LogReaderService service = (LogReaderService) context.getService(arg0);
				addService(service);
				service.addLogListener(logListener);
				return service;
			}
			@Override
			public void modifiedService(ServiceReference arg0, Object arg1) {
				// nothing to do
			}
			@Override
			public void removedService(ServiceReference arg0, Object arg1) {
				LogReaderService service = (LogReaderService) arg1;
				service.removeLogListener(logListener);
				removeService(service);
			}
		});
		logReaderTracker.open();

		System.out.println("Oobium Logger started");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logTracker.close();
		logTracker = null;

		logReaderTracker.close();
		logReaderTracker = null;

		if(services != null) {
			for(LogReaderService service : services) {
				service.removeLogListener(logListener);
			}
			services.clear();
			services = null;
		}
		logListener = null;

		System.out.println("Oobium Logger stopped");
	}

}
