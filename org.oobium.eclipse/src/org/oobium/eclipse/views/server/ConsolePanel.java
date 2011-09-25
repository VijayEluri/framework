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

import static org.oobium.utils.StringUtils.blank;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.oobium.build.console.BuilderRootCommand;
import org.oobium.console.Console;
import org.oobium.console.ConsolePage;
import org.oobium.eclipse.OobiumPlugin;

public class ConsolePanel extends Composite {

	private ServerView view;
	private ConsolePage consolePage;

	public ConsolePanel(SashForm parent, ServerView view) {
		super(parent, 0);
		this.view = view;
		createContents();
	}

	private void createContents() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		setLayout(layout);

		consolePage = new ConsolePage(this, SWT.READ_ONLY);
		consolePage.getConsole().setRootCommand(new BuilderRootCommand(OobiumPlugin.getWorkspace()));
		consolePage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

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
					view.setStatus(OobiumPlugin.getImage("/icons/run.gif"), "command: " + href);
					break;
				}
				case SWT.MouseExit:
					view.setStatus(null, null);
					break;
				}
			}
		};
		consolePage.getConsole().defLink.addListener(SWT.Selection, listener);
		consolePage.getConsole().defLink.addListener(SWT.MouseEnter, listener);
		consolePage.getConsole().defLink.addListener(SWT.MouseExit, listener);
	}
	
	Console getConsole() {
		return consolePage.getConsole();
	}
	
	@Override
	public boolean setFocus() {
		return consolePage.setFocus();
	}
	
}
