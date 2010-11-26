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

import org.oobium.app.server.controller.ActionCache;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.RouteHandler;
import org.oobium.app.server.routing.Router;
import org.oobium.app.server.routing.handlers.AssetHandler;
import org.oobium.app.server.routing.handlers.ViewHandler;
import org.oobium.app.server.view.View;
import org.oobium.app.workers.Worker;
import org.oobium.app.workers.Workers;
import org.oobium.cache.CacheService;
import org.oobium.http.HttpRequest;
import org.oobium.http.HttpRequest404Handler;
import org.oobium.http.HttpRequest500Handler;
import org.oobium.http.HttpRequestHandler;
import org.oobium.http.HttpResponse;
import org.oobium.http.HttpSession;
import org.oobium.http.HttpSessionService;
import org.oobium.http.constants.StatusCode;
import org.oobium.persist.Model;
import org.oobium.persist.PersistClient;
import org.oobium.persist.PersistService;
import org.oobium.persist.PersistServices;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.StringUtils;
import org.oobium.utils.json.JsonUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public abstract class AppService extends ModuleService implements HttpRequestHandler, HttpRequest404Handler, HttpRequest500Handler, HttpSessionService, PersistClient {

	private static final ThreadLocal<AppService> appService = new ThreadLocal<AppService>();
	
	public static AppService get() {
		return appService.get();
	}
	
	public static void set(AppService app) {
		appService.set(app);
	}
	
	public static PersistServices getPersistServices(Class<? extends AppService> appType) {
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


	private Router router;
	private int port;

	private PersistServices persistServices;

	private ServiceTracker cacheTracker;
	private ServiceTracker sessionTracker;
	private ServiceTracker moduleTracker;

	private ServiceRegistration request404HandlerRegistration;
	private ServiceRegistration request500HandlerRegistration;

	private Class<? extends View> errorClass404;
	private Class<? extends View> errorClass500;

	// TODO Workers should be in their own bundle and accessed as a service
	private Map<AppService, Workers> workersMap;
	
	@Override
	public void addRoutes(Config config, Router router) {
		// subclasses to implement if necessary
	}
	
	/**
	 * Load the configuration for this bundle.  The default implementation fetches
	 * the configuration using getClass().  Subclasses should override to implements
	 * alternative behavior.
	 * @return the configuration
	 */
	protected Config loadConfiguration() {
		return Config.loadConfiguration(getClass());
	}

	/**
	 * Load the configuration for the given bundle.  The default implementation using
	 * the bundle's Activator (as found in the MANIFEST) to load the configuration.
	 * @param bundle the bundle for which to get the configuration
	 * @return the configuration
	 * @throws ClassNotFoundException if the given bundle does have an Activator
	 */
	protected Config loadConfiguration(Bundle bundle) throws ClassNotFoundException {
		String name = (String) bundle.getHeaders().get("Bundle-Activator");
		Class<?> clazz = bundle.loadClass(name);
		return Config.loadConfiguration(clazz);
	}
	
	@Override
	protected void doStart(BundleContext context) throws Exception {
		logger.info("configuring in " + Mode.getSystemMode().name() + " mode");
		
		Config config = loadConfiguration();
		
		port = config.getPort();
		String[] hosts = config.getHosts();

		router = new Router(logger, hosts, port);
		router.addControllerNames(getClass().getClassLoader(), getControllerPaths(config));
		try {
			logger.info("initializing routes");
			addRoutes(config, router);
			logger.info("routes initialized successfully");
		} catch(Exception e) {
			logger.error("Error adding routes: " + e.getMessage(), e);
			stop(context);
			throw e;
		}

		loadModels(config);
		loadObservers(config);
		setErrorViewClasses(config);

		// register handlers
		context.registerService(HttpRequestHandler.class.getName(), this, null);
		request404HandlerRegistration = context.registerService(HttpRequest404Handler.class.getName(), this, Properties("port", getPort()));
		request500HandlerRegistration = context.registerService(HttpRequest500Handler.class.getName(), this, Properties("port", getPort()));
		
		// allow subclasses to register custom services
		registerServices(config);

		// initialize trackers
		initializeCacheTracker(config);
		initializeSessionTracker(config);
		initializePersistServices(config);
		initializeModulesTracker(config);
		
		// allow subclasses to initialize custom trackers
		initializeServiceTrackers(config);
	}
	
	@Override
	protected void doStop(BundleContext context) throws Exception {
		if(cacheTracker != null) {
			cacheTracker.close();
			cacheTracker = null;
		}
		
		if(persistServices != null) {
			persistServices.close();
			persistServices = null;
		}
		
		if(sessionTracker != null) {
			sessionTracker.close();
			sessionTracker = null;
		}
		
		if(moduleTracker != null) {
			moduleTracker.close();
			moduleTracker = null;
		}
		
		router.clear();
		router = null;
	}
	
	public CacheService getCacheService() {
		return (cacheTracker != null) ? (CacheService) cacheTracker.getService() : null;
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

	public PersistServices getPersistServices() {
		return persistServices;
	}
	
	@Override
	public int getPort() {
		return port;
	}
	
	public Router getRouter() {
		return router;
	}
	
	@Override
	public HttpSession getSession(int id, String uuid, boolean create) {
		HttpSessionService service = (sessionTracker != null) ? (HttpSessionService) sessionTracker.getService() : null;
		if(service != null) {
			return service.getSession(id, uuid, create);
		}
		return null;
	}

	@Override
	public HttpResponse handle404(HttpRequest request) {
		if(errorClass404 != null) {
			try{
				Response response = View.render(errorClass404, request);
				response.setStatus(StatusCode.NOT_FOUND);
				return response;
			} catch(Exception e) {
				logger.error(e);
			}
		}
		return Response.notFound(request.getType(), request.getContentTypes());
	}

	@Override
	public HttpResponse handle500(HttpRequest request, Exception exception) {
		if(errorClass500 != null) {
			try{
				Response response = View.render(errorClass500, request);
				response.setStatus(StatusCode.SERVER_ERROR);
				return response;
			} catch(Exception e) {
				logger.error(e);
			}
		}
		return Response.serverError(request.getType(), request.getContentTypes());
	}
	
	@Override
	public HttpResponse handleRequest(HttpRequest request) throws Exception {
		if(logger.isLoggingDebug()) {
			logger.debug("start handleRequest - " + getName() + ":" + request.getPath());
		}
		HttpResponse response = null;
		RouteHandler handler = router.getHandler(request);
		if(handler != null) {
			handler.setLogger(logger);
			if(handler instanceof AssetHandler || handler instanceof ViewHandler) {
				response = handler.routeRequest(request);
			} else {
				appService.set(this);
				persistServices.openSession(getName());
				Model.setLogger(logger);
				Model.setPersistServices(persistServices);
				try {
					response = handler.routeRequest(request);
				} finally {
					persistServices.closeSession();
					Model.setPersistServices(null);
					Model.setLogger(null);
					appService.set(null);
				}
			}
			router.applyHeaders(request, response);
		}
		logger.debug("end handleRequest");
		return response;
	}
	
	protected final void initializeCacheTracker(Config config) throws Exception {
		String cache = config.getString(Config.CACHE);
		if(!blank(cache)) {
			BundleContext context = getContext();
			
			loadActionCaches();

			String str = "(&(" + Constants.OBJECTCLASS + "=" + CacheService.class.getName() + ")" +
						"(" + CacheService.TYPE + "=" + cache + "))";
			Filter filter = context.createFilter(str);
			cacheTracker = new ServiceTracker(context, filter, null);
			cacheTracker.open();
			logger.info("cacheTracker started {" + cache + "}");
		} else {
			logger.info("cacheTracker not started");
		}
	}
	
	protected final void initializeModulesTracker(final Config config) throws Exception {
		Object modules = config.get(Config.MODULES);
		if(!blank(modules)) {
			StringBuilder sb = new StringBuilder();
			sb.append("(&(").append(Constants.OBJECTCLASS).append('=').append(ModuleService.class.getName()).append(')');
			if(modules instanceof String) {
				sb.append("(name=").append(modules).append(')');
			} else if(modules instanceof Map<?, ?>) {
				Object module = ((Map<?, ?>) modules).keySet().iterator().next();
				if(module instanceof String) {
					sb.append("(name=").append(module).append(')');
				} else {
					throw new IllegalArgumentException("Configuration option 'modules' must contain a value for 'module'");
				}
			} else if(modules instanceof Iterable<?>) {
				sb.append("(|");
				for(Object object : (Iterable<?>) modules) {
					if(!blank(object)) {
						if(object instanceof String) {
							sb.append("(name=").append(object).append(')');
						} else if(object instanceof Map<?, ?>) {
							Object module = ((Map<?, ?>) object).keySet().iterator().next();
							if(module instanceof String) {
								sb.append("(name=").append(module).append(')');
							} else {
								throw new IllegalArgumentException("Configuration option 'modules' must contain a value for 'module'");
							}
						} else {
							throw new IllegalArgumentException("Configuration option 'modules' must be either a String or an Array, not a " + object.getClass());
						}
					}
				}
				sb.append(')');
			} else {
				throw new IllegalArgumentException("Configuration option 'modules' must be either a String or an Array, not a " + modules.getClass());
			}
			sb.append(')');
			
			BundleContext context = getContext();

			Filter filter = context.createFilter(sb.toString());
			moduleTracker = new ServiceTracker(context, filter, new ServiceTrackerCustomizer() {
				@Override
				public Object addingService(ServiceReference reference) {
					ModuleService module = (ModuleService) AppService.this.getContext().getService(reference);
					Config modConfig = config.getModuleConfig(module.getClass());
					try {
						logger.info("initializing " + module);
						router.addControllerNames(module.getClass().getClassLoader(), module.getControllerPaths(modConfig));
						module.addRoutes(modConfig, router);
						module.loadModels(modConfig);
						module.loadObservers(modConfig);
						
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
						try {
							module.removeRoutes(modConfig, router);
							router.removeControllerNames(module.getControllerPaths(modConfig));
							module.unloadModels(modConfig);
							module.unloadObservers(modConfig);
						} catch(Exception e2) {
							logger.error("Error removing routes for " + module + ": " + e2.getMessage(), e2);
						}
						return null;
					}
				}
				@Override
				public void modifiedService(ServiceReference reference, Object service) {
					// do nothing
				}
				@Override
				public void removedService(ServiceReference reference, Object service) {
					if(service != null) {
						ModuleService module = (ModuleService) service;
						Config modConfig = config.getModuleConfig(module.getClass());
						try {
							logger.info("unloading " + module);
							module.removeRoutes(modConfig, router);
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
							logger.error("Error removing routes for " + module + ": " + e2.getMessage(), e2);
						}
					}
					AppService.this.getContext().ungetService(reference);
				}
			});
			moduleTracker.open();
			logger.info("moduleTracker started {" + StringUtils.asString(modules) + "}");
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
				if(logger.isLoggingDebug()) {
					if(services.size() == 1) {
						logger.debug("registering for persist service: " + services.get(0));
					} else {
						logger.debug("registering for persist services: " + StringUtils.asString(services));
					}
				}
				Properties properties = new Properties();
				properties.setProperty(PersistService.CLIENT, getName());
				properties.setProperty(PersistService.SERVICE, JsonUtils.toJson(services));
				getContext().registerService(PersistClient.class.getName(), this, properties);
			}
		}
	}

	protected final void initializeSessionTracker(Config config) throws Exception {
		String session = config.getString(Config.SESSION);
		if(!blank(session)) {
			BundleContext context = getContext();
			String str = "(&(" + Constants.OBJECTCLASS + "=" + HttpSessionService.class.getName() + ")" +
							"(" + HttpSessionService.TYPE + "=" + session + "))";
			Filter filter = context.createFilter(str);
			sessionTracker = new ServiceTracker(context, filter, null);
			sessionTracker.open();
			logger.info("sessionTracker started {" + session + "}");
		} else {
			logger.info("sessionTracker not started");
		}
	}
	
	private void loadActionCaches() throws Exception {
		logger.info("initializing ActionCache classes");

		String pkg = getClass().getPackage().getName() + ".controllers.caches";
		List<Class<?>> classes = loadClassesInPackage(pkg);
		
		for(ActionCache cache : ActionCache.getCaches(classes)) {
			cache.setHandler(this);
		}
	}

	/**
	 * Top level Applications usually do not need to remove their routes when
	 * being stopped - override this method if this is not a top-level application
	 * or there are special circumstances.
	 * <p>This default implementation does nothing.</p>
	 */
	@Override
	public void removeRoutes(Config config, Router router) {
		// subclasses to implement if necessary
	}

	private void setErrorViewClasses(Config config) {
		Bundle bundle = getContext().getBundle();

		String app = config.getString("app");
		String base;
		if(app == null) {
			base = getClass().getPackage().getName() + ".views.pages.";
		} else {
			base = getClass().getPackage().getName() + "." + app + ".views.pages.";
		}

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
