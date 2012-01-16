package org.oobium.eclipse.designer.views.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.dialogs.CreateModelDialog;
import org.oobium.eclipse.designer.manager.DataService;
import org.oobium.eclipse.designer.manager.DataServiceEvent;
import org.oobium.eclipse.designer.manager.DataServiceListener;
import org.oobium.eclipse.designer.manager.DataServiceManager;
import org.oobium.eclipse.designer.views.IDataServiceView;
import org.oobium.eclipse.designer.views.actions.ConnectAction;
import org.oobium.eclipse.designer.views.data.actions.ResetAction;
import org.oobium.eclipse.designer.views.data.actions.SearchAction;
import org.oobium.persist.Model;
import org.oobium.utils.StringUtils;
import org.oobium.utils.Config.Mode;

public class DataView extends ViewPart implements DataServiceListener, IDataServiceView, ISelectionChangedListener {

	private ConnectAction connectAction;
	private ResetAction resetAction;
	private SearchAction searchAction;
	
	private TableViewer viewer;
	private ToolItem modelItem;
	private ToolItem createItem;
	private ToolItem deleteItem;
	private ToolItem refreshItem;

	private DataService service;
	private String modelName;
	private String modelType;

	public void connect(String service, Mode mode) {
		disconnect();
		this.service = DataServiceManager.instance().getService(service, mode);
		this.service.connect(this);
		this.service.addServiceListener(this);
		updateActions();
		setTitleToolTip(this.service.getLocation());
	}
	
	public void disconnect() {
		if(service != null) {
			service.removeServiceListener(this);
			service.disconnect(this);
			service = null;
		}
		updateActions();
		setTitleToolTip("");
	}

	private void createActions() {
		connectAction = new ConnectAction(this, false);
		resetAction = new ResetAction();
		searchAction = new SearchAction(this);
	}

	private void createMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();

		manager.add(resetAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void createTableToolBar(Composite parent) {
		ToolBar tbLeft = new ToolBar(parent, SWT.BORDER | SWT.FLAT);
		tbLeft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		modelItem = new ToolItem(tbLeft, SWT.PUSH);
		modelItem.setText("Model Type");
		modelItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(service != null) {
					List<String> names = service.getModelNames();
					if(!names.isEmpty()) {
						ModelTypePopup pop = new ModelTypePopup(DataView.this, modelItem);
						for(String type : names) {
							pop.addRow(type);
						}
						pop.open();
					}
				}
			}
		});
		
		new ToolItem(tbLeft, SWT.SEPARATOR);

		createItem = new ToolItem(tbLeft, SWT.PUSH);
		createItem.setImage(DesignerPlugin.getImage("/icons/add.png"));
		createItem.setToolTipText("Create a new Model");
		createItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ModelDefinition def = service.getModelDefinition(modelName);
				CreateModelDialog dlg = new CreateModelDialog(getSite().getShell(), def);
				if(dlg.open() == Dialog.OK) {
					Model model = service.newModel(modelType);
					model.putAll(dlg.getData());
					try {
						service.create(model);
						updateTable();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		new ToolItem(tbLeft, SWT.SEPARATOR);

		deleteItem = new ToolItem(tbLeft, SWT.PUSH);
		deleteItem.setImage(DesignerPlugin.getImage("/icons/delete.png"));
		deleteItem.setToolTipText("Delete the selected Model");
		deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				String message;
				if(sel.size() == 1) {
					message = "Are you sure you want to delete the selected Model?";
				} else {
					message = "Are you sure you want to delete the " + sel.size() + " selected Models?";
				}
				if(MessageDialog.openQuestion(getSite().getShell(), "Delete Model?", message)) {
					try {
						service.destroy(Arrays.asList(sel.toArray()).toArray(new Model[sel.size()]));
						updateTable();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		
		ToolBar tbRight = new ToolBar(parent, SWT.BORDER | SWT.FLAT);
		tbRight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		refreshItem = new ToolItem(tbRight, SWT.PUSH);
		refreshItem.setImage(DesignerPlugin.getImage("/icons/refresh.gif"));
		refreshItem.setToolTipText("Reload the Models");
		refreshItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				load(modelName);
			}
		});
	}
	
	private void createTable(Composite parent) {
		Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		viewer = new TableViewer(table);
		viewer.setContentProvider(new DataContentProvider());
		viewer.setLabelProvider(new DataLabelProvider(viewer));
		viewer.addSelectionChangedListener(this);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);
		
		createTableToolBar(parent);
		createTable(parent);
		
		createActions();
		createMenu();
		createToolBar();
		
		updateActions();
	}

	private void createToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(connectAction);
		manager.add(new Separator());
		manager.add(searchAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	public Shell getShell() {
		return getSite().getShell();
	}
	
	public void load(String model) {
		if(service == null) {
			throw new IllegalStateException("service cannot be null");
		}
		setModel(model);
		if(service.connect(this)) {
			updateTable();
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		deleteItem.setEnabled(service != null && modelType != null && !viewer.getSelection().isEmpty());
	}
	
	@Override
	public void setFocus() {
		if(viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().setFocus();
		}
	}

	public void setModel(final String model) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				modelName = model;
				modelType = null;
				Table table = viewer.getTable();
				for(TableItem i : table.getItems()) {
					i.dispose();
				}
				for(TableColumn c : table.getColumns()) {
					c.dispose();
				}
				if(model != null && service != null) {
					modelItem.setText(StringUtils.plural(model));
					modelItem.getParent().layout(true);
					ModelDefinition def = service.getModelDefinition(model);
					if(def != null) {
						modelType = def.getCanonicalName();
						updateActions();
						
						List<String> properties = new ArrayList<String>();
						
						TableColumn c = new TableColumn(table, SWT.NONE);
						c.setText("id");
						c.pack();
						properties.add("id");
						List<String> columns = new ArrayList<String>();
						for(ModelAttribute a : def.getAttributes()) {
							columns.add(a.name());
						}
						for(ModelRelation r : def.getRelations()) {
							columns.add(r.name());
						}
						Collections.sort(columns);
						for(String name : columns) {
							c = new TableColumn(table, SWT.NONE);
							c.setText(name);
							if(def.hasAttribute(name)) {
								c.setImage(DesignerPlugin.getImage("/icons/attribute.gif"));
							}
							else if(def.hasOne(name)) {
								c.setImage(DesignerPlugin.getImage("/icons/has_one.gif"));
							}
							else {
								c.setImage(DesignerPlugin.getImage("/icons/has_many.gif"));
							}
							c.pack();
							properties.add(name);
						}
						
						viewer.setColumnProperties(properties.toArray(new String[properties.size()]));
					}
				}
			}
		});
	}

	@Override
	public void handleDataServiceEvent(DataServiceEvent event) {
		switch(event.type) {
		case Connected:
			service = event.service;
			setModel(modelName);
			updateTable();
			updateActions();
			break;
		case Disconnected:
			service = null;
			updateActions();
			break;
		}
	}

	protected void updateActions() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				connectAction.setEnabled(true);
				resetAction.setEnabled(service != null);
				searchAction.setEnabled(service != null);
				modelItem.setEnabled(service != null);
				createItem.setEnabled(service != null && modelType != null);
				deleteItem.setEnabled(service != null && modelType != null && !viewer.getSelection().isEmpty());
				refreshItem.setEnabled(service != null && modelName != null && modelType != null);
			}
		});
	}

	private void updateTable() {
		if(service != null && modelType != null) {
			try {
				final List<? extends Model> models = service.findAll(modelType);
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						viewer.setInput(models);
						for(TableColumn c : viewer.getTable().getColumns()) {
							c.pack();
						}
					}
				});
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
