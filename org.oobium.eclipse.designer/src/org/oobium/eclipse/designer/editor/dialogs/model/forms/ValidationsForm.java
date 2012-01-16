package org.oobium.eclipse.designer.editor.dialogs.model.forms;

import static org.oobium.persist.Relation.DEPENDENT_CONSTANTS;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelValidation;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.editor.dialogs.model.ModelDialog;
import org.oobium.eclipse.designer.editor.dialogs.model.TableEditorForm;

public class ValidationsForm extends TableEditorForm {

	private ModelDefinition def;
	private Map<String, Map<String, Object>> validations;

	private Composite validationsComposite;

	public ValidationsForm(ModelDialog dlg, Composite parent) {
		super(dlg, parent);
	}

	private Object getValidation(String field, String name) {
		if(validations != null) {
			Map<String, Object> props = validations.get(field);
			if(props != null) {
				return props.get(name);
			}
		}
		return null;
	}
	
	private void setValidation(String field, String name, Object value) {
		if(validations == null) {
			validations = new HashMap<String, Map<String,Object>>();
		}
		Map<String, Object> props = validations.get(field);
		if(props == null) {
			validations.put(field, props = new HashMap<String, Object>());
		}
		props.put(name, value);
	}

	private void createCheck(Composite parent, final String field, final String name, boolean defaultValue) {
		Button b = new Button(parent, SWT.CHECK);
		b.setText(name);
		b.setSelection(coerce(getValidation(field, name), defaultValue));
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		b.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setValidation(field, name, ((Button) event.widget).getSelection());
			}
		});
	}
	
	private void createInt(Composite parent, final String field, final String name, int defaultValue) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(name);
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		Spinner spnr = new Spinner(parent, SWT.BORDER);
		spnr.setSelection(coerce(getValidation(field, name), defaultValue));
		spnr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		spnr.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setValidation(field, name, ((Spinner) event.widget).getSelection());
			}
		});
	}
	
	private void createOptions(Composite parent, final String field, final String name, String[] options, int defaultSelection) {
		Label l = new Label(validationsComposite, SWT.NONE);
		l.setText(name);
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		Combo c = new Combo(validationsComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		c.setItems(DEPENDENT_CONSTANTS);
		c.select(coerce(getValidation(field, name), defaultSelection) + 1);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		c.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setValidation(field, name, ((Combo) event.widget).getSelectionIndex() - 1);
			}
		});
	}

	private void createString(Composite parent, final String field, final String name, String defaultValue) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(name);
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		Text spnr = new Text(parent, SWT.BORDER);
		spnr.setText(coerce(getValidation(field, name), defaultValue));
		spnr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		spnr.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setValidation(field, name, ((Text) event.widget).getSelection());
			}
		});
	}
	
	private void setValidation(TableItem item) {
		for(Control child : validationsComposite.getChildren()) {
			child.dispose();
		}
		
		if(item == null) {
			// set null selection
		}
		else {
			final String field = item.getText(0);

			createCheck(validationsComposite, field, "isBlank", false);
			createString(validationsComposite, field, "isIn", "");
			createCheck(validationsComposite, field, "isNotBlank", false);
			createString(validationsComposite, field, "isNotIn", "");
			createCheck(validationsComposite, field, "isNotNull", false);
			createCheck(validationsComposite, field, "isNull", false);
			createCheck(validationsComposite, field, "isUnique", false);
			createInt(validationsComposite, field, "lengthIs", -1);
			createString(validationsComposite, field, "matches", "");
			createString(validationsComposite, field, "max", "");
			createInt(validationsComposite, field, "maxLength", -1);
			createString(validationsComposite, field, "message", "");
			createString(validationsComposite, field, "min", "");
			createInt(validationsComposite, field, "minLength", -1);
//			createOptions(propsComposite, field, "on", Validate.);
			createString(validationsComposite, field, "tokenizer", "");
			createString(validationsComposite, field, "unless", "");
			createCheck(validationsComposite, field, "unlessBlank", false);
			createCheck(validationsComposite, field, "unlessNull", false);
			createString(validationsComposite, field, "when", "");
//			private Class<?> with;
			createString(validationsComposite, field, "withMethod", "");
		}
		
		layout(true, true);
	}
	
	@Override
	protected void createForm() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		Composite left = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		left.setLayout(layout);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite right = new Composite(this, SWT.BORDER);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Label lbl = new Label(right, SWT.BORDER);
		lbl.setText("Properties:");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		validationsComposite = new Composite(right, SWT.BORDER);
		validationsComposite.setLayout(new GridLayout(2, false));
		validationsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		

		Composite tcomp = new Composite(left, SWT.BORDER);
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
		table.setLayoutData(data);
		
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText("Field");

		final TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.LEFT;
		
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				final TableItem item = table.getItem(new Point(event.x, event.y));
				if(item == null) return;
				if(readonly.contains(item.getText(0))) return;

				if(item.getBounds(0).contains(event.x, event.y)) {
					createAttrNameEditor(editor, item);
				}
			}
		});
		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem[] items = table.getSelection();
				if(items.length == 1) {
					setValidation(items[0]);
				} else {
					setValidation(null);
				}
			}
		});


		packColumns();
		
		ToolItem item = new ToolItem(tb, SWT.PUSH);
		item.setToolTipText("Add a new validation");
		item.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ADD));
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelEdit();
				TableItem item = addItem("", "");
				createAttrNameEditor(editor, item);
			}
		});

		final ToolItem del = new ToolItem(tb, SWT.PUSH);
		item.setToolTipText("Remove selected validation");
		del.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_DELETE));
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
			}
		});
	}

	@Override
	protected void validate(TableItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModel(Object model) {
		clearTable();
		def = (ModelDefinition) model;
		if(def != null) {
			for(ModelValidation validation : def.getValidations()) {
				String field = validation.field();
				addItem(field);
				Map<String, Object> props = validation.getCustomProperties();
				if(!props.isEmpty()) {
					for(Entry<String, Object> entry : props.entrySet()) {
						setValidation(field, entry.getKey(), entry.getValue());
					}
				}
			}
			if(table.getItemCount() > 0) {
				table.select(0);
				setValidation(table.getItem(0));
			}
		}
		packColumns();
	}

	@Override
	public void updateModel(Object model) {
		// TODO Auto-generated method stub

	}

}
