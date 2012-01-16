package org.oobium.eclipse.designer.views.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

public class ModelTypePopup {

	private final DataView view;
	private final ToolItem toolItem;
	
	private Shell shell;
	
	private List<String> names;
	private String lastName;
	private Set<String> separators;

	
	public ModelTypePopup(DataView view, ToolItem toolItem) {
		this.view = view;
		this.toolItem = toolItem;

		names = new ArrayList<String>();
		separators = new HashSet<String>();
	}

	public void addRow(String name) {
		names.add(name);
		lastName = name;
	}
	
	public void addSeparator() {
		separators.add(lastName);
	}
	
	private void createContents() {
		shell = new Shell(toolItem.getParent().getShell(), SWT.TOOL | SWT.NO_FOCUS);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		shell.setLayout(layout);
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellDeactivated(ShellEvent e) {
				shell.close();
			}
		});

		for(String label : names) {
			createRow(label);
		}
	}
	
	private void createRow(final String name) {
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch(event.type) {
				case SWT.MouseDown:
					if(event.button == 1) {
						view.load(name);
						shell.close();
					}
					break;
				case SWT.MouseEnter:
					Color color = event.display.getSystemColor(SWT.COLOR_LIST_SELECTION);
					if(event.widget instanceof Label) {
						((Label) event.widget).getParent().setBackground(color);
					} else {
						((Composite) event.widget).setBackground(color);
					}
					break;
				case SWT.MouseExit:
					color = event.display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
					if(event.widget instanceof Label) {
						((Label) event.widget).getParent().setBackground(color);
					} else {
						((Composite) event.widget).setBackground(color);
					}
					break;
				}
			}
		};
		
		Composite row = new Composite(shell, SWT.NONE);
		row.setBackgroundMode(SWT.INHERIT_FORCE);
		row.setLayout(new GridLayout());
		row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		row.addListener(SWT.MouseDown, listener);
		row.addListener(SWT.MouseEnter, listener);
		row.addListener(SWT.MouseExit, listener);

		Label lbl = new Label(row, SWT.NONE);
		lbl.setText(name);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lbl.addListener(SWT.MouseDown, listener);
		lbl.addListener(SWT.MouseEnter, listener);
		lbl.addListener(SWT.MouseExit, listener);
		
		if(separators.contains(name)) {
			Label sep = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
	}

	public void open() {
		createContents();
		
		shell.pack();

		Rectangle r = toolItem.getBounds();
		shell.setLocation(toolItem.getParent().toDisplay(r.x, r.y + r.height));
		
		shell.open();
	}
	
}
