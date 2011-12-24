package org.oobium.app;

import java.util.HashMap;
import java.util.Map;

import org.oobium.app.handlers.HttpRequest404Handler;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.handlers.RequestHandler;
import org.oobium.app.server.Server;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@SuppressWarnings("rawtypes")
public class AppServer implements BundleActivator {

	private Logger logger;
	private ServiceTracker requestHandlerTracker;
	private ServiceTracker request404HandlerTracker;
	private ServiceTracker request500HandlerTracker;
	private Map<String, Server> servers;
	
	public AppServer() {
		this(LogProvider.getLogger(AppServer.class));
	}
	
	public AppServer(Logger logger) {
		this.logger = logger;
		this.servers = new HashMap<String, Server>();
	}

	private void closeTrackers() {
		if(requestHandlerTracker != null) {
			requestHandlerTracker.close();
			requestHandlerTracker = null;
		}
		if(request404HandlerTracker != null) {
			request404HandlerTracker.close();
			request404HandlerTracker = null;
		}
		if(request500HandlerTracker != null) {
			request500HandlerTracker.close();
			request500HandlerTracker = null;
		}
	}

	private void disposeServers() {
		if(servers != null) {
			for(Server server : servers.values()) {
				server.dispose();
			}
		}
	}

	private Server getServer(String name) {
		Server server = servers.get(name);
		if(server == null) {
			servers.put(name, server = new Server(logger));
		}
		return server;
	}
	
	private Object addHandler(RequestHandler handler) {
		String name = handler.getServerConfig().name();
		getServer(name).addHandler(handler);
		return handler;
	}
	
	private void removeHandler(RequestHandler handler) {
		String name = handler.getServerConfig().name();
		Server server = servers.get(name);
		if(server != null) {
			server.removeHandler(handler);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void openTrackers(final BundleContext context) {
		requestHandlerTracker = new ServiceTracker(context, RequestHandler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				return addHandler((RequestHandler) context.getService(reference));
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				removeHandler((RequestHandler) service);
			}
		});
		requestHandlerTracker.open();
		
		request404HandlerTracker = new ServiceTracker(context, HttpRequest404Handler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				String name = String.valueOf(reference.getProperty("name"));
				return getServer(name).add404Handler((HttpRequest404Handler) context.getService(reference));
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				String name = String.valueOf(reference.getProperty("name"));
				getServer(name).remove404Handler((HttpRequest404Handler) service);
			}
		});
		request404HandlerTracker.open();
		
		request500HandlerTracker = new ServiceTracker(context, HttpRequest500Handler.class.getName(), new ServiceTrackerCustomizer() {
			public Object addingService(ServiceReference reference) {
				String name = String.valueOf(reference.getProperty("name"));
				return getServer(name).add500Handler((HttpRequest500Handler) context.getService(reference));
			}
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do
			}
			public void removedService(ServiceReference reference, Object service) {
				String name = String.valueOf(reference.getProperty("name"));
				getServer(name).remove500Handler((HttpRequest500Handler) service);
			}
		});
		request500HandlerTracker.open();
	}
	
	public void start(BundleContext context) throws Exception {
		logger.setTag(context.getBundle().getSymbolicName());
		logger.info("Starting server");

		openTrackers(context);
		
		logger.info("Server started");
	}

	public void stop(BundleContext context) throws Exception {
		logger.info("Stopping server");
		closeTrackers();
		disposeServers();
		logger.info("Server stopped");
		logger.setTag(null);
		logger = null;
	}

}
