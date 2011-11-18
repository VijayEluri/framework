package org.oobium.eclipse.wizards.model.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.eclipse.wizards.model.NewModelWizardPage;

public abstract class TableEditorForm extends Composite {

	protected final NewModelWizardPage page;

	protected Table table;
	protected ToolItem up;
	protected ToolItem dn;
	
	protected final List<String> readonly;
	protected Control editControl;

	public TableEditorForm(NewModelWizardPage dlg, Composite parent) {
		super(parent, SWT.NONE);
		
		this.page = dlg;
		this.readonly = new ArrayList<String>();

		createForm();
	}
	
	protected void clearTable() {
		for(TableItem item : table.getItems()) {
			item.dispose();
		}
	}
	
	protected abstract void createForm();
	
	protected TableItem addItem(String name, String type) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(new String[] { name, type });
		setItemUI(item);
		return item;
	}
		
	protected TableItem addItem(String name, String type, boolean readonly) {
		if(readonly) {
			this.readonly.add(name);
		}
		return addItem(name, type);
	}

	protected void cancelEdit() {
		if(editControl != null) {
			editControl.dispose();
			editControl = null;
		}
	}

	public void validate() {
		for(TableItem item : table.getItems()) {
			validate(item);
		}
	}
	
	protected abstract void validate(TableItem item);
		
	protected void createAttrNameEditor(final TableEditor editor, final TableItem item) {
		final Text text = new Text(item.getParent(), SWT.NONE);
		editControl = text;
		text.setText(item.getText(0));
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch(event.type) {
				case SWT.FocusOut:
					item.setText(0, text.getText());
					text.dispose();
					packColumns();
					update();
					break;
				case SWT.Modify:
					validate(item);
					break;
				case SWT.Traverse:
					switch(event.detail) {
					case SWT.TRAVERSE_ESCAPE:
						text.dispose();
						event.doit = false;
						update();
						break;
					case SWT.TRAVERSE_RETURN:
						event.doit = false;
					case SWT.TRAVERSE_TAB_NEXT:
						item.setText(0, text.getText());
						text.dispose();
						packColumns();
//						createAttrTypeEditor(editor, item);
						update();
						break;
					}
				}
			}
		};
		text.addListener(SWT.FocusOut, listener);
		text.addListener(SWT.Modify, listener);
		text.addListener(SWT.Traverse, listener);
		
		text.selectAll();
		text.setFocus();
		
		editor.setEditor(text, item, 0);
	}

	protected void moveSelectedItemDown(Table table, ToolItem up, ToolItem dn) {
		cancelEdit();
		int ix = table.getSelectionIndex();
		TableItem item = table.getItem(ix);
		String[] sa = new String[] { item.getText(0), item.getText(1) };
		item.dispose();
		ix++;
		item = new TableItem(table, SWT.NONE, ix);
		item.setText(sa);
		setItemUI(item);
		table.select(ix);
		if(ix == table.getItemCount()-1) {
			dn.setEnabled(false);
		}
		if(ix > 0) {
			up.setEnabled(true);
		}
	}

	protected void moveSelectedItemUp(Table table, ToolItem up, ToolItem dn) {
		cancelEdit();
		int ix = table.getSelectionIndex();
		TableItem item = table.getItem(ix);
		String[] sa = new String[] { item.getText(0), item.getText(1) };
		item.dispose();
		ix--;
		item = new TableItem(table, SWT.NONE, ix);
		item.setText(sa);
		setItemUI(item);
		table.select(ix);
		if(ix == 0) {
			up.setEnabled(false);
		}
		if(ix < table.getItemCount()-1) {
			dn.setEnabled(true);
		}
	}

	protected void packColumns() {
		for(TableColumn column : table.getColumns()) {
			column.pack();
			column.setWidth(column.getWidth() + 5);
		}
	}
	
	public abstract void setModel(Object model);
	
	protected void setItemUI(TableItem item) {
		if(readonly.contains(item.getText(0))) {
//			item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
			item.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		}
	}

	public abstract void updateModel(Object model);
	
}
