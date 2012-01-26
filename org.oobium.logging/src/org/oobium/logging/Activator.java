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
import java.util.Arrays;
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
		if(instance != null && instance.loggerServices != null) {
			return instance.loggerServices[0];
		}
		return null;
	}

	public static boolean hasLogTracker() {
		return instance != null && instance.loggerServices != null;
	}

	public static boolean isBundle(Bundle bundle) {
		return instance.bundle == bundle;
	}

	private Bundle bundle;
	private ServiceTracker logTracker;
	private ServiceTracker logReaderTracker;
	
	private LogService[] loggerServices;
	private List<LogReaderService> readerServices;
	private LogListener logListener;

	public Activator() {
		instance = this;
	}

	private void addService(LogReaderService service) {
		if(readerServices == null) {
			readerServices = new ArrayList<LogReaderService>();
		}
		readerServices.add(service);
	}
	
	private void removeService(LogReaderService service) {
		if(readerServices != null) {
			readerServices.remove(service);
			if(readerServices.isEmpty()) {
				readerServices = null;
			}
		}
	}
	
	private void addService(LogService service) {
		if(loggerServices == null) {
			loggerServices = new LogService[1];
			loggerServices[0] = service;
		} else {
			loggerServices = Arrays.copyOf(loggerServices, loggerServices.length + 1);
			loggerServices[loggerServices.length - 1] = service;
		}
	}
	
	private void removeService(LogService service) {
		if(loggerServices != null) {
			if(loggerServices.length == 1) {
				loggerServices = null;
			} else {
				LogService[] tmp = loggerServices;
				loggerServices = Arrays.copyOf(loggerServices, loggerServices.length - 1);
				for(int i = 0; i < loggerServices.length; i++) {
					if(loggerServices[i] != service) {
						tmp[i] = loggerServices[i];
					}
				}
				loggerServices = tmp;
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void start(final BundleContext context) throws Exception {
		bundle = context.getBundle();

		logTracker = new ServiceTracker(context, LogService.class.getName(), new ServiceTrackerCustomizer() {
			@Override
			public Object addingService(ServiceReference reference) {
				LogService service = (LogService) context.getService(reference);
				addService(service);
				return service;
			}
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			@Override
			public void removedService(ServiceReference reference, Object service) {
				LogService loggerService = (LogService) service;
				removeService(loggerService);
			}
		});
		logTracker.open();

		logListener = new LogHandler();
		
		logReaderTracker = new ServiceTracker(context, LogReaderService.class.getName(), new ServiceTrackerCustomizer() {
			@Override
			public Object addingService(ServiceReference reference) {
				LogReaderService service = (LogReaderService) context.getService(reference);
				addService(service);
				service.addLogListener(logListener);
				return service;
			}
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			@Override
			public void removedService(ServiceReference reference, Object service) {
				LogReaderService readerService = (LogReaderService) service;
				readerService.removeLogListener(logListener);
				removeService(readerService);
			}
		});
		logReaderTracker.open();

		System.out.println("Oobium Logger started");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logTracker.close();
		logTracker = null;

		if(loggerServices != null) {
			readerServices = null;
		}

		logReaderTracker.close();
		logReaderTracker = null;

		if(readerServices != null) {
			for(LogReaderService service : readerServices) {
				service.removeLogListener(logListener);
			}
			readerServices.clear();
			readerServices = null;
		}
		logListener = null;

		System.out.println("Oobium Logger stopped");
	}

}
