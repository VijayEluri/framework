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
import static org.oobium.build.workspace.Workspace.WORKSPACE;

import java.util.Map;
import java.util.Map.Entry;

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
import org.oobium.build.runner.RunEvent;
import org.oobium.build.runner.RunEvent.Type;
import org.oobium.build.runner.RunListener;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.views.developer.ConsoleView;
import org.oobium.eclipse.views.server.ServerView;
import org.oobium.eclipse.workspace.ResourceChangeListener;
import org.oobium.logging.Logger;
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
	private BundleContext context;
	private Workspace workspace;
	private ResourceChangeListener listener;
	
	public OobiumPlugin() {
		super();
		instance = this;
		workspace = new Workspace();
		logger = Logger.getLogger(OobiumPlugin.class);
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
		
		String value;
		
		logger.info("setting up workspace");
		
		value = Platform.getInstallLocation().getURL().getPath() + "plugins";
		preferences.setDefault(BUNDLE_REPOS, value);
		if(!preferences.contains(BUNDLE_REPOS)) {
			preferences.setToDefault(BUNDLE_REPOS);
		}
		String repos = preferences.getString(BUNDLE_REPOS);
		workspace.setBundleRepositories(repos);
		if(logger.isLoggingInfo()) {
			logger.info("workspace bundle repos set to \"" + repos + "\"");
		}

		value = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		preferences.setDefault(WORKSPACE, value);
		if(!preferences.contains(WORKSPACE)) {
			preferences.setToDefault(WORKSPACE);
		}
		String dir = preferences.getString(WORKSPACE);
		workspace.setDirectory(dir);
		if(logger.isLoggingInfo()) {
			logger.info("workspace directory set to \"" + dir + "\"");
		}
		
		logger.info("workspace setup");
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
		logger.setBundle(context.getBundle());
		
		loadPreferences();
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener = new ResourceChangeListener());
		
		RunnerService.addListener(new RunListener() {
			@Override
			public void handleEvent(final RunEvent event) {
				if(event.type == Type.Start) {
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
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		logger.setBundle(null);
		instance = null;
		workspace = null;
		this.context = null;
		super.stop(context);
	}
	
}
