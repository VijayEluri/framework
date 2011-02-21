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
package org.oobium.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import org.oobium.http.HttpRequest404Handler;
import org.oobium.http.HttpRequest500Handler;
import org.oobium.http.HttpRequestHandler;
import org.oobium.http.HttpServer;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Server implements BundleActivator, HttpServer {

	// TODO maxOpenSockets
	// public static final int maxOpenSockets;
	public static final long maxReadTime;
	public static final long maxReadIdleTime;
	public static final int maxRequestHandlers;

	static {
		long tmp;

		try {
			tmp = Long.parseLong(System.getProperty("org.oobium.server.maxReadTime", "300000")); // five minutes
		} catch(Exception e) {
			tmp = 300000;
		}
		maxReadTime = tmp;

		try {
			tmp = Long.parseLong(System.getProperty("org.oobium.server.maxReadIdleTime", "10000")); // 10 seconds
		} catch(Exception e) {
			tmp = 10000;
		}
		maxReadIdleTime = tmp;

		try {
			tmp = Long.parseLong(System.getProperty("org.oobium.server.maxThreads", "10"));
		} catch(Exception e) {
			tmp = 10;
		}
		maxRequestHandlers = (int) tmp;
	}


	private Logger logger;
	private ServerSelector selector;
	private ServiceTracker requestHandlerTracker;
	private ServiceTracker request404HandlerTracker;
	private ServiceTracker request500HandlerTracker;

	public Server() {
		logger = LogProvider.getLogger(Server.class);
	}
	
	synchronized HttpRequestHandler addHandler(HttpRequestHandler handler) {
		if(selector == null) {
			selector = new ServerSelector();
			new Thread(selector).start();
		}
		return selector.addHandler(handler);
	}
	
	public int[] getPorts() {
		return selector.getPorts();
	}

	public void start(final BundleContext context) throws Exception {
		logger.setTag(context.getBundle().getSymbolicName());
		logger.info("Starting server");
		
		selector = new ServerSelector();
		new Thread(selector).start();

		requestHandlerTracker = new ServiceTracker(context, HttpRequestHandler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				return addHandler((HttpRequestHandler) context.getService(reference));
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				selector.removeHandler((HttpRequestHandler) service);
			}
		});
		requestHandlerTracker.open();
		
		request404HandlerTracker = new ServiceTracker(context, HttpRequest404Handler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				int port = coerce(reference.getProperty("port"), -1);
				return selector.addHandler((HttpRequest404Handler) context.getService(reference), port);
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				int port = coerce(reference.getProperty("port"), -1);
				selector.removeHandler((HttpRequest404Handler) service, port);
			}
		});
		request404HandlerTracker.open();
		
		request500HandlerTracker = new ServiceTracker(context, HttpRequest500Handler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				int port = coerce(reference.getProperty("port"), -1);
				return selector.addHandler((HttpRequest500Handler) context.getService(reference), port);
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				int port = coerce(reference.getProperty("port"), -1);
				selector.removeHandler((HttpRequest500Handler) service, port);
			}
		});
		request500HandlerTracker.open();
		
		logger.info("Server started");
	}

	public void stop(BundleContext context) throws Exception {
		logger.info("Stopping server");
		requestHandlerTracker.close();
		requestHandlerTracker = null;
		request404HandlerTracker.close();
		request404HandlerTracker = null;
		request500HandlerTracker.close();
		request500HandlerTracker = null;
		selector.close();
		selector = null;
		logger.info("Server stopped");
		logger.setTag(null);
		logger = null;
	}

}
