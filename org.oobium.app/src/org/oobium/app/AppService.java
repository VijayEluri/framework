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
package org.oobium.app;

import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.literal.Properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.oobium.app.handlers.HttpRequest404Handler;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.persist.PersistServices;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.AppRouter;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;
import org.oobium.app.routing.handlers.HttpHandler;
import org.oobium.app.server.HandlerTask;
import org.oobium.app.sessions.Session;
import org.oobium.app.views.View;
import org.oobium.app.workers.Worker;
import org.oobium.app.workers.Workers;
import org.oobium.cache.CacheService;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.PersistClient;
import org.oobium.persist.PersistService;
import org.oobium.persist.PersistServiceProvider;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class AppService extends ModuleService implements HttpRequestHandler, HttpRequest404Handler, HttpRequest500Handler, PersistClient {

	private static final ThreadLocal<AppService> appService = new ThreadLocal<AppService>();
	
	public static AppService get() {
		return appService.get();
	}
	
	public static PersistServiceProvider getPersistServices(Class<? extends AppService> appType) {
		AppService service = getActivator(appType);
		if(service != null) {
			return service.getPersistServices();
		}
		return null;
	}
	
	public static Router getRouter(Class<? extends AppService> appType) {
		AppService service = getActivator(appType);
		if(service != null) {
			return service.getRouter();
		}
		return null;
	}

	public static void set(AppService app) {
		appService.set(app);
	}


	private int port;

	private PersistServices persistServices;

	private ServiceTracker cacheTracker;
	private ServiceTracker moduleTracker;

	private ServiceRegistration request404HandlerRegistration;
	private ServiceRegistration request500HandlerRegistration;

	private Class<? extends View> errorClass404;
	private Class<? extends View> errorClass500;

	// TODO Workers should be in their own bundle and accessed as a service
	private Map<AppService, Workers> workersMap;
	
	public AppService() {
		super();
	}
	
	public AppService(Logger logger) {
		super(logger);
	}
	
	private ModuleService addModule(ServiceReference reference, Config config) {
		ModuleService module = (ModuleService) AppService.this.getContext().getService(reference);
		Config modConfig = config.getModuleConfig(module.getClass());
		try {
			logger.info("initializing " + module);

			Router modRouter = module.getRouter();
			module.addRoutes(modConfig, modRouter);
			getRouter().add(modRouter);

			module.loadModels(modConfig);
			module.loadObservers(modConfig);
			module.loadActionCaches(this, modConfig);
			
			if(module instanceof HttpRequest404Handler) {
				request404HandlerRegistration.unregister();
				request404HandlerRegistration = getContext().registerService(HttpRequest404Handler.class.getName(), module, Properties("port", getPort()));
				logger.info("set port " + getPort() + "'s 404 handler to " + module);
			}
			if(module instanceof HttpRequest500Handler) {
				request500HandlerRegistration.unregister();
				request500HandlerRegistration = getContext().registerService(HttpRequest500Handler.class.getName(), module, Properties("port", getPort()));
				logger.info("set port " + getPort() + "'s 500 handler to " + module);
			}
			
			logger.info(module + " initialized successfully");
			return module;
		} catch(Exception e1) {
			logger.error("Error adding routes for " + module + ": " + e1.getMessage(), e1);
			removeModule(reference, module, config);
			return null;
		}
	}

	/**
	 * Add this application's routes to the given router. Routes can be
	 * added and removed later as necessary.
	 * @param config the configuration
	 * @param router an {@link AppRouter} to which routes are to be added
	 * @see ModuleService#addRoutes(Config, Router)
	 */
	public void addRoutes(Config config, AppRouter router) {
		// subclasses to override if necessary
	}
	
	/**
	 * The application implementation of this method simply routes
	 * it to the {@link AppService}{@link #addRoutes(Config, AppRouter)} method.
	 */
	@Override
	public void addRoutes(Config config, Router router) {
		addRoutes(config, (AppRouter) router);
	}
	
	public final void startApp() throws Exception {
		logger.info("configuring in " + Mode.getSystemMode().name() + " mode");
		
		Config config = loadConfiguration();
		
		port = config.getPort();
		String[] hosts = config.getHosts();

		// allow subclasses to perform custom setup functions
		setup();
		
		router = new AppRouter(this, hosts, port);
		try {
			logger.info("initializing routes");
			addRoutes(config, router);
			logger.info("routes initialized successfully");
		} catch(Exception e) {
			if(logger.isLoggingDebug()) {
				logger.error("Error adding routes: " + e.getMessage(), e);
			} else {
				logger.error("Error adding routes: " + e.getMessage());
			}
		}

		loadModels(config);
		loadObservers(config);
		loadActionCaches(this, config);
		setErrorViewClasses(config);

		// register handlers
		if(context != null) {
			context.registerService(HttpRequestHandler.class.getName(), this, null);
			request404HandlerRegistration = context.registerService(HttpRequest404Handler.class.getName(), this, Properties("port", getPort()));
			request500HandlerRegistration = context.registerService(HttpRequest500Handler.class.getName(), this, Properties("port", getPort()));
		}
		
		// allow subclasses to register custom services
		registerServices(config);

		// initialize trackers
		initializeCacheTracker(config);
		initializePersistServices(config);
		initializeModulesTracker(config);
		
		// allow subclasses to initialize custom trackers
		initializeServiceTrackers(config);
	}
	
	public final void stopApp() throws Exception {
		if(cacheTracker != null) {
			cacheTracker.close();
			cacheTracker = null;
		}
		
		if(persistServices != null) {
			persistServices.close();
			persistServices = null;
		}
		
		if(moduleTracker != null) {
			moduleTracker.close();
			moduleTracker = null;
		}
		
		router.clear();
		router = null;
	}
	
	private Filter createModulesFilter(List<String> modules) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder();
		sb.append("(&(").append(Constants.OBJECTCLASS).append('=').append(ModuleService.class.getName()).append(')');
		if(modules.size() == 1) {
			sb.append("(name=").append(modules.get(0)).append(')');
		} else {
			sb.append("(|");
			for(String module : modules) {
				sb.append("(name=").append(module).append(')');
			}
			sb.append(')');
		}
		sb.append(')');
		
		Filter filter = context.createFilter(sb.toString());
		return filter;
	}

	public CacheService getCacheService() {
		return (cacheTracker != null) ? (CacheService) cacheTracker.getService() : null;
	}

	@Override
	public String getPersistClientName() {
		return getName();
	}

	/**
	 * Get the primary persist service from this application's PersistServices
	 * @return the primary persist service, or null if PersistServices is null
	 */
	public PersistService getPersistService() {
		if(persistServices != null) {
			return persistServices.getPrimary();
		}
		return null;
	}
	
	/**
	 * Get the persist service for the given class from this application's PersistServices
	 * @return the persist service, or null if PersistServices is null
	 */
	public PersistService getPersistService(Class<? extends Model> clazz) {
		if(persistServices != null) {
			return persistServices.getFor(clazz);
		}
		return null;
	}
	
	public PersistServiceProvider getPersistServices() {
		return persistServices;
	}
	
	@Override
	public int getPort() {
		return port;
	}

	public AppRouter getRouter() {
		return (AppRouter) router;
	}

	public Session getSession(int id, String uuid, boolean create) {
		Session session = null;
		if(id > 0 && uuid != null && !uuid.isEmpty()) {
			session = Session.retrieve(id, uuid);
		}
		if(session == null && create) {
			session = new Session(30*60);
		}
		return session;
	}
	
	@Override
	public Response handle404(Request request) {
		if(getRouter().hasHost(request.getHost())) {
			if(errorClass404 != null) {
				try{
					Response response = View.render(errorClass404, request);
					response.setStatus(HttpResponseStatus.NOT_FOUND);
					return response;
				} catch(Exception e) {
					logger.error(e);
				}
			}
		}
		return null;
	}

	@Override
	public Response handle500(Request request, Exception exception) {
		if(getRouter().hasHost(request.getHost())) {
			if(errorClass500 != null) {
				try{
					Response response = View.render(errorClass500, request);
					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
					return response;
				} catch(Exception e) {
					logger.error(e);
				}
			}
		}
		return null;
	}
	
	@Override
	public Object handleRequest(Request request) throws Exception {
		if(logger.isLoggingDebug()) {
			logger.debug("start handleRequest - " + getName() + ":" + request.getPath());
		}
		AppRouter router = getRouter();
		final RouteHandler handler = router.getHandler(request);
		if(handler != null) {
			handler.setLogger(logger);
			if(handler instanceof HttpHandler) {
				return new HandlerTask(request) {
					@Override
					protected Response handleRequest(Request request) throws Exception {
						appService.set(AppService.this);
						persistServices.openSession(getPersistClientName());
						Model.setLogger(logger);
						Model.setPersistServiceProvider(persistServices);
						try {
							Response response = handler.routeRequest(request);
							getRouter().applyHeaders(request, response);
							return response;
						} finally {
							persistServices.closeSession();
							Model.setPersistServiceProvider(null);
							Model.setLogger(null);
							appService.set(null);
						}
					}
				};
			} else {
				Response response = handler.routeRequest(request);
				router.applyHeaders(request, response);
				return response;
			}
		}
		logger.debug("end handleRequest");
		return null;
	}

	protected final void initializeCacheTracker(Config config) throws Exception {
		String cache = config.getString(Config.CACHE);
		if(!blank(cache)) {
			BundleContext context = getContext();
			if(context == null) {
				logger.info("no context - cacheTracker not started");
			} else {
				String str = "(&(" + Constants.OBJECTCLASS + "=" + CacheService.class.getName() + ")" +
							"(" + CacheService.TYPE + "=" + cache + "))";
				Filter filter = context.createFilter(str);
				cacheTracker = new ServiceTracker(context, filter, null);
				cacheTracker.open();
				logger.info("cacheTracker started {" + cache + "}");
			}
		} else {
			logger.info("cacheTracker not started");
		}
	}

	protected final void initializeModulesTracker(final Config config) throws Exception {
		List<String> modules = config.getModules();
		if(!modules.isEmpty()) {
			BundleContext context = getContext();
			if(context == null) {
				logger.info("no context - moduleTracker not started");
			} else {
				Filter filter = createModulesFilter(modules);
				moduleTracker = new ServiceTracker(context, filter, new ServiceTrackerCustomizer() {
					@Override
					public Object addingService(ServiceReference reference) {
						return addModule(reference, config);
					}
					@Override
					public void modifiedService(ServiceReference reference, Object service) {
						// do nothing
					}
					@Override
					public void removedService(ServiceReference reference, Object service) {
						if(service != null) {
							removeModule(reference, (ModuleService) service, config);
						}
						AppService.this.getContext().ungetService(reference);
					}
				});
				moduleTracker.open();
				logger.info("moduleTracker started {" + StringUtils.asString(modules) + "}");
			}
		}
	}

	private void registerPersistService(String service, Map<?, ?> options) throws Exception {
		if(logger.isLoggingDebug()) {
			logger.debug("registering for persist service: " + service);
		}
		Properties properties = new Properties();
		properties.setProperty(PersistService.SERVICE, service);
		properties.setProperty(PersistService.CLIENT, getPersistClientName());
		if(options != null && !options.isEmpty()) {
			properties.putAll(options);
		}
		getContext().registerService(PersistClient.class.getName(), this, properties);
	}

	private void registerPersistServices(Object persist) throws Exception {
		if(persist instanceof String) {
			registerPersistService((String) persist, null);
		} else if(persist instanceof List<?>) {
			for(Object o : (List<?>) persist) {
				registerPersistServices(o);
			}
		} else if(persist instanceof Map<?,?>) {
			Map<?,?> options = (Map<?,?>) persist;
			String service = (String) options.remove(PersistService.SERVICE);
			registerPersistService(service, options);
		}
	}
	
	protected final void initializePersistServices(Config config) throws Exception {
		if(persistServices != null) {
			logger.debug("skipping configured PersistServices - already set");
		} else {
			Object persist = config.get(Config.PERSIST);
			persistServices = new PersistServices(getContext(), persist);
			List<String> services = persistServices.getServiceNames();
			if(services.isEmpty()) {
				logger.debug("no presist services configured - skipping registration");
			} else {
				registerPersistServices(persist);
			}
		}
	}
	
	private void removeModule(ServiceReference reference, ModuleService module, Config config) {
		Config modConfig = config.getModuleConfig(module.getClass());
		try {
			logger.info("unloading " + module);
			
			Router modRouter = module.getRouter();
			if(modRouter != null) {
				getRouter().remove(modRouter);
				modRouter.clear();
			}
			
			module.unloadModels(modConfig);
			module.unloadObservers(modConfig);

			if(reference == request404HandlerRegistration.getReference()) {
				request404HandlerRegistration.unregister();
				request404HandlerRegistration = getContext().registerService(HttpRequest404Handler.class.getName(), this, Properties("port", getPort()));
				logger.info("set port " + getPort() + "'s 404 handler to " + AppService.this);
			}
			if(reference == request500HandlerRegistration.getReference()) {
				request500HandlerRegistration.unregister();
				request500HandlerRegistration = getContext().registerService(HttpRequest500Handler.class.getName(), this, Properties("port", getPort()));
				logger.info("set port " + getPort() + "'s 500 handler to " + AppService.this);
			}
			
			logger.info(module + " unloaded successfully");
		} catch(Exception e2) {
			logger.error("Error removing " + module + ": " + e2.getMessage(), e2);
		}
	}
	
	private void setErrorViewClasses(Config config) {
		BundleContext context = getContext();
		if(context == null) {
			return;
		}
		
		Bundle bundle = context.getBundle();

		String base;
		int ix = name.lastIndexOf('_');
		if(ix == -1) {
			base = name;
		} else {
			base = name.substring(0, ix);
		}
		base = config.getPathToViews(base).replace('/', '.') + ".pages.";

		try {
			Class<?> clazz = bundle.loadClass(base + "Error404");
			if(View.class.isAssignableFrom(clazz)) {
				errorClass404 = clazz.asSubclass(View.class);
			}
		} catch(ClassNotFoundException e) {
			// discard
		}

		try {
			Class<?> clazz = bundle.loadClass(base + "Error500");
			if(View.class.isAssignableFrom(clazz)) {
				errorClass500 = clazz.asSubclass(View.class);
			}
		} catch(ClassNotFoundException e) {
			// discard
		}
	}

	public void setPersistService(PersistService service) {
		if(persistServices == null) {
			persistServices = new PersistServices(service);
		} else {
			persistServices.set(service);
		}
	}

	public synchronized void submit(Worker worker) {
		if(workersMap == null) {
			workersMap = new HashMap<AppService, Workers>();
		}
		Workers workers = workersMap.get(this);
		if(workers == null) {
			workers = new Workers(this);
			workersMap.put(this, workers);
		}
		workers.submit(worker);
	}

	@Override
	public String toString() {
		return "Application " + getName();
	}

}
