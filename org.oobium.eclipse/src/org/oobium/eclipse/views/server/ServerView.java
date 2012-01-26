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
package org.oobium.eclipse.views.server;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ID_SOCKET_LISTEN_VM_CONNECTOR;
import static org.oobium.utils.literal.Map;
import static org.oobium.utils.literal.e;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.oobium.build.runner.RunEvent;
import org.oobium.build.runner.RunListener;
import org.oobium.build.runner.Runner;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Application;
import org.oobium.console.ConsolePrintStream;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.actions.ClearAction;
import org.oobium.eclipse.views.actions.ScrollLockAction;
import org.oobium.eclipse.views.server.actions.AutoMigrateAction;
import org.oobium.eclipse.views.server.actions.AutoUpdateAction;
import org.oobium.eclipse.views.server.actions.DebugAction;
import org.oobium.eclipse.views.server.actions.MigrateAction;
import org.oobium.eclipse.views.server.actions.MigratePurgeAction;
import org.oobium.eclipse.views.server.actions.MigrateRedoAction;
import org.oobium.eclipse.views.server.actions.StartAction;
import org.oobium.eclipse.views.server.actions.StopAction;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.json.JsonUtils;

public class ServerView extends ViewPart {

	public static final String ID = ServerView.class.getCanonicalName();
	
	
	private String applicationName;
	private Application application;
	private String properties;
	private RunListener errorListener;
	private RunListener runListener;
	
	private Composite panel;
	private BrowserPanel browserPanel;
	private ConsolePanel consolePanel;
	
	private DebugAction debugAction;
	private StartAction startAction;
	private StopAction stopAction;
	private AutoUpdateAction autoUpAction;
	private MigrateAction migrateAction;
	private MigrateRedoAction redoAction;
	private MigratePurgeAction purgeAction;
	private AutoMigrateAction autoMigAction;
	
	private ClearAction clearAction;
	private ScrollLockAction scrollLockAction;
	

	public ServerView() {
		// default constructor
	}

	private void addListeners(Composite parent) {
		if(applicationName == null) {
			setApplication(getViewSite().getSecondaryId());
		} else {
			setApplication(applicationName);
		}
		
		browserPanel.updateAppState(isRunning());
		
		errorListener = new RunListener() {
			@Override
			public void handleEvent(RunEvent event) {
				if(event.application == application) {
					switch(event.type) {
					case Error:
						StatusManager.getManager().handle(getStatus(event), StatusManager.SHOW);
						break;
					case Warning:
						StatusManager.getManager().handle(getStatus(event), StatusManager.SHOW);
						break;
					}
				}
			}
		};

		RunnerService.addListener(runListener = new RunListener() {
			@Override
			public void handleEvent(RunEvent event) {
				if(event.application == application) {
					switch(event.type) {
					case Start:
						properties = JsonUtils.toJson(RunnerService.getRunner(application).getProperties());
						start();
						break;
					case Started:
						browserPanel.updateAppState(true);
						if(!RunnerService.isAutoMigrating(application)) {
							browserPanel.refreshPaths(true);
						}
						break;
					case Stop:
						stop();
						browserPanel.updateAppState(false);
						break;
					case Updated:
						if(!RunnerService.isAutoMigrating(application)) {
							browserPanel.refreshPaths(true);
						}
						break;
					case Migrated:
						if(RunnerService.isAutoMigrating(application)) {
							browserPanel.refreshPaths(true);
						}
						break;
					}
				}
			}
		});
	}

	private void createActions() {
		clearAction = new ClearAction(consolePanel.getConsole());
		scrollLockAction = new ScrollLockAction(consolePanel.getConsole());
		startAction = new StartAction(this);
		debugAction = new DebugAction(this);
		stopAction = new StopAction(this);
		autoUpAction = new AutoUpdateAction(this);
		migrateAction = new MigrateAction();
		redoAction = new MigrateRedoAction();
		purgeAction = new MigratePurgeAction();
		autoMigAction = new AutoMigrateAction(this);
	}
	
	private void createMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();

		manager.add(clearAction);
		manager.add(scrollLockAction);
		manager.add(new Separator());
		manager.add(new Separator());
		manager.add(purgeAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	Composite getPanel() {
		return panel;
	}
	
	public void createPartControl(Composite parent) {
		panel = parent;

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		panel.setLayout(layout);

		browserPanel = new BrowserPanel(this);
		browserPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		consolePanel = new ConsolePanel(this);
		consolePanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

//		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.oobium.eclipse.viewer");
		createActions();
		hookContextMenu();
		createMenu();
		createToolBar();

		addListeners(parent);
	}
	
	private void createToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(debugAction);
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(autoUpAction);
		manager.add(new Separator());
		manager.add(migrateAction);
		manager.add(redoAction);
		manager.add(autoMigAction);
		manager.add(new Separator());
		manager.add(new Separator());
		manager.add(clearAction);
		manager.add(scrollLockAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private boolean isRunning() {
		if(application != null) {
			return RunnerService.isRunning(application);
		}
		return false;
	}
	
	@Override
	public void dispose() {
		if(runListener != null) {
			RunnerService.removeListener(runListener);
		}
		if(application != null && RunnerService.isRunning(application)) {
			RunnerService.stop(application);
		}
	}

	BrowserPanel getBrowserPanel() {
		return browserPanel;
	}
	
	ConsolePanel getConsolePanel() {
		return consolePanel;
	}

	List<String> getPaths() {
		if(application != null) {
			return RunnerService.getPaths(application);
		}
		return new ArrayList<String>(0);
	}
	
	private IStatus getStatus(RunEvent event) {
		int severity = IStatus.OK;
		String message = event.getMessage();
		Throwable throwable = null;
		int ix = -1;
		switch(event.type) {
		case Error:
			severity = IStatus.ERROR;
			ix = message.indexOf("(ERROR)");
			throwable = new Throwable(message);
			break;
		case Warning:
			severity = IStatus.WARNING;
			ix = message.indexOf("(WARN)");
			throwable = new Throwable(message);
			break;
		}
		if(ix != -1) {
			ix = message.indexOf(':', ix);
			message = message.substring(ix+1).trim();
		}
		return new Status(severity, applicationName, message, throwable);
	}
	
	private void hookContextMenu() {
//		MenuManager menuMgr = new MenuManager("#PopupMenu");
//		menuMgr.setRemoveAllWhenShown(true);
//		menuMgr.addMenuListener(new IMenuListener() {
//			public void menuAboutToShow(IMenuManager manager) {
//				DeveloperConsoleView.this.createContextMenu(manager);
//			}
//		});
//		Menu menu = menuMgr.createContextMenu(consolePanel.getConsole());
//		consolePanel.getConsole().setMenu(menu);
//		getSite().registerContextMenu(menuMgr, consolePanel.getConsole());
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		applicationName = (memento != null) ? memento.getString("application") : null;
		properties = (memento != null) ? memento.getString("properties") : null;
		super.init(site, memento);
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString("application", applicationName);
		memento.putString("properties", properties);
	}
	
	public void setApplication(String name) {
		applicationName = name;
		application = OobiumPlugin.getWorkspace().getApplication(applicationName);
		browserPanel.setApplication(name);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if(application != null) {
					if(isRunning()) {
						Runner runner = RunnerService.getRunner(application);
						runner.setError(new ConsolePrintStream(consolePanel.getConsole().err));
						runner.setOut(new ConsolePrintStream(consolePanel.getConsole().out));
						debugAction.setEnabled(false);
						startAction.setEnabled(false);
						stopAction.setEnabled(true);
						autoUpAction.setEnabled(true);
						boolean b = autoUpAction.isChecked();
						migrateAction.setEnabled(b);
						redoAction.setEnabled(b);
						purgeAction.setEnabled(b);
						autoMigAction.setEnabled(b);
					} else {
						debugAction.setEnabled(true);
						startAction.setEnabled(true);
						stopAction.setEnabled(false);
						autoUpAction.setEnabled(false);
						migrateAction.setEnabled(false);
						redoAction.setEnabled(false);
						purgeAction.setEnabled(false);
						autoMigAction.setEnabled(false);
					}
				} else {
					debugAction.setEnabled(false);
					startAction.setEnabled(false);
					stopAction.setEnabled(false);
					autoUpAction.setEnabled(false);
					migrateAction.setEnabled(false);
					redoAction.setEnabled(false);
					purgeAction.setEnabled(false);
					autoMigAction.setEnabled(false);
				}
			}
		});
	}
	
	public void setFocus() {
		if(browserPanel.isVisible()) {
			browserPanel.setFocus();
		} else {
			consolePanel.setFocus();
		}
	}
	
	void setStatus(final Image image, final String message) {
		getSite().getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
				manager.setMessage(image, message);
			}
		});
	}
	
	public void start() {
		start(false);
	}
	
	public void start(boolean debug) {
		if(application != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					consolePanel.getConsole().clear();
				}
			});
			Map<String, String> properties = JsonUtils.toStringMap(this.properties);
			
			Runner runner = RunnerService.getRunner(application);
			if(runner == null) {
				if(debug) {
					runner = RunnerService.debug(OobiumPlugin.getWorkspace(), application, Mode.DEV, properties);
				} else {
					runner = RunnerService.run(OobiumPlugin.getWorkspace(), application, Mode.DEV, properties);
				}
			}
			runner.setError(new ConsolePrintStream(consolePanel.getConsole().err));
			runner.setOut(new ConsolePrintStream(consolePanel.getConsole().out));
			final boolean debugging = runner.getDebug();
			if(debugging) {
				try {
					
					Thread.sleep(500); // TODO another oobicrap temporary fix...
					
					ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
					ILaunchConfigurationType type = manager.getLaunchConfigurationType(ID_REMOTE_JAVA_APPLICATION);
					ILaunchConfigurationWorkingCopy configuration = type.newInstance(null, "Debug Oobium");
					boolean attach = true; // place-holder
					if(attach) {
						configuration.setAttribute(ATTR_VM_CONNECTOR, ID_SOCKET_ATTACH_VM_CONNECTOR);
						configuration.setAttribute(ATTR_CONNECT_MAP, Map( e("hostname", "localhost"), e("port", "8000")) );
					} else {
						configuration.setAttribute(ATTR_VM_CONNECTOR, ID_SOCKET_LISTEN_VM_CONNECTOR);
						configuration.setAttribute(ATTR_CONNECT_MAP, Map("port", "8000"));
					}
					configuration.setAttribute(ATTR_ALLOW_TERMINATE, true);
					
					configuration.setAttribute(ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.pde.ui.workbenchClasspathProvider");
					configuration.setAttribute(ATTR_DEFAULT_SOURCE_PATH, false);
					
					configuration.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor());
				} catch(Exception e) {
					// TODO
					e.printStackTrace();
				}
			}

			debugAction.setEnabled(false);
			startAction.setEnabled(false);
			stopAction.setEnabled(true);
			autoUpAction.setEnabled(true);
			purgeAction.setEnabled(true);
			if(autoUpAction.isChecked()) {
				boolean b = isRunning();
				migrateAction.setEnabled(b);
				redoAction.setEnabled(b);
				autoMigAction.setEnabled(b);
				RunnerService.unpauseUpdaters();
			} else {
				migrateAction.setEnabled(false);
				redoAction.setEnabled(false);
				autoMigAction.setEnabled(false);
				RunnerService.pauseUpdaters();
			}

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if(debugging) {
						setPartName("Server (debug)");
					} else {
						setPartName("Server (run)");
					}
				}
			});

			RunnerService.addListener(errorListener);
		}
	}

	public void setAutoUpdate(boolean update) {
		autoUpAction.setChecked(update);
		boolean b = isRunning() && update;
		migrateAction.setEnabled(b);
		redoAction.setEnabled(b);
		autoMigAction.setEnabled(b);
		if(application != null) {
			if(update) {
				RunnerService.unpauseUpdaters();
			} else {
				RunnerService.pauseUpdaters();
			}
		}
	}
	
	public void setAutoMigrate(boolean auto) {
		autoMigAction.setChecked(auto);
		if(application != null) {
			if(auto) {
				RunnerService.unpauseMigratorUpdaters();
			} else {
				RunnerService.pauseMigratorUpdaters();
			}
		}
	}
	
	public void stop() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				setPartName("Server");
			}
		});
		RunnerService.removeListener(errorListener);
		if(application != null) {
			RunnerService.removeListener(runListener);
			RunnerService.stop(application);
			debugAction.setEnabled(true);
			startAction.setEnabled(true);
			stopAction.setEnabled(false);
			autoUpAction.setEnabled(false);
			migrateAction.setEnabled(false);
			redoAction.setEnabled(false);
			purgeAction.setEnabled(false);
			autoMigAction.setEnabled(false);
			browserPanel.updateAppState(false);
			RunnerService.addListener(runListener);
		}
	}
	
}
