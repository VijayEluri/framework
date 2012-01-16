package org.oobium.eclipse.designer.editor.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class EndpointDialog extends SimpleDialog {

	private String field;
	private boolean hasMany;
	
	@Override
	protected boolean canFinish() {
		return field != null && field.length() > 0;
	}

	@Override
	protected void createForm(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText("Field: ");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		final Text source = new Text(parent, SWT.BORDER);
		if(field != null) {
			source.setText(field);
		}
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.minimumWidth = 150;
		source.setLayoutData(data);
		source.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				field = source.getText();
				setCanFinish(field.length() > 0);
			}
		});
		source.selectAll();
		
		new Label(parent, SWT.NONE);
		
		Button b = new Button(parent, SWT.CHECK);
		b.setText("has many");
		b.setSelection(hasMany);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hasMany = ((Button) e.widget).getSelection();
			}
		});
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
	}

	public String getField() {
		return field;
	}
	
	public boolean getHasMany() {
		return hasMany;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public void setHasMany(boolean hasMany) {
		this.hasMany = hasMany;
	}
	
}
