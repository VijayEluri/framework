package org.oobium.eclipse.designer.editor.dialogs.model.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.oobium.build.model.ModelDefinition;
import org.oobium.eclipse.designer.editor.dialogs.model.ModelDialog;
import org.oobium.eclipse.designer.editor.dialogs.model.TableEditorForm;

public class IndexesForm extends TableEditorForm {

	public IndexesForm(ModelDialog dlg, Composite parent) {
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
		
		table = new Table(tcomp, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
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
	}

	@Override
	protected void validate(TableItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModel(Object model) {
		ModelDefinition def = (ModelDefinition) model;
		if(def != null) {
			for(String ix : def.getIndexes()) {
				addItem(ix);
				setReadOnly(ix, true);
			}
		}
		packColumns();
	}

	@Override
	public void updateModel(Object model) {
		// TODO Auto-generated method stub

	}

}
