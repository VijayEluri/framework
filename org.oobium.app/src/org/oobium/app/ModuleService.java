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

import static org.oobium.utils.literal.Dictionary;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.oobium.app.controllers.ControllerCache;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.routing.Router;
import org.oobium.app.sessions.ISession;
import org.oobium.app.views.View;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.Observer;
import org.oobium.utils.Config;
import org.oobium.utils.StringUtils;
import org.oobium.utils.json.JsonUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public abstract class ModuleService implements BundleActivator {

	private static final Map<Class<? extends ModuleService>, ModuleService> activatorsByClass = new HashMap<Class<? extends ModuleService>, ModuleService>();
	private static final Map<String, ModuleService> activatorsByName = new HashMap<String, ModuleService>();

	public static <T extends ModuleService> T getActivator(Class<T> type) {
		synchronized(activatorsByClass) {
			return type.cast(activatorsByClass.get(type));
		}
	}

	/**
	 * @param name Bundle Name and Bundle Version (ie "bundle_1.0.0")
	 * @return
	 */
	public static ModuleService getActivator(String name) {
		synchronized(activatorsByName) {
			return activatorsByName.get(name);
		}
	}

	public static ModuleService[] getActivators() {
		synchronized(activatorsByClass) {
			return activatorsByClass.values().toArray(new ModuleService[activatorsByClass.size()]);
		}
	}

	
	protected final Logger logger;
	protected String name;
	protected BundleContext context;
	protected Config config;
	protected Router router;

	protected Class<? extends Model> sessionClass;

	volatile boolean running;

	public ModuleService() {
		this.logger = LogProvider.getLogger(getClass());
	}

	public ModuleService(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Add this module's routes to the given router. Routes can be
	 * added and removed later as necessary.
	 * <p>Note that a single module may serve multiple applications.</p>
	 * @param config the configuration
	 * @param router a Router to which routes are to be added
	 */
	public void addRoutes(Config config, Router router) {
		// subclasses to override if necessary
	}

	public List<String> getAssetList() {
		return JsonUtils.toStringList(StringUtils.getResourceAsString(getClass(), "assets.js"));
	}

	public Bundle getBundle(String fullName) {
		String name;
		Version version;
		int ix = fullName.lastIndexOf('_');
		if(ix == -1) {
			name = fullName;
			version = null;
		} else {
			name = fullName.substring(0, ix);
			version = Version.parseVersion(fullName.substring(ix+1));
		}
		for(Bundle bundle : context.getBundles()) {
			if(name.equals(bundle.getSymbolicName())) {
				if(version == null || version.equals(bundle.getVersion())) {
					return bundle;
				}
			}
		}
		return null;
	}

	public Config getConfiguration() {
		return config;
	}
	
	public BundleContext getContext() {
		return context;
	}

	public Class<?> getClass(String className) {
		Class<?> clazz = null;
		try {
			clazz = context.getBundle().loadClass(className);
		} catch(ClassNotFoundException e) {
			// fall through
		}
		for(String module : config.getModules()) {
			Bundle bundle = getBundle(module);
			if(bundle != null) {
				try {
					return context.getBundle().loadClass(className);
				} catch(ClassNotFoundException e) {
					// fall through
				}
			}
		}
		return clazz;
	}
	
	public Class<? extends HttpController> getControllerClass(Class<? extends Model> modelClass) {
		String controllerName = modelClass.getSimpleName() + "Controller";
		Class<? extends HttpController> controllerClass = getControllerClass(config, controllerName, context.getBundle());
		if(controllerClass != null) {
			return controllerClass;
		}
		for(String module : config.getModules()) {
			Bundle bundle = getBundle(module);
			if(bundle != null) {
				controllerClass = getControllerClass(config, controllerName, bundle);
				if(controllerClass != null) {
					return controllerClass;
				}
			}
		}
		return null;
	}

	private Class<? extends HttpController> getControllerClass(Config config, String controllerName, Bundle bundle) {
		String base;
		int ix = name.lastIndexOf('_');
		if(ix == -1) {
			base = name;
		} else {
			base = name.substring(0, ix);
		}
		String name = config.getPathToControllers(base).replace('/', '.') + "." + controllerName;
		try {
			Class<?> clazz = bundle.loadClass(name);
			if(HttpController.class.isAssignableFrom(clazz)) {
				return clazz.asSubclass(HttpController.class);
			}
		} catch(ClassNotFoundException e) {
			// discard
		}
		return null;
	}
	
	public Logger getLogger() {
		return logger;
	}

	/**
	 * The full name of this module (symbolicName + "_" + version)
	 */
	public String getName() {
		return name;
	}

	public Router getRouter() {
		if(router == null) {
			router = new Router(this);
		}
		return router;
	}
	
	public String getSymbolicName() {
		return context.getBundle().getSymbolicName();
	}

	Class<? extends View> getViewClass(String name) {
		int ix = this.name.lastIndexOf('_');
		String base = (ix == -1) ? this.name : this.name.substring(0, ix);
		String fullname = config.getPathToViews(base).replace('/', '.') + "." + name;
		try {
			Class<?> clazz = context.getBundle().loadClass(fullname);
			if(View.class.isAssignableFrom(clazz)) {
				return clazz.asSubclass(View.class);
			}
		} catch(ClassNotFoundException e) {
			// discard
		}
		return null;
	}
	
	/**
	 * Subclasses may override this method to initialize their own custom
	 * service trackers. Note that the service trackers for cache, session,
	 * persistence, and modules have already been initialized according to the
	 * configuration.js file by the time this method is called.
	 * 
	 * @param config
	 *            the configuration for this application
	 * @param mode
	 *            the mode in which this application is running
	 * @throws Exception
	 */
	protected void initializeServiceTrackers(Config config) throws Exception {
		// subclasses to implement if necessary
	}
	
	protected List<Class<?>> loadClassesInPackage(String pkg) throws Exception {
		ClassLoader loader = getClass().getClassLoader();
		String path = pkg.replace('.', '/') + "/";

		List<Class<?>> classes = new ArrayList<Class<?>>();

		File source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		if(source.isDirectory()) {
			source = new File(source, "src" + File.separator + path.replace('/', File.separatorChar));
			if(source.exists()) {
				String[] names = source.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".java");
					}
				});
				if(names != null && names.length > 0) {
					for(String name : names) {
						String clazz = pkg + "." + name.substring(0, name.length() - 5);
						classes.add(Class.forName(clazz, true, loader));
					}
				}
			}
		} else {
			JarFile jar = new JarFile(source);
			for(Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if(name.startsWith(path) && name.endsWith(".class") && name.indexOf('$') == -1) {
					String clazz = name.substring(0, name.length() - 6).replace('/', '.');
					classes.add(Class.forName(clazz, true, loader));
				}
			}
		}

		return classes;
	}

	/**
	 * Load the configuration for this bundle.  The default implementation fetches
	 * the configuration using getClass().  Subclasses should override to implement
	 * alternative behavior.
	 * @return the configuration
	 */
	protected void loadConfiguration() {
		config = Config.loadConfiguration(getClass());
	}
	
	/**
	 * Load the configuration for the given bundle.  The default implementation uses
	 * the bundle's Activator (as found in the MANIFEST) to load the configuration.
	 * @param bundle the bundle for which to get the configuration
	 * @return the configuration
	 * @throws ClassNotFoundException if the given bundle does have an Activator
	 */
	public Config loadConfiguration(Bundle bundle) throws ClassNotFoundException {
		String name = (String) bundle.getHeaders().get("Bundle-Activator");
		Class<?> clazz = bundle.loadClass(name);
		return Config.loadConfiguration(clazz);
	}

	@SuppressWarnings("unchecked")
	void loadControllerCaches(AppService app, Config config) throws Exception {
		logger.info("initializing ActionCache classes");

		String pkg = pkg(config.getPathToCaches(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			if(ControllerCache.class.isAssignableFrom(clazz)) {
				ControllerCache.addCache(app, this, (Class<? extends ControllerCache<?>>) clazz);
			}
		}
	}

	void loadModels(Config config) throws Exception {
		logger.info("loading Model classes");

		String pkg = pkg(config.getPathToModels(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			if("Session".equals(clazz.getSimpleName())) {
				if(Model.class.isAssignableFrom(clazz) && ISession.class.isAssignableFrom(clazz)) {
					logger.info("setting session class: " + clazz);
					sessionClass = clazz.asSubclass(Model.class);
				}
				break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	void loadObservers(Config config) throws Exception {
		logger.info("loading Observer and Notifier classes");

		String pkg = pkg(config.getPathToObservers(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			if(Observer.class.isAssignableFrom(clazz)) {
				Model.addObserver((Class<? extends Observer<?>>) clazz);
			}
		}
		
		pkg = pkg(config.getPathToNotifiers(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			if(Observer.class.isAssignableFrom(clazz)) {
				Model.addObserver((Class<? extends Observer<?>>) clazz);
			}
		}
	}

	private String pkg(String path) {
		path = path.trim();
		if(path.charAt(0) == '/') {
			path = path.substring(1);
		}
		if(path.charAt(path.length()-1) == '/') {
			path = path.substring(0, path.length()-1);
		}
		return path.replace('/', '.');
	}
	
	private String pkgPath() {
		return getClass().getPackage().getName().replace('.', '/');
	}

	/**
	 * Subclasses may override this method to register their own custom
	 * services. Note that the HttpApplication service for this application has
	 * already been registered by the time this method is called.
	 * 
	 * @param config
	 *            the configuration for this application
	 * @param mode
	 *            the mode in which this application is running
	 * @throws Exception
	 */
	protected void registerServices(Config config) throws Exception {
		// subclasses to implement if necessary
	}

	protected void setName(BundleContext context) throws Exception {
		Bundle bundle = context.getBundle();
		String n = bundle.getSymbolicName();
		Version v = bundle.getVersion();
		StringBuilder sb = new StringBuilder();
		sb.append(n).append('_');
		sb.append(v.getMajor()).append('.');
		sb.append(v.getMinor()).append('.');
		sb.append(v.getMicro());
		name = sb.toString();
	}

	protected void setup() {
		// subclasses to override if necessary
	}

	@Override
	public final void start(BundleContext context) throws Exception {
		this.context = context;
		setName(context);
		loadConfiguration();

		synchronized(activatorsByClass) {
			activatorsByClass.put(getClass(), this);
		}
		synchronized(activatorsByName) {
			activatorsByName.put(getName(), this);
		}

		logger.setTag(context.getBundle().getSymbolicName());
		logger.info(toString() + " starting...");
		
		if(this instanceof AppService) {
			((AppService) this).startApp();
		} else {
			setup();
		}

		context.registerService(ModuleService.class.getName(), this, Dictionary("name", getName()));

		startWorkers();

		logger.info(toString() + " started");
		running = true;
	}

	/**
	 * A place to start any work that needs to be run (do not run in main thread - use a worker).
	 * The module has been completely initialized at this point.
	 */
	public void startWorkers() {
		// subclasses to implement
	}
	
	@Override
	public final void stop(BundleContext context) throws Exception {
		running = false;
		logger.info(toString() + " stopping");

		teardown();
		if(this instanceof AppService) {
			((AppService) this).stopApp();
		}

		logger.info(toString() + " stopped");
		logger.setTag(null);

		this.context = null;

		synchronized(activatorsByClass) {
			activatorsByClass.remove(getClass());
		}
	}
	
	protected void teardown() {
		// subclasses to override if necessary
	}

	@Override
	public String toString() {
		return "Module " + getName();
	}

	void unloadControllerCaches(Config config) throws Exception {
		logger.info("unloading ActionCache classes");

		String pkg = pkg(config.getPathToCaches(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			ControllerCache.removeCache(this, clazz);
		}
	}

	void unloadModels(Config config) throws Exception {
		logger.info("unloading Model classes");

		String pkg = pkg(config.getPathToModels(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			Model.removeObservers(clazz);
		}
	}

	void unloadObservers(Config config) throws Exception {
		logger.info("unloading Observer and Notifier classes");

		String pkg = pkg(config.getPathToObservers(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			Model.removeObservers(clazz);
		}

		pkg = pkg(config.getPathToNotifiers(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			Model.removeObservers(clazz);
		}
	}

}
