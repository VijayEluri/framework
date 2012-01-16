package org.oobium.eclipse.designer.editor;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.oobium.app.http.Action;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;

public class DesignerToolBarViewsPopup {

	private final ToolItem toolItem;
	private final Module module;
	private final String model;
	
	private boolean viewsPopup;

	private Shell shell;
	
	private Map<String, Boolean> connections;
	private Map<String, Label> images;
	
	private String lastLabel;
	private Set<String> separators;

	
	public DesignerToolBarViewsPopup(ToolItem toolItem, Module module, String model) {
		this.toolItem = toolItem;
		this.module = module;
		this.model = model;

		viewsPopup = true;
		
		images = new LinkedHashMap<String, Label>();
		connections = new LinkedHashMap<String, Boolean>();
		separators = new HashSet<String>();
	}

	public void addRow(String label) {
		connections.put(label, null);
		lastLabel = label;
	}
	
	public void addRow(String label, boolean connected) {
		viewsPopup = false;
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
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellDeactivated(ShellEvent e) {
				shell.close();
			}
		});

		for(String label : connections.keySet()) {
			createRow(label);
		}
		
		if(viewsPopup) {
			createGenViewsRow();
		}
	}
	
	private void createGenViewsRow() {
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch(event.type) {
				case SWT.MouseDown:
					if(event.button == 1) {
						shell.close();
						if(viewsPopup) {
							boolean confirmed = module.findViews(model).isEmpty();
							if(!confirmed) {
								String title = "Generate Model Views";
								String message = "This will overwrite the existing default model views. Continue anyway?";
								confirmed = MessageDialog.openConfirm(shell, title, message);
							}
							if(confirmed) {
								Workspace workspace = OobiumPlugin.getWorkspace();
								module.createForModel(workspace, model, Module.VIEW);
								Eclipse.refresh(module.file, module.getViewsFolder(model));
							}
						}
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

		if(!connections.isEmpty()) {
			Label sep = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		
		Composite row = new Composite(shell, SWT.NONE);
		row.setBackgroundMode(SWT.INHERIT_FORCE);
		row.setLayout(new GridLayout(3, false));
		row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		row.addListener(SWT.MouseDown, listener);
		row.addListener(SWT.MouseEnter, listener);
		row.addListener(SWT.MouseExit, listener);

		Label lbl = new Label(row, SWT.NONE);
		lbl.setText("Generate Views");
		lbl.setToolTipText("Generate the default Views for the selected Model");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lbl.addListener(SWT.MouseDown, listener);
		lbl.addListener(SWT.MouseEnter, listener);
		lbl.addListener(SWT.MouseExit, listener);
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
							File file = module.getControllerFor(model);
							if(!file.isFile()) {
								module.createForModel(OobiumPlugin.getWorkspace(), module.getModel(model), Module.CONTROLLER);
								Eclipse.refreshProject(module.name);
							}
							int line = module.getLine(model, Action.valueOf(label));
							Eclipse.openFile(module.file, file, line);
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
//			img.setImage(connections.get(label) ? connectImg : disconnectImg);
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

	public void open() {
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
//			images.get(label).setImage(connected ? connectImg : disconnectImg);
			boolean newFile = false;
			boolean changed;
			if(connected) {
				File file  = module.getModel(model);
				if(!file.isFile()) {
					module.createModel(model, null);
					newFile = true;
				}
				file = module.getControllerFor(model);
				if(!file.isFile()) {
					module.createForModel(OobiumPlugin.getWorkspace(), module.getModel(model), Module.CONTROLLER);
					newFile = true;
				}
				changed = module.addModelRoute(model, Action.valueOf(label));
			} else {
				changed = module.removeModelRoute(model, Action.valueOf(label));
			}
			if(newFile) {
				Eclipse.refreshProject(module.name);
			}
			else if(changed) {
				Eclipse.refresh(module.file, module.activator);
			}
		}
	}
	
}
