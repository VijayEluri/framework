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

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.oobium.build.runner.RunEvent;
import org.oobium.build.runner.RunListener;
import org.oobium.build.runner.Runner;
import org.oobium.build.runner.RunnerService;
import org.oobium.build.workspace.Application;
import org.oobium.console.ConsolePrintStream;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.actions.ClearAction;
import org.oobium.eclipse.views.actions.ScrollLockAction;
import org.oobium.eclipse.views.server.actions.AutoUpdateAction;
import org.oobium.eclipse.views.server.actions.Migrate;
import org.oobium.eclipse.views.server.actions.MigratePurge;
import org.oobium.eclipse.views.server.actions.MigrateRedo;
import org.oobium.eclipse.views.server.actions.ShowBrowserAction;
import org.oobium.eclipse.views.server.actions.ShowConsoleAction;
import org.oobium.eclipse.views.server.actions.StartAction;
import org.oobium.eclipse.views.server.actions.StopAction;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.Mode;

public class ServerView extends ViewPart {

	public static final String ID = ServerView.class.getCanonicalName();
	
	
	private String applicationName;
	private Application application;
	private RunListener runListener;
	
	private SashForm sf;
	private BrowserPanel browserPanel;
	private ConsolePanel consolePanel;
	
	private StartAction startAction;
	private StopAction stopAction;
	private AutoUpdateAction updateAction;
	private Migrate migrateAction;
	private MigrateRedo redoAction;
	private MigratePurge purgeAction;
	
	private ClearAction clearAction;
	private ScrollLockAction scrollLockAction;
	private ShowBrowserAction showBrowserAction;
	private ShowConsoleAction showConsoleAction;
	
	private int browserWeight;
	private boolean browserVisible;
	private boolean consoleVisible;
	

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
		
		RunnerService.addListener(runListener = new RunListener() {
			@Override
			public void handleEvent(RunEvent event) {
				if(event.application == application) {
					switch(event.type) {
					case Start:
						start();
						browserPanel.updateAppState(true);
						break;
					case Started:
						browserPanel.refreshPaths(true);
						break;
					case Stop:
						stop();
						browserPanel.updateAppState(false);
						break;
					case Updated:
						browserPanel.refresh();
						break;
					}
				}
			}
		});
	}
	
	private void createActions() {
		clearAction = new ClearAction(consolePanel.getConsole());
		scrollLockAction = new ScrollLockAction(consolePanel.getConsole());
		showBrowserAction = new ShowBrowserAction(this, browserVisible);
		showConsoleAction = new ShowConsoleAction(this, consoleVisible);
		startAction = new StartAction(this);
		stopAction = new StopAction(this);
		updateAction = new AutoUpdateAction(this);
		migrateAction = new Migrate();
		redoAction = new MigrateRedo();
		purgeAction = new MigratePurge();
	}
	
	private void createMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();

		manager.add(clearAction);
		manager.add(scrollLockAction);
		manager.add(new Separator());
		manager.add(showBrowserAction);
		manager.add(showConsoleAction);
		manager.add(new Separator());
		manager.add(purgeAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		sf = new SashForm(parent, SWT.VERTICAL);

		browserPanel = new BrowserPanel(sf, this);
		consolePanel = new ConsolePanel(sf, this);

		sf.setWeights(new int[] { browserWeight, 100 - browserWeight } );

//		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.oobium.eclipse.viewer");
		createActions();
		hookContextMenu();
		createMenu();
		createToolBar();

		addListeners(parent);
	}
	
	private void createToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(startAction);
		manager.add(stopAction);
		manager.add(updateAction);
		manager.add(new Separator());
		manager.add(migrateAction);
		manager.add(redoAction);
		manager.add(new Separator());
		manager.add(showBrowserAction);
		manager.add(showConsoleAction);
		manager.add(new Separator());
		manager.add(clearAction);
		manager.add(scrollLockAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	boolean isRunning() {
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
		browserWeight = (memento != null) ? coerce(memento.getInteger("browserWeight"), 75) : 75;
		browserVisible = (memento != null) ? coerce(memento.getBoolean("browserVisible"), true) : true;
		consoleVisible = (memento != null) ? coerce(memento.getBoolean("consoleVisible"), true) : true;
		super.init(site, memento);
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString("application", applicationName);
		
		int[] weights = sf.getWeights();
		int total = 0;
		for(int weight : weights) {
			total += weight;
		}
		browserWeight = (int) (100 * ((float) weights[0] / total));
		memento.putInteger("browserWeight", browserWeight);
		
		memento.putBoolean("browserVisible", browserVisible);
		memento.putBoolean("consoleVisible", consoleVisible);
	}
	
	public void setApplication(String name) {
		applicationName = name;
		application = OobiumPlugin.getWorkspace().getApplication(applicationName);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if(application != null) {
					if(isRunning()) {
						Runner runner = RunnerService.getRunner(application);
						runner.setError(new ConsolePrintStream(consolePanel.getConsole().err));
						runner.setOut(new ConsolePrintStream(consolePanel.getConsole().out));
						startAction.setEnabled(false);
						stopAction.setEnabled(true);
						migrateAction.setEnabled(true);
						redoAction.setEnabled(true);
					} else {
						startAction.setEnabled(true);
						stopAction.setEnabled(false);
						migrateAction.setEnabled(false);
						redoAction.setEnabled(false);
					}
				} else {
					startAction.setEnabled(false);
					stopAction.setEnabled(false);
					migrateAction.setEnabled(false);
					redoAction.setEnabled(false);
				}
			}
		});
	}
	
	public void setBrowserVisible(boolean visible) {
		if(visible) {
			if(sf.getWeights()[0] == 0) {
				sf.setWeights(new int[] { 50, 50 });
			}
			if(!showBrowserAction.isChecked()) {
				showBrowserAction.setChecked(true);
			}
		} else {
			if(sf.getWeights()[0] != 0) {
				sf.setWeights(new int[] { 0, 100 });
			}
			if(showBrowserAction.isChecked()) {
				showBrowserAction.setChecked(false);
			}
		}
		browserVisible = visible;
	}
	
	public void setConsoleVisible(boolean visible) {
		if(visible) {
			if(sf.getWeights()[1] == 0) {
				sf.setWeights(new int[] { 50, 50 });
			}
			if(!showConsoleAction.isChecked()) {
				showConsoleAction.setChecked(true);
			}
		} else {
			if(sf.getWeights()[1] != 0) {
				sf.setWeights(new int[] { 100, 0 });
			}
			if(showConsoleAction.isChecked()) {
				showConsoleAction.setChecked(false);
			}
		}
		consoleVisible = visible;
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
		if(application != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					consolePanel.getConsole().clear();
				}
			});
			String[] options = new String[] {
					"-D" + Logger.SYS_PROP_CONSOLE + "=debug"
			};
			Runner runner = RunnerService.start(OobiumPlugin.getWorkspace(), application, Mode.DEV, options);
			runner.setError(new ConsolePrintStream(consolePanel.getConsole().err));
			runner.setOut(new ConsolePrintStream(consolePanel.getConsole().out));
			startAction.setEnabled(false);
			stopAction.setEnabled(true);
			migrateAction.setEnabled(true);
			redoAction.setEnabled(true);
			if(updateAction.isChecked()) {
				RunnerService.unpauseUpdaters();
			} else {
				RunnerService.pauseUpdaters();
			}
		}
	}

	public void setAutoUpdate(boolean update) {
		updateAction.setChecked(update);
		if(application != null) {
			if(update) {
				RunnerService.unpauseUpdaters();
			} else {
				RunnerService.pauseUpdaters();
			}
		}
	}
	
	public void stop() {
		if(application != null) {
			RunnerService.removeListener(runListener);
			RunnerService.stop(application);
			startAction.setEnabled(true);
			stopAction.setEnabled(false);
			migrateAction.setEnabled(false);
			redoAction.setEnabled(false);
			browserPanel.updateAppState(false);
			consolePanel.getConsole().out.println(application + " stopped");
			RunnerService.addListener(runListener);
		}
	}
	
}
