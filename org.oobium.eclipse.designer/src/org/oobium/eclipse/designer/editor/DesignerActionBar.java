package org.oobium.eclipse.designer.editor;

import java.util.Iterator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
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
	
	private DefaultEditDomain editDomain;
	
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
		ToolBar tb = new ToolBar(parent, SWT.BORDER);
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
				super.widgetSelected(e);
			}
		});

		new ToolItem(tb, SWT.SEPARATOR);

		ToolItem item;
		Combo combo;
		
		item = new ToolItem(tb, SWT.PUSH);
		item.setText("Controller");

		combo = new Combo(tb, SWT.DROP_DOWN | SWT.READ_ONLY);
		for(String s : new String[] { "create", "update", "destroy", "show", "showAll", "showEdit", "showNew" }) {
			combo.add(s);
		}
		combo.setBackground(tb.getBackground());
		item = new ToolItem(tb, SWT.SEPARATOR);
		item.setControl(combo);
		item.setWidth(combo.computeSize(-1, -1).x);


		item = new ToolItem(tb, SWT.PUSH);
		item.setText("Views");

		combo = new Combo(tb, SWT.DROP_DOWN | SWT.READ_ONLY);
		for(String s : new String[] { "AlarmForm", "ShowAlarm", "ShowAllAlarms", "ShowEditAlarm", "ShowNewAlarm" }) {
			combo.add(s);
		}
		combo.setBackground(tb.getBackground());
		item = new ToolItem(tb, SWT.SEPARATOR);
		item.setControl(combo);
		item.setWidth(combo.computeSize(-1, -1).x);


		item = new ToolItem(tb, SWT.PUSH);
		item.setText("Routes");

		combo = new Combo(tb, SWT.DROP_DOWN | SWT.READ_ONLY);
		for(String s : new String[] { "create", "retrieve", "retrieveAll", "update", "destroy" }) {
			combo.add(s);
		}
		combo.setBackground(tb.getBackground());
		item = new ToolItem(tb, SWT.SEPARATOR);
		item.setControl(combo);
		item.setWidth(combo.computeSize(-1, -1).x);
	}

	public void dispose() {
		pointerImage.dispose();
		addModelImage.dispose();
		addConnectionImage.dispose();
		delImage.dispose();
	}

	public void hookViewer(GraphicalViewer viewer) {
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
		if(selection instanceof StructuredSelection) {
			if(selection.isEmpty()) {
				delete.setEnabled(false);
			} else {
				StructuredSelection ss = (StructuredSelection) selection;
				for(Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
					Object o = iter.next();
					if(o instanceof ModelPart || o instanceof ConnectionPart) {
						continue;
					}
					delete.setEnabled(false);
					return;
				}
				delete.setEnabled(true);
			}
		}
		delete.setEnabled(false);
	}
	
}
