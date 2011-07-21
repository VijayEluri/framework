package org.oobium.eclipse.designer.editor;

import static org.oobium.utils.StringUtils.plural;

import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.app.http.Action;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionDeleteCommand;
import org.oobium.eclipse.designer.editor.models.commands.ModelDeleteCommand;
import org.oobium.eclipse.designer.editor.parts.ConnectionPart;
import org.oobium.eclipse.designer.editor.parts.ModelPart;
import org.oobium.eclipse.designer.editor.tools.ConnectionCreateTool;
import org.oobium.eclipse.designer.editor.tools.ModelCreateTool;

public class DesignerActionBar {

	private Image pointerImage;
	private Image addModelImage;
	private Image addConnectionImage;
	private Image delImage;
	
	private ToolItem pointer;
	private ToolItem addModel;
	private ToolItem addConnection;
	private ToolItem delete;
	private ToolItem controller;
	private ToolItem views;
	
	private DefaultEditDomain editDomain;
	private ModelPart activeModel;

	private GraphicalViewer viewer;
	
	public DesignerActionBar(Composite parent) {
		createResources(parent);
		createToolBar(parent);
	}

	private void createResources(Composite parent) {
		pointerImage = new Image(parent.getDisplay(), getClass().getResourceAsStream("cursor.png"));
		addModelImage = new Image(parent.getDisplay(), getClass().getResourceAsStream("add_model.png"));
		addConnectionImage = new Image(parent.getDisplay(), getClass().getResourceAsStream("add_connection.png"));
		delImage = new Image(parent.getDisplay(), getClass().getResourceAsStream("delete.png"));
	}
	
	public void createToolBar(Composite parent) {
		final ToolBar tb = new ToolBar(parent, SWT.BORDER);
		tb.setBackground(ColorConstants.listBackground);
		tb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		pointer = new ToolItem(tb, SWT.PUSH);
		pointer.setToolTipText("Selection Tool");
		pointer.setImage(pointerImage);
		pointer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editDomain.setActiveTool(editDomain.getDefaultTool());
			}
		});

		new ToolItem(tb, SWT.SEPARATOR);
		
		addModel = new ToolItem(tb, SWT.PUSH);
		addModel.setToolTipText("Add a new Model");
		addModel.setImage(addModelImage);
		addModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editDomain.setActiveTool(new ModelCreateTool());
			}
		});

		addConnection = new ToolItem(tb, SWT.PUSH);
		addConnection.setToolTipText("Add a new Connection");
		addConnection.setImage(addConnectionImage);
		addConnection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editDomain.setActiveTool(new ConnectionCreateTool());
			}
		});

		new ToolItem(tb, SWT.SEPARATOR);
		
		delete = new ToolItem(tb, SWT.PUSH);
		delete.setToolTipText("Delete the selected item(s)");
		delete.setImage(delImage);
		delete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = viewer.getSelection();
				if(!selection.isEmpty() && selection instanceof StructuredSelection) {
					CompoundCommand cc = new CompoundCommand();
					StructuredSelection ss = (StructuredSelection) selection;
					for(Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
						Object o = iter.next();
						if(o instanceof ModelPart) {
							cc.add(new ModelDeleteCommand(((ModelPart) o).getModel()));
							continue;
						}
						if(o instanceof ConnectionPart) {
							cc.add(new ConnectionDeleteCommand(((ConnectionPart) o).getModel()));
							continue;
						}
						return; // can't do a delete, just exit
					}
					editDomain.getCommandStack().execute(cc);
				}
			}
		});

		new ToolItem(tb, SWT.SEPARATOR);

		controller = new ToolItem(tb, SWT.PUSH);
		controller.setText("Controller");
		controller.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Module module = activeModel.getModel().getModuleElement().getModule();
				String model = activeModel.getModel().getName();
				DesignerActionBarPopup pop = new DesignerActionBarPopup(controller, module, model);
				for(String action : new String[] { "create", "update", "destroy", "show", "showAll" }) {
					pop.addRow(action, module.isRouted(model, Action.valueOf(action)));
				}
				pop.addSeparator();
				for(String action : new String[] { "showEdit", "showNew" }) {
					pop.addRow(action, module.isRouted(model, Action.valueOf(action)));
				}
				pop.open();
			}
		});
		
		new ToolItem(tb, SWT.SEPARATOR);

		views = new ToolItem(tb, SWT.PUSH);
		views.setText("Views");
		views.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Module module = activeModel.getModel().getModuleElement().getModule();
				String model = activeModel.getModel().getName();
				DesignerActionBarPopup pop = new DesignerActionBarPopup(views, module, model);
				for(String label : new String[] { "Show"+model, "ShowAll"+plural(model), "ShowEdit"+model, "ShowNew"+model, model+"Form" }) {
					pop.addRow(label);
				}
				pop.open();
			}
		});
	}

	public void dispose() {
		pointerImage.dispose();
		addModelImage.dispose();
		addConnectionImage.dispose();
		delImage.dispose();
	}

	public void setViewer(GraphicalViewer viewer) {
		this.viewer = viewer;
		updateSelection(viewer.getSelection());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateSelection(event.getSelection());
			}
		});
	}

	public void setEditDomain(DefaultEditDomain editDomain) {
		this.editDomain = editDomain;
	}

	private void updateSelection(ISelection selection) {
		activeModel = null;
		controller.setEnabled(false);
		views.setEnabled(false);
		if(selection instanceof StructuredSelection) {
			if(selection.isEmpty()) {
				delete.setEnabled(false);
			} else {
				StructuredSelection ss = (StructuredSelection) selection;
				for(Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
					Object o = iter.next();
					if(o instanceof ModelPart) {
						if(ss.size() == 1) {
							activeModel = (ModelPart) o;
							controller.setEnabled(true);
							views.setEnabled(true);
						}
						continue;
					}
					if(o instanceof ConnectionPart) {
						continue;
					}
					delete.setEnabled(false);
					return;
				}
				delete.setEnabled(true);
				return;
			}
		}
		delete.setEnabled(false);
	}
	
}
