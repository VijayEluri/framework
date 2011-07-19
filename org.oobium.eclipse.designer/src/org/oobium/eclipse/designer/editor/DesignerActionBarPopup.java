package org.oobium.eclipse.designer.editor;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.app.http.Action;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;

public class DesignerActionBarPopup {

	private final ToolItem toolItem;
	private final Module module;
	private final String model;
	
	private boolean viewsPopup;

	private Shell shell;
	private Image connectImg;
	private Image disconnectImg;
	
	private Map<String, Boolean> connections;
	private Map<String, Label> images;
	
	private String lastLabel;
	private Set<String> separators;

	
	public DesignerActionBarPopup(ToolItem toolItem, Module module, String model) {
		this.toolItem = toolItem;
		this.module = module;
		this.model = model;

		images = new LinkedHashMap<String, Label>();
		connections = new LinkedHashMap<String, Boolean>();
		separators = new HashSet<String>();
	}

	public void addRow(String label) {
		viewsPopup = true;
		connections.put(label, null);
		lastLabel = label;
	}
	
	public void addRow(String label, boolean connected) {
		connections.put(label, connected);
		lastLabel = label;
	}

	public void addSeparator() {
		separators.add(lastLabel);
	}
	
	private void createContents() {
		shell = new Shell(toolItem.getParent().getShell(), SWT.TOOL | SWT.NO_FOCUS);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		shell.setLayout(layout);
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeResources();
			}
		});
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellDeactivated(ShellEvent e) {
				shell.close();
			}
		});

		for(String label : connections.keySet()) {
			createRow(label);
		}
	}
	
	private void createResources() {
		connectImg = new Image(toolItem.getDisplay(), getClass().getResourceAsStream("plug-connect.png"));
		disconnectImg = new Image(toolItem.getDisplay(), getClass().getResourceAsStream("plug-disconnect.png"));
	}

	private void createRow(final String label) {
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch(event.type) {
				case SWT.MouseDown:
					if(event.button == 1) {
						if(viewsPopup) {
							Eclipse.openFile(module.file, module.getView(model, label));
						}
						else {
							int line = module.getLine(model, Action.valueOf(label));
							Eclipse.openFile(module.file, module.getControllerFor(model), line);
						}
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
		row.setLayout(new GridLayout(3, false));
		row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		row.addListener(SWT.MouseDown, listener);
		row.addListener(SWT.MouseEnter, listener);
		row.addListener(SWT.MouseExit, listener);

		if(!viewsPopup) {
			Label img = new Label(row, SWT.NONE);
			img.setImage(connections.get(label) ? connectImg : disconnectImg);
			img.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			img.addListener(SWT.MouseEnter, listener);
			img.addListener(SWT.MouseExit, listener);
			img.addListener(SWT.MouseDown, new Listener() {
				@Override
				public void handleEvent(Event event) {
					updateStatus(label, !connections.get(label));
				}
			});
			images.put(label, img);
		}
		
		Label lbl = new Label(row, SWT.NONE);
		lbl.setText(label);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lbl.addListener(SWT.MouseDown, listener);
		lbl.addListener(SWT.MouseEnter, listener);
		lbl.addListener(SWT.MouseExit, listener);
		
		if(separators.contains(label)) {
			Label sep = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
	}

	private void disposeResources() {
		if(connectImg != null) {
			connectImg.dispose();
			disconnectImg.dispose();
		}
	}
	
	public void open() {
		createResources();
		createContents();
		
		shell.pack();

		Rectangle r = toolItem.getBounds();
		shell.setLocation(toolItem.getParent().toDisplay(r.x, r.y + r.height));
		
		shell.open();
	}
	
	private void updateStatus(String label, boolean connected) {
		boolean old = connections.get(label);
		if(old != connected) {
			connections.put(label, connected);
			images.get(label).setImage(connected ? connectImg : disconnectImg);
		}
	}
	
}
