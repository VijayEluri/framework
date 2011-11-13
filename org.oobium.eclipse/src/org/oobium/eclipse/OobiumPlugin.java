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
package org.oobium.eclipse;

import static org.eclipse.ui.IWorkbenchPage.VIEW_ACTIVATE;
import static org.oobium.build.workspace.Workspace.BUNDLE_REPOS;
import static org.oobium.build.workspace.Workspace.WORKING_DIR;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.oobium.build.console.Eclipse;
import org.oobium.build.runner.RunEvent;
import org.oobium.build.runner.RunListener;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.views.developer.ConsoleView;
import org.oobium.eclipse.views.server.ServerView;
import org.oobium.eclipse.workspace.ResourceChangeListener;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.OsgiRuntime;
import org.oobium.utils.FileUtils;
import org.oobium.utils.json.JsonUtils;
import org.osgi.framework.BundleContext;

public class OobiumPlugin extends AbstractUIPlugin {

	public static final String ID = OobiumPlugin.class.getName();
	
	public static final String PREFERENCES = "org.oobium.eclipse.preferences";
	
	private static OobiumPlugin instance;

	public static BundleContext getContext() {
		return instance.getBundle().getBundleContext();
	}
	
	public static Image getImage(String key) {
		return getImageDescriptor(key).createImage();
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor descriptor = instance.getImageRegistry().getDescriptor(key);
		if(descriptor == null) {
			descriptor = ImageDescriptor.createFromFile(OobiumPlugin.class, key);
			instance.getImageRegistry().put(key, descriptor);
		}
		return descriptor;
	}
	
	public static OobiumPlugin getInstance() {
		return instance;
	}
	
	public static Workspace getWorkspace() {
		return (instance != null) ? instance.workspace : null;
	}


	private final Logger logger;
	private Workspace workspace;
	private ResourceChangeListener listener;
	
	public OobiumPlugin() {
		super();
		instance = this;
		workspace = new Workspace();
		logger = LogProvider.getLogger(OobiumPlugin.class);
	}

	/**
	 * Execute the command at the given path, in the {@link ConsoleView} with the given ID.
	 * @param consoleViewId a String that is the ID of the {@link ConsoleView} to use to execute the command
	 * @param path a String that is the command to execute
	 * @see BundleConsoleView
	 */
	public void execute(String consoleViewId, String command) {
		try {
			ConsoleView view = (ConsoleView) getWorkbench().getWorkbenchWindows()[0].getPages()[0].showView(consoleViewId);
			view.execute(command);
		} catch(PartInitException e) {
			logger.warn(e);
		}
	}
	
	@Override
	public ImageRegistry getImageRegistry() {
		return super.getImageRegistry();
	}

	private void loadPreferences() {
		IPreferenceStore preferences = getPreferenceStore();
		
		String props = System.getProperty(PREFERENCES);
		if(props != null && props.length() > 0) {
			Map<String, String> map = JsonUtils.toStringMap(props);
			for(Entry<String, String> entry : map.entrySet()) {
				preferences.putValue(entry.getKey(), entry.getValue());
			}
		}
	}

	private void setupWorkspace(BundleContext context) {
		logger.info("setting up workspace");

		File wd; // working directory
		String wdPath = getPreferenceStore().getString(WORKING_DIR);
		if(wdPath.length() == 0) {
			wd = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
		} else {
			wd = new File(wdPath);
			if(!wd.isAbsolute()) {
				wd = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString(), wdPath);
			}
		}
		workspace.setWorkingDirectory(wd);
		if(logger.isLoggingDebug()) {
			logger.debug("workspace working directory set to \"" + wd + "\"");
		}

		String repos = getPreferenceStore().getString(BUNDLE_REPOS);
		if(repos.length() > 0) {
			workspace.setRepositories(repos);
			logger.debug("workspace bundle repos set to \"{}\"", repos);
		}

		String install = Platform.getInstallLocation().getURL().getFile();
		logger.debug("Eclipse installed at: {}", install);
		
		for(org.osgi.framework.Bundle bundle : context.getBundles()) {
			if(bundle.getState() != org.osgi.framework.Bundle.UNINSTALLED) {
				String location = bundle.getLocation();
				int ix = location.indexOf("file:");
				if(ix != -1) {
					File file = FileUtils.getAbsolute(location.substring(ix+5), install);
					Project loaded = workspace.load(file);
					if(loaded != null) {
						logger.trace("loaded {} from {}", loaded, loaded.file);
					} else {
						logger.trace("could not load bundle: {}", file);
					}
				} else {
					logger.trace("skipping {}", location);
				}
			}
		}
		
		if(workspace.getBundle(OsgiRuntime.Felix) == null) {
			logger.debug("loading felix bundles");
			Bundle build = workspace.getBuildBundle();
			if(build == null) {
				logger.debug("build bundle not found - cannot load felix bundles");
			} else {
				File dataArea = context.getDataFile("");
				if(!dataArea.isDirectory() && !dataArea.mkdirs()) {
					throw new IllegalStateException("could not create data directory: " + dataArea);
				}
				try {
					if(build.isJar) {
						workspace.load(FileUtils.copyJarEntry(build.file, "lib/felix.jar", dataArea));
						workspace.load(FileUtils.copyJarEntry(build.file, "lib/org.apache.felix.log-1.0.0.jar", dataArea));
					} else {
						File src = new File(build.file, "lib");
						File dst = new File(dataArea, "lib");
						if(!dst.isDirectory() && !dst.mkdirs()) {
							throw new IllegalStateException("could not create lib directory: " + dst);
						}
						FileUtils.copy(new File(src, "felix.jar"), dst);
						FileUtils.copy(new File(src, "org.apache.felix.log-1.0.0.jar"), dst);
						workspace.addRepository(dst);
					}
					logger.debug("felix bundles loaded from {}", dataArea);
				} catch(Exception e) {
					logger.warn("failed to copy felix bundles", e);
				}
			}
		}
		
		logger.debug("loading projects in the Eclipse workspace");
		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if(project.isOpen()) {
				workspace.load(project.getLocation().toFile());
				logger.debug("loaded {}", project.getName());
			}
		}
		
		logger.info("workspace setup complete");
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		logger.setTag(context.getBundle().getSymbolicName());
		
		loadPreferences();

		setupWorkspace(context);
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener = new ResourceChangeListener());
		
		RunnerService.addListener(new RunListener() {
			@Override
			public void handleEvent(final RunEvent event) {
				switch(event.type) {
				case Open:
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								Eclipse.openType(event.getMessage(), coerce(event.getDetails(), int.class));
							} catch(Exception e) {
								logger.warn(e);
							}
						}
					});
					break;
				case Start:
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(event.application.name);
					if(project.isOpen()) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									IWorkbenchPage page = getWorkbench().getWorkbenchWindows()[0].getPages()[0];
									page.showView(ServerView.ID, event.application.name, VIEW_ACTIVATE);
								} catch(PartInitException e) {
									logger.warn(e);
								}
							}
						});
					}
					break;
				}
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		logger.setTag(null);
		instance = null;
		workspace = null;
		super.stop(context);
	}
	
}
