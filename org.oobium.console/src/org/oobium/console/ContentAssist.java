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
package org.oobium.console;

import static java.lang.Math.*;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class ContentAssist {

	private Console console;
	private Shell shell;
	private Table table;

	private Shell helpShell;
	private Browser helpBrowser;
	

	ContentAssist(Console console) {
		this.console = console;
	}

	private void adjustSelection(int inc, boolean wrap) {
		int ix = table.getSelectionIndex() + inc;
		if(ix < 0 || ix >= table.getItemCount()) {
			if(inc < 0) {
				ix = wrap ? (table.getItemCount() - 1) : 0;
			} else {
				ix = wrap ? 0 : (table.getItemCount() - 1);
			}
		}
		table.setSelection(ix);
		handleSelectionChange();
	}
	
	void close() {
		if(console != null) {
			console.canvas.redraw();
		}
		
		if(helpShell != null && !helpShell.isDisposed()) {
			helpShell.dispose();
		}
		helpShell = null;
		helpBrowser = null;

		shell.dispose();
		shell = null;
		
		table = null;
		
		console.contentAssist = null;
		console = null;
	}
	
	void execute() {
		console.complete(getSelection().getName());
		close();
	}
	
	private void fillTable(Suggestion[] suggestions) {
		for(TableItem item : table.getItems()) {
			item.dispose();
		}
		Arrays.sort(suggestions);
		for(int i = 0; i < suggestions.length; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			if(suggestions[i].isDefault()) {
				item.setText(0, "*");
			}
			item.setText(1, suggestions[i].getName().trim());
			item.setData(suggestions[i]);
		}
		table.select(0);
	}
	
	private Suggestion getSelection() {
		return (Suggestion) table.getSelection()[0].getData();
	}
	
	void handleEvent(int type) {
		switch(type) {
		case SWT.ARROW_DOWN:
			adjustSelection(1, true);
			break;
		case SWT.ARROW_UP:
			adjustSelection(-1, true);
			break;
		case SWT.END:
			table.setSelection(table.getItemCount() - 1);
			handleSelectionChange();
			break;
		case SWT.HOME:
			table.setSelection(0);
			handleSelectionChange();
			break;
		case SWT.PAGE_DOWN:
			adjustSelection(10, false);
			break;
		case SWT.PAGE_UP:
			adjustSelection(-10, false);
			break;
		}
	}
	
	private void handleSelectionChange() {
		if(table.getSelectionCount() > 0) {
			String help = ((Suggestion) table.getSelection()[0].getData()).getHtmlDescription();
			setHelp(help);
		} else {
			setHelp(null);
		}
	}

	boolean hasFocus() {
		if(isDisposed()) {
			return false;
		}
		return table == table.getDisplay().getFocusControl();
	}
	
	boolean isDisposed() {
		return shell != null && shell.isDisposed();
	}

	boolean isOpen() {
		return shell != null;
	}
	
	void open(Suggestion[] suggestions) {
		shell = new Shell(console.getShell(), SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
		shell.setLayout(new FillLayout());

		table = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				execute();
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSelectionChange();
			}
		});
		
		TableColumn col1 = new TableColumn(table, SWT.NONE);
		TableColumn col2 = new TableColumn(table, SWT.NONE);
		TableColumn col3 = new TableColumn(table, SWT.NONE);

		fillTable(suggestions);
		
		col1.pack();
		col2.pack();
		col3.pack();

		Rectangle bounds = console.getMonitor().getBounds();
		
		Point location = console.toDisplay(console.getCaretLocation());
		
		int height = (table.getItemHeight() * min(10, table.getItemCount())) + 5;
		Point size = shell.computeSize(-1, height);
		
		if((location.y + console.getCharHeight() + size.y) > (bounds.y + bounds.height)) {
			location.y -= size.y;
		} else {
			location.y += console.getCharHeight();
		}
		
		shell.setBounds(location.x, location.y, size.x, size.y);
		shell.setVisible(true);

		shell.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				handleSelectionChange();
			}
		});
	}
	
	private void setHelp(String help) {
		if(help != null && help.length() > 0) {
			if(helpShell == null) {
				helpShell = new Shell(console.getShell(), SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
				helpShell.setLayout(new FillLayout());
				helpBrowser = new Browser(helpShell, SWT.NONE);
			}
			helpBrowser.setText("<html><body style=\"background-color:#F5F5B5\">" + help + "</body></html>");
			Rectangle r = shell.getBounds();
			helpShell.setBounds(r.x+r.width, r.y, 300, r.height);
			helpShell.setVisible(true);
		} else {
			if(helpShell != null) {
				helpShell.setVisible(false);
			}
		}
	}
	
}
