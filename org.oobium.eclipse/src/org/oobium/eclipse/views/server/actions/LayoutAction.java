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
package org.oobium.eclipse.views.server.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.oobium.eclipse.views.server.ServerView;
import org.oobium.eclipse.views.server.ServerView.Layout;

public class LayoutAction extends Action implements IMenuCreator {

	private Menu cmenu;
	private Menu mmenu;
	private ServerView view;
	private SelectionAdapter listener;
	
	public LayoutAction(final ServerView view) {
		super("Layout", AS_DROP_DOWN_MENU);
		setToolTipText("Layout");
		this.view = view;
		listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Layout layout = Layout.valueOf(((MenuItem) e.widget).getText());
				view.setLayout(layout);
			}
		};
		setMenuCreator(this);
	}

	private void addItems(Menu menu) {
		MenuItem item;
		item = new MenuItem(menu, SWT.RADIO);
		item.setText("Horizontal");
		item.addSelectionListener(listener);
		item.setSelection(view.getLayout() == Layout.Horizontal);
		
		item = new MenuItem(menu, SWT.RADIO);
		item.setText("Vertical");
		item.addSelectionListener(listener);
		item.setSelection(view.getLayout() == Layout.Vertical);
	}
	
	@Override
	public Menu getMenu(Control parent) {
		if(cmenu == null) {
			cmenu = new Menu(parent);
			addItems(cmenu);
		}
		return cmenu;
	}
	
	@Override
	public Menu getMenu(Menu parent) {
		if(mmenu == null) {
			mmenu = new Menu(parent);
			addItems(mmenu);
		}
		return mmenu;
	}

	@Override
	public void dispose() {
		if(cmenu != null) cmenu.dispose();
		if(mmenu != null) mmenu.dispose();
	}
	
}
