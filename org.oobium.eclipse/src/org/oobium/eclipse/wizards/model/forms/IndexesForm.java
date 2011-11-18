package org.oobium.eclipse.wizards.model.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.wizards.model.NewModelWizardPage;

public class IndexesForm extends TableEditorForm {

	public IndexesForm(NewModelWizardPage dlg, Composite parent) {
		super(dlg, parent);
	}

	@Override
	protected void createForm() {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		Composite tcomp = new Composite(this, SWT.BORDER);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		tcomp.setLayout(layout);
		tcomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		ToolBar tb = new ToolBar(tcomp, SWT.BORDER);
		tb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		table = new Table(tcomp, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 300;
		data.heightHint = 150;
		table.setLayoutData(data);
		
		for(String cname : new String[] { "Name", "Type" }) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(cname);
		}

		final TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.LEFT;
		
		table.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final TableItem item = table.getItem(new Point(event.x, event.y));
				if(item == null) return;
				if(readonly.contains(item.getText(0))) return;

				if(item.getBounds(0).contains(event.x, event.y)) {
					createAttrNameEditor(editor, item);
				}
				else if(item.getBounds(1).contains(event.x, event.y)) {
//					createAttrTypeEditor(editor, item);
				}
			}
		});

		packColumns();
		
		ToolItem item = new ToolItem(tb, SWT.PUSH);
		item.setToolTipText("Add a new attribute");
		item.setImage(OobiumPlugin.getImage(OobiumPlugin.IMG_ADD));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelEdit();
				TableItem item = addItem("", "");
				createAttrNameEditor(editor, item);
			}
		});

		final ToolItem del = new ToolItem(tb, SWT.PUSH);
		item.setToolTipText("Remove selected attribute");
		del.setImage(OobiumPlugin.getImage(OobiumPlugin.IMG_DELETE));
		del.setEnabled(false);
		del.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelEdit();
				TableItem[] ia = table.getSelection();
				for(TableItem ti : ia) {
					ti.dispose();
				}
			}
		});

		up = new ToolItem(tb, SWT.PUSH);
		up.setToolTipText("Move selected attribute up one position");
		up.setImage(OobiumPlugin.getImage(OobiumPlugin.IMG_ARROW_UP));
		up.setEnabled(false);
		up.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveSelectedItemUp(table, up, dn);
			}
		});

		dn = new ToolItem(tb, SWT.PUSH);
		dn.setToolTipText("Move selected attribute down one position");
		dn.setImage(OobiumPlugin.getImage(OobiumPlugin.IMG_ARROW_DOWN));
		dn.setEnabled(false);
		dn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveSelectedItemDown(table, up, dn);
			}
		});

		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(table.getSelectionCount() > 0) {
					boolean enabled = true;
					if(!readonly.isEmpty()) {
						for(TableItem item : table.getSelection()) {
							if(readonly.contains(item.getText(0))) {
								enabled = false;
								break;
							}
						}
					}
					del.setEnabled(enabled);
				} else {
					del.setEnabled(false);
				}
				up.setEnabled(table.getSelectionCount() == 1 && table.getSelectionIndex() > 0);
				dn.setEnabled(table.getSelectionCount() == 1 && table.getSelectionIndex() < table.getItemCount()-1);
			}
		});
	}

	@Override
	protected void validate(TableItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModel(Object model) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateModel(Object model) {
		// TODO Auto-generated method stub

	}

}
