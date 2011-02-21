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
package org.oobium.eclipse.views.developer;

import static org.oobium.build.workspace.Workspace.BUNDLE_REPOS;
import static org.oobium.build.workspace.Workspace.RUNTIME;
import static org.oobium.build.workspace.Workspace.WORKING_DIR;
import static org.oobium.utils.StringUtils.blank;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderRootCommand;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Migrator;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.TestSuite;
import org.oobium.build.workspace.WorkspaceEvent;
import org.oobium.build.workspace.WorkspaceListener;
import org.oobium.console.Command;
import org.oobium.console.CommandEvent;
import org.oobium.console.CommandListener;
import org.oobium.console.Console;
import org.oobium.console.ConsolePage;
import org.oobium.eclipse.OobiumNature;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.actions.ClearAction;
import org.oobium.eclipse.views.actions.ScrollLockAction;
import org.oobium.eclipse.views.developer.actions.LinkedAction;
import org.oobium.eclipse.views.developer.commands.PreferenceCommand;
import org.oobium.eclipse.views.developer.commands.PreferencesCommand;
import org.oobium.utils.json.JsonUtils;

public class ConsoleView extends ViewPart {

	public static final String ID = ConsoleView.class.getCanonicalName();
	
	private static final String WORKBENCH_PREFERENCES = "org.eclipse.ui.workbench";
	private static final String TEXTFONT_KEY = "org.eclipse.jdt.ui.editors.textfont";


	private BuilderRootCommand root;

	private Composite labelBar;
	private Label applicationImg;
	private Label applicationLbl;
	private Label projectImg;
	private Label projectLbl;
	private ConsolePage consolePage;
	
	private String application;
	private String project;
	private String[] commandHistory;
	private boolean linked;
	
	private ClearAction clearAction;
	private ScrollLockAction scrollLockAction;
	
	private LinkedAction linkedAction;
	private ISelectionListener explorerListener;
	private WorkspaceListener workspaceListener;
	private IPreferenceChangeListener preferenceListener;

	private PropertyChangeListener applicationListener;
	private PropertyChangeListener bundleListener;

	private CommandTracker tracker;
	
	
	public ConsoleView() {
		// default constructor
	}

	private void createActions() {
		clearAction = new ClearAction(consolePage.getConsole());
		scrollLockAction = new ScrollLockAction(consolePage.getConsole());
		linkedAction = new LinkedAction(this);
		linkedAction.setChecked(linked);
	}
	
	protected ConsolePage createConsolePage(Composite parent) {
		ConsolePage page = new ConsolePage(parent, SWT.NONE);
		Console console = page.getConsole();
		
		root = new BuilderRootCommand(OobiumPlugin.getWorkspace());
		console.setRootCommand(root);
		
		String bundleName = getClass().getPackage().getName() + ".console";
		console.addResourceStrings(bundleName, getClass().getClassLoader());

		root.add("set", new PreferenceCommand());
		root.add("show", new PreferencesCommand(this));

		CommandListener createAppListener = new CommandListener() {
			@Override
			public void handleEvent(CommandEvent event) {
				File app = new File(event.command.param(0));
				if(!app.isAbsolute()) {
					app = new File(((BuilderCommand) event.command).getPwd(), event.command.param(0));
				}
				setApplication(app);
				setProject(app);
			}
		};
		root.get("create application").addListener(createAppListener);
		root.get("create webservice").addListener(createAppListener);
		
		root.get("create module").addListener(new CommandListener() {
			@Override
			public void handleEvent(CommandEvent event) {
				File app = new File(event.command.param(0));
				if(!app.isAbsolute()) {
					app = new File(((BuilderCommand) event.command).getPwd(), event.command.param(0));
				}
				setProject(app);
			}
		});
		
		root.addListener("application", applicationListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Application app = (Application) evt.getNewValue();
				setApplication((app == null) ? null : app.file);
			}
		});
		
		root.addListener("bundle", bundleListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Bundle bundle = (Bundle) evt.getNewValue();
				setProject((bundle == null) ? null : bundle.file);
			}
		});
		
		tracker = new CommandTracker(console);
		tracker.open();
		
		return page;
	}
	
	private void createContextMenu() {
//		manager.add(action1);
//		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void createMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();

		manager.add(clearAction);
		manager.add(scrollLockAction);
		manager.add(new Separator());
		manager.add(linkedAction);
		
		manager.add(new Separator());
	}
	
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		parent.setLayout(layout);

		labelBar = new Composite(parent, SWT.NONE);
		labelBar.setLayout(new GridLayout(4, false));
		labelBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		applicationImg = new Label(labelBar, SWT.NONE);
		applicationImg.setImage(OobiumPlugin.getImage("/icons/application.png"));
		applicationImg.setToolTipText("Active Application");
		applicationImg.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		
		applicationLbl = new Label(labelBar, SWT.NONE);
		applicationLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		projectImg = new Label(labelBar, SWT.NONE);
		GridData data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.horizontalIndent = 15;
		projectImg.setLayoutData(data);
		
		projectLbl = new Label(labelBar, SWT.NONE);
		projectLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		consolePage = createConsolePage(parent);
		consolePage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if(commandHistory != null) {
			consolePage.getConsole().setCommandHistory(commandHistory);
		}

		updateConsoleFont();
		
		setApplication(application);
		setProject(project);
		
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.oobium.eclipse.viewer");
		createActions();
		hookContextMenu();
		createContextMenu();
		createMenu();
		createToolBar();

		
		// add listeners
		
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch(event.type) {
				case SWT.Selection: {
					String href = !blank(event.data) ? String.valueOf(event.data) : event.text;
					consolePage.getConsole().execute(href, false);
					break;
				}
				case SWT.MouseEnter: {
					String href = !blank(event.data) ? String.valueOf(event.data) : event.text;
					setStatus(OobiumPlugin.getImage("/icons/run.gif"), "command: " + href);
					break;
				}
				case SWT.MouseExit:
					setStatus(null, null);
					break;
				}
			}
		};
		consolePage.getConsole().defLink.addListener(SWT.Selection, listener);
		consolePage.getConsole().defLink.addListener(SWT.MouseEnter, listener);
		consolePage.getConsole().defLink.addListener(SWT.MouseExit, listener);

		parent.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle r = consolePage.getBounds();
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
				e.gc.drawLine(0, r.y-1, r.width, r.y-1);
			}
		});
		
		OobiumPlugin.getWorkspace().addListener(workspaceListener = new WorkspaceListener() {
			@Override
			public void handleEvent(WorkspaceEvent event) {
				setApplication(application);
				setProject(project);
			}
		});
		
		new InstanceScope().getNode(WORKBENCH_PREFERENCES).addPreferenceChangeListener(preferenceListener = new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				if(TEXTFONT_KEY.equals(event.getKey())) {
					updateConsoleFont();
				}
			}
		});

		root.setPwd(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
	}

	private void createToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(clearAction);
		manager.add(scrollLockAction);
		manager.add(new Separator());
		manager.add(linkedAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	@Override
	public void dispose() {
		if(explorerListener != null) {
			getSite().getPage().removeSelectionListener(/*JavaUI.ID_PACKAGES, */explorerListener);
			explorerListener = null;
		}
		if(workspaceListener != null) {
			OobiumPlugin.getWorkspace().removeListener(workspaceListener);
			workspaceListener = null;
		}
		if(preferenceListener != null) {
			new InstanceScope().getNode(WORKBENCH_PREFERENCES).removePreferenceChangeListener(preferenceListener);
		}
		if(tracker != null) {
			tracker.close();
			tracker = null;
		}
	}
	
	public void execute(String command) {
		consolePage.getConsole().execute(command, false);
	}
	
	private String getLabelText(String label) {
		if(label != null && label.length() > 0) {
			int ix = label.lastIndexOf(File.separatorChar);
			if(ix == -1) {
				return label;
			} else {
				return new String(label.substring(ix+1));
			}
		}
		return "not set";
	}
	
	private String getLabelToolTipText(String label) {
		if(label != null && label.length() > 0) {
			return label;
		}
		return "";
	}
	
	public String[] getPreferences() {
		return new String[] { BUNDLE_REPOS, RUNTIME, WORKING_DIR };
	}
	
	protected Command getRootCommand() {
		return root;
	}
	
	private void hookContextMenu() {
//		MenuManager menuMgr = new MenuManager("#PopupMenu");
//		menuMgr.setRemoveAllWhenShown(true);
//		menuMgr.addMenuListener(new IMenuListener() {
//			public void menuAboutToShow(IMenuManager manager) {
//				DeveloperConsoleView.this.createContextMenu(manager);
//			}
//		});
//		Menu menu = menuMgr.createContextMenu(console);
//		console.setMenu(menu);
//		getSite().registerContextMenu(menuMgr, console);
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		application = (memento == null) ? null : memento.getString("application");
		project = (memento == null) ? null : memento.getString("bundle");

		String history = (memento == null) ? null : memento.getString("history");
		if(history != null) {
			commandHistory = JsonUtils.toStringList(history).toArray(new String[0]);
		}

		Boolean linked = (memento == null) ? null : memento.getBoolean("linked");
		if(linked == null) {
			this.linked = true; // default value
		} else {
			this.linked = linked;
		}
		
		super.init(site);
		
		if(project == null) {
			setSelection(getSite().getPage().getSelection());
		}
		
		getSite().getPage().addSelectionListener(/*JavaUI.ID_PACKAGES, */explorerListener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if(isLinked()) {
					setSelection(selection);
				}
			}
		});
	}
	
	public boolean isLinked() {
		return linked;
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putString("application", application);
		memento.putString("bundle", project);
		memento.putString("history", JsonUtils.toJson(consolePage.getConsole().getCommandHistory()));
		memento.putBoolean("linked", linked);
	}
	
	public void setApplication(File dir) {
		application = (dir == null || !dir.isDirectory()) ? null : dir.getAbsolutePath();
		if(applicationLbl != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					applicationLbl.setText(getLabelText(application));
					applicationLbl.setToolTipText(getLabelToolTipText(application));
					labelBar.getParent().layout(new Control[] { applicationLbl });
				}
			});
		}
		if(root != null) {
			Application app = OobiumPlugin.getWorkspace().getApplication(dir);
			root.removeListener("application", applicationListener);
			root.setApplication(app);
			root.addListener("application", applicationListener);
		}
	}
	
	private void setApplication(String str) {
		setApplication((str != null) ? new File(str) : null);
	}
	
	public void setFocus() {
		consolePage.setFocus();
	}
	
	/**
	 * Set whether or not the active application and/or bundle are linked to
	 * the selection in the Package Explorer view.
	 * @param linked true if this console is to be linked with the package explorer, false otherwise
	 */
	public void setLinked(boolean linked) {
		this.linked = linked;
	}

	public void setProject(File dir) {
		final Bundle bundle = OobiumPlugin.getWorkspace().getBundle(dir);
		project = (dir == null || !dir.isDirectory()) ? null : dir.getAbsolutePath();
		if(projectLbl != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if(project == null || project.equals(application)) {
						projectImg.setVisible(false);
						projectLbl.setVisible(false);
						((GridData) projectImg.getLayoutData()).exclude = true;
						((GridData) projectLbl.getLayoutData()).exclude = true;
					} else {
						if(bundle instanceof Module) {
							projectImg.setImage(OobiumPlugin.getImage("/icons/module.png"));
							projectImg.setToolTipText("Active Project (Module)");
						} else {
							projectImg.setImage(OobiumPlugin.getImage("/icons/plugin.gif"));
							projectImg.setToolTipText("Active Project (Bundle)");
						}
						projectLbl.setText(getLabelText(project));
						projectLbl.setToolTipText(getLabelToolTipText(project));
						projectImg.setVisible(true);
						projectLbl.setVisible(true);
						((GridData) projectImg.getLayoutData()).exclude = false;
						((GridData) projectLbl.getLayoutData()).exclude = false;
					}
					labelBar.getParent().layout();
				}
			});
		}
		if(root != null) {
			root.removeListener("bundle", bundleListener);
			root.setBundle(bundle);
			root.addListener("bundle", bundleListener);
		}
	}

	private void setProject(String str) {
		setProject((str != null) ? new File(str) : null);
	}
	
	public void setSelection(ISelection selection) {
		if(selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();
			if(sel instanceof IJavaElement) {
				sel = ((IJavaElement) sel).getResource();
			}
			if(sel instanceof IResource) {
				IProject iproject = ((IResource) sel).getProject();
				try {
					if(iproject.isOpen() && iproject.hasNature(OobiumNature.ID)) {
						File file = iproject.getLocation().toFile();
						Project project = OobiumPlugin.getWorkspace().getBundle(file);
						if(project == null) {
							project = OobiumPlugin.getWorkspace().load(file);
						}
						if(project != null) {
							if(project.isApplication()) {
								setApplication(file);
							} else if(project.isMigration()) {
								Migrator migrator = (Migrator) project;
								Module module = OobiumPlugin.getWorkspace().getModule(migrator.moduleName);
								if(module != null && module.isApplication()) {
									setApplication(module.file);
								}
							} else if(project.isTestSuite()) {
								TestSuite tests = (TestSuite) project;
								Module module = OobiumPlugin.getWorkspace().getModule(tests.moduleName);
								if(module != null && module.isApplication()) {
									setApplication(module.file);
								}
							}
							setProject(file);
						}
					}
				} catch(CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void setStatus(final Image image, final String message) {
		getSite().getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
				manager.setMessage(image, message);
			}
		});
	}
	
	private void updateConsoleFont() {
		String font = Platform.getPreferencesService().getString(WORKBENCH_PREFERENCES, TEXTFONT_KEY, null, null);
		if(font != null) {
			try {
				String[] sa = font.split("\\|");
				consolePage.getConsole().setFont(sa[1], (int) Float.parseFloat(sa[2]));
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

}
