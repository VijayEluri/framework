package org.oobium.eclipse.designer.editor.dialogs;

import static org.oobium.utils.StringUtils.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.oobium.eclipse.designer.editor.models.Connection;

public class ConnectionDialog extends SimpleDialog {

	public enum ConnectionType {
		ONE_TO_NONE,
		ONE_TO_ONE,
		ONE_TO_MANY,
		MANY_TO_NONE,
		MANY_TO_ONE,
		MANY_TO_MANY
	}
	
	private String sourceField;
	private String targetField;
	private ConnectionType type;
	
	private Combo types;
	
	
	public ConnectionDialog(String sourceField) {
		setText("New Connection");
		this.sourceField = sourceField;
	}

	public ConnectionDialog(Connection connection) {
		setText("Edit Connection");
		sourceField = connection.getSourceField();
		targetField = connection.getTargetField();
		boolean sourceHasMany = connection.getSourceModel().getDefinition().hasMany(sourceField);
		if(blank(targetField)) {
			if(sourceHasMany) {
				type = ConnectionType.MANY_TO_NONE;
			} else {
				type = ConnectionType.ONE_TO_NONE;
			}
		} else {
			boolean targetHasMany = connection.getTargetModel().getDefinition().hasMany(targetField);
			if(sourceHasMany) {
				if(targetHasMany) {
					type = ConnectionType.MANY_TO_MANY;
				} else {
					type = ConnectionType.MANY_TO_ONE;
				}
			} else {
				if(targetHasMany) {
					type = ConnectionType.ONE_TO_MANY;
				} else {
					type = ConnectionType.ONE_TO_ONE;
				}
			}
		}
	}
	
	@Override
	protected boolean canFinish() {
		return sourceField != null && sourceField.length() > 0;
	}
	
	@Override
	protected void createForm(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText("Source Field: ");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		final Text source = new Text(parent, SWT.BORDER);
		if(sourceField != null) {
			source.setText(sourceField);
		}
		source.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		source.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				sourceField = source.getText();
				setCanFinish(sourceField.length() > 0);
			}
		});
		source.selectAll();
		
		lbl = new Label(parent, SWT.NONE);
		lbl.setText("Target Field: ");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		final Text target = new Text(parent, SWT.BORDER);
		target.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		target.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String old = targetField;
				targetField = target.getText();
				if(blank(old) != blank(targetField)) {
					setTypes();
				}
			}
		});
		
		lbl = new Label(parent, SWT.NONE);
		lbl.setText("Type: ");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		types = new Combo(parent, SWT.DROP_DOWN | SWT.SINGLE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.verticalIndent = 5;
		types.setLayoutData(data);
		types.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setType();
			}
		});
		
		setTypes();
		
		if(type != null) {
			if(blank(targetField)) {
				switch(type) {
				case ONE_TO_NONE: 	types.select(0); break;
				case MANY_TO_NONE: 	types.select(1); break;
				default: throw new IllegalArgumentException();
				}
			} else {
				switch(type) {
				case ONE_TO_ONE: 	types.select(0); break;
				case ONE_TO_MANY: 	types.select(1); break;
				case MANY_TO_ONE: 	types.select(2); break;
				case MANY_TO_MANY: 	types.select(3); break;
				default: throw new IllegalArgumentException();
				}
			}
		}
	}

	public String getSourceField() {
		return sourceField;
	}
	
	public void setSourceHasMany(boolean hasMany) {
//		this.sourceHasMany = hasMany;
	}
	
	public void setTargetHasMany(boolean hasMany) {
//		this.targetHasMany = hasMany;
	}
	
	public boolean getSourceHasMany() {
		switch(type) {
		case MANY_TO_MANY:
		case MANY_TO_NONE:
		case MANY_TO_ONE:
			return true;
		}
		return false;
	}
	
	public String getTargetField() {
		return targetField;
	}

	public boolean getTargetHasMany() {
		switch(type) {
		case MANY_TO_MANY:
		case ONE_TO_MANY:
			return true;
		}
		return false;
	}
	
	public ConnectionType getType() {
		return type;
	}
	
	private void setType() {
		if(blank(targetField)) {
			switch(types.getSelectionIndex()) {
			case 0: type = ConnectionType.ONE_TO_NONE; break;
			case 1: type = ConnectionType.MANY_TO_NONE; break;
			default: throw new IllegalArgumentException();
			}
		} else {
			switch(types.getSelectionIndex()) {
			case 0: type = ConnectionType.ONE_TO_ONE; break;
			case 1: type = ConnectionType.ONE_TO_MANY; break;
			case 2: type = ConnectionType.MANY_TO_ONE; break;
			case 3: type = ConnectionType.MANY_TO_MANY; break;
			default: throw new IllegalArgumentException();
			}
		}
	}
	
	private void setTypes() {
		types.removeAll();
		if(blank(targetField)) {
			types.add(titleize(ConnectionType.ONE_TO_NONE.name()));
			types.add(titleize(ConnectionType.MANY_TO_NONE.name()));
		} else {
			types.add(titleize(ConnectionType.ONE_TO_ONE.name()));
			types.add(titleize(ConnectionType.ONE_TO_MANY.name()));
			types.add(titleize(ConnectionType.MANY_TO_ONE.name()));
			types.add(titleize(ConnectionType.MANY_TO_MANY.name()));
		}
		types.select(0);
		setType();
	}
	
}
