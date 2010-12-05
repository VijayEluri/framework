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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.oobium.app.server.routing.Router;
import org.oobium.logging.Logger;
import org.oobium.persist.Observer;
import org.oobium.utils.Config;
import org.oobium.utils.StringUtils;
import org.oobium.utils.json.JsonUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public abstract class ModuleService implements AssetProvider, BundleActivator {

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

	public ModuleService() {
		logger = Logger.getLogger(getClass());
	}

	/**
	 * Add this module's routes to the Application's router. Routes can be
	 * added, removed, and modified later if necessary.<br/>
	 * Note that a single module may serve multiple applications.
	 * 
	 * @param router
	 *            a Router object belonging to the Application
	 */
	public abstract void addRoutes(Config config, Router router);

	protected void doStart(BundleContext context) throws Exception {
		// subclasses to implement if necessary
	}

	protected void doStop(BundleContext context) throws Exception {
		// subclasses to implement if necessary
	}

	@Override
	public List<String> getAssetList() {
		return JsonUtils.toStringList(StringUtils.getResourceAsString(getClass(), "assets.js"));
	}

	public BundleContext getContext() {
		return context;
	}

	List<String> getControllerPaths(Config config) throws Exception {
		String path = config.getPathToControllers(pkgPath());
		if(path.charAt(path.length()-1) != '/') {
			path = path + "/";
		}
		
		List<String> names = new ArrayList<String>();

		File source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		if(source.isDirectory()) {
			source = new File(source, "src/" + path);
			if(source.exists()) {
				String pkg = pkg(path);
				
				String[] classes = source.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("Controller.java");
					}
				});
				if(classes != null && classes.length > 0) {
					for(String name : classes) {
						names.add(pkg + "." + name.substring(0, name.length() - 5));
					}
				}
			}
		} else {
			JarFile jar = new JarFile(source);
			for(Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if(name.startsWith(path) && name.endsWith(".class") && name.indexOf('$') == -1) {
					names.add(name.substring(0, name.length() - 6).replace('/', '.'));
				}
			}
		}

		return names;
	}

	public Logger getLogger() {
		return logger;
	}

	/**
	 * The full name of this module (symbolicName + "_" + version)
	 */
	@Override
	public String getName() {
		return name;
	}

	public String getSymbolicName() {
		return context.getBundle().getSymbolicName();
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
			source = new File(source, "src/" + path);
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
	
	void loadModels(Config config) throws Exception {
		logger.info("loading Model classes");

		String pkg = pkg(config.getPathToModels(pkgPath()));
		loadClassesInPackage(pkg);
	}

	void loadObservers(Config config) throws Exception {
		logger.info("loading Observer classes");

		String pkg = pkg(config.getPathToObservers(pkgPath()));
		for(Class<?> clazz : loadClassesInPackage(pkg)) {
			if(Observer.class.isAssignableFrom(clazz)) {
				addObserver(clazz);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addObserver(Class<?> clazz) {
		Observer.addObserver((Class<? extends Observer<?>>) clazz);
	}
	
	private String pkg(String path) {
		path = path.trim();
		if(path.charAt(0) == '/') {
			path = path.substring(1);
		}
		if(path.charAt(path.length()-1) == '/') {
			path = path.substring(0, path.length()-1);
		}
		return path.replaceAll("/", ".");
	}

	private String pkgPath() {
		return getClass().getPackage().getName().replaceAll("\\.", "/");
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

	/**
	 * Remove this module's routes to the Application's router. Routes can be
	 * added, removed, and modified later if necessary.<br/>
	 * Note that a single module may serve multiple applications.
	 * 
	 * @param router
	 *            a Router object belonging to the Application
	 */
	public abstract void removeRoutes(Config config, Router router);

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

	@Override
	public final void start(BundleContext context) throws Exception {
		this.context = context;
		setName(context);

		synchronized(activatorsByClass) {
			activatorsByClass.put(getClass(), this);
		}
		synchronized(activatorsByName) {
			activatorsByName.put(getName(), this);
		}

		logger.setBundle(context.getBundle());
		logger.info(toString() + " starting...");

		Properties props = new Properties();
		props.put("name", getName());
		context.registerService(ModuleService.class.getName(), this, props);

		doStart(context);

		logger.info(toString() + " started");
	}

	@Override
	public final void stop(BundleContext context) throws Exception {
		logger.info(toString() + " stopping");

		doStop(context);

		logger.info(toString() + " stopped");
		logger.setBundle(null);

		this.context = null;

		synchronized(activatorsByClass) {
			activatorsByClass.remove(getClass());
		}
	}

	@Override
	public String toString() {
		return "Module " + getName();
	}

	void unloadModels(Config config) throws Exception {
		logger.info("unloading Model classes");

		String pkg = pkg(config.getPathToModels(pkgPath()));
		List<Class<?>> classes = loadClassesInPackage(pkg);
		for(Class<?> clazz : classes) {
			Observer.removeObservers(clazz);
		}
	}

	void unloadObservers(Config config) throws Exception {
		logger.info("unloading Observer classes");

		String pkg = pkg(config.getPathToObservers(pkgPath()));
		List<Class<?>> classes = loadClassesInPackage(pkg);
		for(Class<?> clazz : classes) {
			Observer.removeObservers(clazz);
		}
	}

}
