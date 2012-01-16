package org.oobium.eclipse.designer.editor;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Module;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.commands.ConnectionDeleteCommand;
import org.oobium.eclipse.designer.editor.models.commands.ModelDeleteCommand;
import org.oobium.eclipse.designer.editor.parts.ConnectionPart;
import org.oobium.eclipse.designer.editor.parts.ModelPart;
import org.oobium.eclipse.designer.editor.parts.SitePart;
import org.oobium.eclipse.designer.editor.tools.ConnectionCreateTool;
import org.oobium.eclipse.designer.editor.tools.ModelCreateTool;

public class DesignerToolBar {

	private DefaultEditDomain editDomain;
	private ModelPart activeModel;
	private GraphicalViewer viewer;
	
	private ToolItem pointer;
	private ToolItem addModel;
	private ToolItem addConnection;
	private ToolItem delete;
	private ToolItem model;
	private ToolItem views;
	private ToolItem controller;
	private ToolItem grid;
	private ToolItem snap;
	private ToolItem zoomIn;
	private ToolItem zoomOut;
	private ToolItem zoomReset;
	
	public DesignerToolBar(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		createToolBar(comp);
	}

	public void createToolBar(Composite parent) {
		ToolBar tbLeft = new ToolBar(parent, SWT.BORDER);
		tbLeft.setBackground(ColorConstants.listBackground);
		tbLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		pointer = new ToolItem(tbLeft, SWT.PUSH);
		pointer.setToolTipText("Selection Tool");
		pointer.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_CURSOR));
		pointer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editDomain.setActiveTool(editDomain.getDefaultTool());
			}
		});

		new ToolItem(tbLeft, SWT.SEPARATOR);
		
		addModel = new ToolItem(tbLeft, SWT.PUSH);
		addModel.setToolTipText("Add a new Model");
		addModel.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ADD_MODEL));
		addModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editDomain.setActiveTool(new ModelCreateTool());
			}
		});

		addConnection = new ToolItem(tbLeft, SWT.PUSH);
		addConnection.setToolTipText("Add a new Connection");
		addConnection.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ADD_CONNECTION));
		addConnection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editDomain.setActiveTool(new ConnectionCreateTool());
			}
		});

		new ToolItem(tbLeft, SWT.SEPARATOR);
		
		delete = new ToolItem(tbLeft, SWT.PUSH);
		delete.setToolTipText("Delete the selected item(s)");
		delete.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_DELETE));
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

		new ToolItem(tbLeft, SWT.SEPARATOR);

		model = new ToolItem(tbLeft, SWT.PUSH);
		model.setText("M");
		model.setToolTipText("Open the Model");
		model.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Module module = activeModel.getModel().getModuleElement().getModule();
				String model = activeModel.getModel().getName();
				Eclipse.openFile(module.file, module.getModel(model));
			}
		});

		views = new ToolItem(tbLeft, SWT.PUSH);
		views.setText("V");
		views.setToolTipText("Select a Model View to open...");
		views.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Module module = activeModel.getModel().getModuleElement().getModule();
				String model = activeModel.getModel().getName();
				DesignerToolBarViewsPopup pop = new DesignerToolBarViewsPopup(views, module, model);
				File folder = module.getViewsFolder(model);
				if(folder.isDirectory()) {
					String[] names = folder.list();
					Arrays.sort(names);
					for(String name : names) {
						if(name.endsWith(".esp")) {
							pop.addRow(name.substring(0, name.length() - 4));
						}
					}
				}
				pop.open();
			}
		});

		controller = new ToolItem(tbLeft, SWT.PUSH);
		controller.setText("C");
		controller.setToolTipText("Open the Model's Controller");
		controller.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Module module = activeModel.getModel().getModuleElement().getModule();
				String model = activeModel.getModel().getName();
				File controller = module.getControllerFor(model);
				if(!controller.exists()) {
					module.createForModel(OobiumPlugin.getWorkspace(), model, Module.CONTROLLER);
				}
				Eclipse.openFile(module.file, controller);
			}
		});


		ToolBar tbRight = new ToolBar(parent, SWT.BORDER);
		tbRight.setBackground(ColorConstants.listBackground);
		tbRight.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

		grid = new ToolItem(tbRight, SWT.CHECK);
		grid.setToolTipText("Show Grid");
		grid.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_GRID));
		grid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, grid.getSelection());
			}
		});

		snap = new ToolItem(tbRight, SWT.CHECK);
		snap.setToolTipText("Snap to Grid");
		snap.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_SNAP_TO_GRID));
		snap.setSelection(true);
		snap.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RootEditPart root = viewer.getRootEditPart();
				for(Object child : root.getChildren()) {
					if(child instanceof SitePart) {
						((SitePart) child).setSnapToGrid(snap.getSelection());
					}
				}
			}
		});

		new ToolItem(tbRight, SWT.SEPARATOR);

		zoomIn = new ToolItem(tbRight, SWT.PUSH);
		zoomIn.setToolTipText("Zoom In");
		zoomIn.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ZOOM_IN));
		zoomIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getRoot().getZoomManager().zoomIn();
				zoomIn.setEnabled(getRoot().getZoomManager().canZoomIn());
				zoomOut.setEnabled(getRoot().getZoomManager().canZoomOut());
			}
		});

		zoomOut = new ToolItem(tbRight, SWT.PUSH);
		zoomOut.setToolTipText("Zoom Out");
		zoomOut.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ZOOM_OUT));
		zoomOut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getRoot().getZoomManager().zoomOut();
				zoomIn.setEnabled(getRoot().getZoomManager().canZoomIn());
				zoomOut.setEnabled(getRoot().getZoomManager().canZoomOut());
			}
		});

		zoomReset = new ToolItem(tbRight, SWT.PUSH);
		zoomReset.setToolTipText("Reset Zoom");
		zoomReset.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ZOOM_ACTUAL));
		zoomReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getRoot().getZoomManager().setZoom(1);
				zoomIn.setEnabled(getRoot().getZoomManager().canZoomIn());
				zoomOut.setEnabled(getRoot().getZoomManager().canZoomOut());
			}
		});
	}
	
	private ScalableFreeformRootEditPart getRoot() {
		return (ScalableFreeformRootEditPart) viewer.getRootEditPart();
	}

	public void setViewer(GraphicalViewer viewer) {
		this.viewer = viewer;
		List<?> children = viewer.getRootEditPart().getChildren();
		if(children != null && children.size() == 1) {
			boolean serviceModels = true;
			SitePart site = (SitePart) children.get(0);
			for(ApplicationElement app : site.getModel().getApplications()) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(app.getName());
				if(project.isOpen()) {
					serviceModels = false;
					break;
				}
			}
			if(serviceModels) {
				model.dispose();
				model = null;
				views.dispose();
				views = null;
				controller.dispose();
				controller = null;
			}
		}
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
		if(model != null) model.setEnabled(false);
		if(views != null) views.setEnabled(false);
		if(controller != null) controller.setEnabled(false);
		if(selection instanceof StructuredSelection) {
			if(selection.isEmpty()) {
				delete.setEnabled(false);
			} else {
				StructuredSelection ss = (StructuredSelection) selection;
				for(Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
					Object o = iter.next();
					if(o instanceof ModelPart) {
						if(model != null && ss.size() == 1) {
							activeModel = (ModelPart) o;
							Module module = activeModel.getModel().getModuleElement().getModule();
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(module.name);
							if(project.isOpen()) {
								String modelName = activeModel.getModel().getName();
								boolean hasModel = module.getModel(modelName).exists();
								model.setEnabled(hasModel);
								views.setEnabled(hasModel);
								controller.setEnabled(hasModel);
							}
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
