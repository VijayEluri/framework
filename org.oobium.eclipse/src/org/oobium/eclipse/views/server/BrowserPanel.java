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

import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.views.server.actions.browser.LaunchExternalBrowserAction;
import org.oobium.eclipse.views.server.actions.browser.RefreshPathsAction;

public class BrowserPanel extends Composite {

	private ServerView view;

	private String app;
	private Label appImg;
	private Label appLbl;
	private ToolItem appItem;
	
	private Combo locationBar;
	private RefreshPathsAction refreshPathsAction;
	private LaunchExternalBrowserAction launchExternalAction;
	
	
	public BrowserPanel(ServerView view) {
		super(view.getPanel(), 0);
		this.view = view;
		createContents();
	}
	
	public void createContents() {
		refreshPathsAction = new RefreshPathsAction(this);
		refreshPathsAction.setEnabled(false);

		launchExternalAction = new LaunchExternalBrowserAction(this);
		launchExternalAction.setEnabled(false);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		setLayout(layout);

		IContributionItem applicationItem = new ContributionItem() {
			@Override
			public void fill(ToolBar parent, int index) {
				appItem = new ToolItem(parent, SWT.SEPARATOR);
				Composite comp = new Composite(parent, SWT.NONE);
				GridLayout layout = new GridLayout(2, false);
				layout.marginWidth = 5;
				layout.marginHeight = 0;
				comp.setLayout(layout);

				appImg = new Label(comp, SWT.NONE);
				appImg.setImage(OobiumPlugin.getImage("/icons/application.png"));
				appImg.setToolTipText("Active Application");
				appImg.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

				appLbl = new Label(comp, SWT.NONE);
				appLbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
				if(app != null) {
					appLbl.setText(app);
				}

				appItem.setControl(comp);
				appItem.setWidth(comp.computeSize(-1, -1).x);
			}
		};
		
		IContributionItem locationBarItem = new ContributionItem() {
			@Override
			public void fill(ToolBar parent, int index) {
				ToolItem item = new ToolItem(parent, SWT.SEPARATOR);
				locationBar = new Combo(parent, SWT.DROP_DOWN);
				item.setControl(locationBar);
				item.setWidth(400);
				locationBar.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int len = locationBar.getText().length();
						locationBar.setSelection(new Point(len, len));
					}
				});
			}
		};
		
		ToolBarManager tmanager = new ToolBarManager();
		tmanager.add(applicationItem);
		tmanager.add(locationBarItem);
		tmanager.add(refreshPathsAction);
		tmanager.add(new Separator());
		tmanager.add(launchExternalAction);
		ToolBar tb = tmanager.createControl(this);
		tb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	void updateAppState(boolean isRunning) {
		refreshPathsAction.setEnabled(isRunning);
		launchExternalAction.setEnabled(isRunning);
	}
	
	public void launchExternalBrowser() {
		String url = locationBar.getText();
		url = url.startsWith("http://") ? url : ("http://" + url);
		Program.launch(url);
	}
	
	public void refreshPaths() {
		refreshPaths(false);
	}
	
	public void refreshPaths(final boolean load) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				String txt = locationBar.getText();
				List<String> paths = view.getPaths();
				locationBar.setItems(paths.toArray(new String[paths.size()]));
				locationBar.setText(txt);
				if(load) {
					if(txt.length() == 0) {
						for(int i = 0; i < locationBar.getItemCount(); i++) {
							String path = locationBar.getItem(i);
							if(path.indexOf('{') == -1) {
								locationBar.select(i);
								return;
							}
						}
					}
				}
			}
		});
	}
	
	public void setApplication(String name) {
		app = name;
		if(appLbl != null) {
			appLbl.setText(app);
			appItem.setWidth(appLbl.getParent().computeSize(-1, -1).x);
			layout(true, true);
		}
	}
	
	@Override
	public boolean setFocus() {
		return locationBar.setFocus();
	}
	
}
