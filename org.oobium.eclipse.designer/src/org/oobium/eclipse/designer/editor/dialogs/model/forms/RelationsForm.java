package org.oobium.eclipse.designer.editor.dialogs.model.forms;

import static org.oobium.persist.Relation.DEFAULT_DEPENDENT;
import static org.oobium.persist.Relation.DEFAULT_HASKEY;
import static org.oobium.persist.Relation.DEFAULT_READONLY;
import static org.oobium.persist.Relation.DEFAULT_UNIQUE;
import static org.oobium.persist.Relation.DEPENDENT_CONSTANTS;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.build.model.ModelValidation;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.editor.dialogs.model.ModelDialog;
import org.oobium.eclipse.designer.editor.dialogs.model.TableEditorForm;

public class RelationsForm extends TableEditorForm {

	private ModelDefinition def;
	private Map<String, Map<String, Object>> properties;
	private Map<String, Map<String, Object>> validations;

	private ToolItem up;
	private ToolItem dn;
	
	private Composite propertiesComposite;
	private Composite validationsComposite;

	public RelationsForm(ModelDialog dlg, Composite parent) {
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
//		Label lbl = new Label(parent, SWT.NONE);
//		lbl.setText(name);
//		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
//		
//		Spinner spnr = new Spinner(parent, SWT.BORDER);
//		spnr.setSelection(coerce(getValidation(field, name), defaultValue));
//		spnr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		spnr.addListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event event) {
//				setValidation(field, name, ((Spinner) event.widget).getSelection());
//			}
//		});
	}
	
	private void createString(Composite parent, final String field, final String name, String defaultValue) {
//		Label lbl = new Label(parent, SWT.NONE);
//		lbl.setText(name);
//		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
//		
//		Text spnr = new Text(parent, SWT.BORDER);
//		spnr.setText(coerce(getValidation(field, name), defaultValue));
//		spnr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		spnr.addListener(SWT.Modify, new Listener() {
//			@Override
//			public void handleEvent(Event event) {
//				setValidation(field, name, ((Text) event.widget).getSelection());
//			}
//		});
	}
	
	private void setValidations(TableItem item) {
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
	protected void validate(TableItem item) {
		// nothing to do (there are no free-form fields)
	}
	
	private Object getProperty(String field, String name) {
		if(properties != null) {
			Map<String, Object> props = properties.get(field);
			if(props != null) {
				return props.get(name);
			}
		}
		return null;
	}
	
	private void setProperty(String field, String name, Object value) {
		if(properties == null) {
			properties = new HashMap<String, Map<String,Object>>();
		}
		Map<String, Object> props = properties.get(field);
		if(props == null) {
			properties.put(field, props = new HashMap<String, Object>());
		}
		props.put(name, value);
	}
	
	private void setSelection(TableItem item) {
		setProperties(item);
		setValidations(item);
	}
	
	private void setProperties(TableItem item) {
		for(Control child : propertiesComposite.getChildren()) {
			child.dispose();
		}
		
		if(item == null) {
			// set null selection
		}
		else {
			final String name = item.getText(0);

			Button b = new Button(propertiesComposite, SWT.CHECK);
			b.setText("Read Only");
			b.setToolTipText("This field is read only");
			b.setSelection(coerce(getProperty(name, "readOnly"), DEFAULT_READONLY));
			b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			b.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					setProperty(name, "readOnly", ((Button) event.widget).getSelection());
				}
			});

			b = new Button(propertiesComposite, SWT.CHECK);
			b.setText("Unique");
			b.setToolTipText("Values for this field must be Unique");
			b.setSelection(coerce(getProperty(name, "unique"), DEFAULT_UNIQUE));
			b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			b.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					setProperty(name, "unique", ((Button) event.widget).getSelection());
				}
			});

			b = new Button(propertiesComposite, SWT.CHECK);
			b.setText("Has Key");
			b.setToolTipText("Force this field to contain the key in a 1-to-1 relationship");
			b.setSelection(coerce(getProperty(name, "hasKey"), DEFAULT_HASKEY));
			b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			b.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					setProperty(name, "hasKey", ((Button) event.widget).getSelection());
				}
			});

			Label l = new Label(propertiesComposite, SWT.NONE);
			l.setText("Dependent");
			l.setToolTipText("Specify what to do with dependent (related) model objects when this model is destroyed");
			l.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
			
			Combo c = new Combo(propertiesComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			c.setItems(DEPENDENT_CONSTANTS);
			c.select(coerce(getProperty(name, "dependent"), DEFAULT_DEPENDENT) + 1);
			GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
			data.horizontalIndent = 10;
			c.setLayoutData(data);
			c.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					setProperty(name, "dependent", ((Combo) event.widget).getSelectionIndex() - 1);
				}
			});
		}
		
		propertiesComposite.getParent().getParent().layout(true, true);
	}
	
	@Override
	protected void createForm() {
		GridLayout layout = new GridLayout(3, false);
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
		
		
		Composite center = new Composite(this, SWT.BORDER);
		center.setLayout(new GridLayout());
		center.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		Label lbl = new Label(center, SWT.NONE);
		lbl.setText("Properties:");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		propertiesComposite = new Composite(center, SWT.NONE);
		propertiesComposite.setLayout(new GridLayout());
		propertiesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		
		Composite right = new Composite(this, SWT.BORDER);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		lbl = new Label(right, SWT.NONE);
		lbl.setText("Validations:");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		validationsComposite = new Composite(right, SWT.NONE);
		validationsComposite.setLayout(new GridLayout());
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
		
		for(String cname : new String[] { "Name", "Type" }) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(cname);
		}

		final TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.LEFT;
		
		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem[] items = table.getSelection();
				if(items.length == 1) {
					setSelection(items[0]);
				} else {
					setSelection(null);
				}
			}
		});


		packColumns();
		
		up = new ToolItem(tb, SWT.PUSH);
		up.setToolTipText("Move selected relationship up one position");
		up.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ARROW_UP));
		up.setEnabled(false);
		up.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveSelectedItemUp(table, up, dn);
			}
		});

		dn = new ToolItem(tb, SWT.PUSH);
		dn.setToolTipText("Move selected relationship down one position");
		dn.setImage(DesignerPlugin.getImage(DesignerPlugin.IMG_ARROW_DOWN));
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
				up.setEnabled(table.getSelectionCount() == 1 && table.getSelectionIndex() > 0);
				dn.setEnabled(table.getSelectionCount() == 1 && table.getSelectionIndex() < table.getItemCount()-1);
			}
		});
	}

	@Override
	public void setModel(Object model) {
		clearTable();
		def = (ModelDefinition) model;
		if(def != null) {
			for(ModelRelation relation : def.getRelations()) {
				String name = relation.name();
				addItem(name, relation.getSimpleType());
				setReadOnly(name, true);
				Map<String, Object> props = relation.getCustomProperties();
				if(!props.isEmpty()) {
					for(Entry<String, Object> entry : props.entrySet()) {
						setProperty(name, entry.getKey(), entry.getValue());
					}
				}
				ModelValidation validation = def.getValidation(name);
				if(validation != null) {
					Map<String, Object> vprops = validation.getCustomProperties();
					if(!vprops.isEmpty()) {
						for(Entry<String, Object> entry : vprops.entrySet()) {
							setValidation(name, entry.getKey(), entry.getValue());
						}
					}
				}
			}
			if(table.getItemCount() > 0) {
				table.select(0);
				setSelection(table.getItem(0));
			}
		}
		packColumns();
	}

	private Map<String, Object> getProperties(String name, ModelRelation rel) {
		Map<String, Object> props = (rel != null) ? rel.getCustomProperties() : new HashMap<String, Object>();
		if(properties != null && properties.containsKey(name)) {
			props.putAll(properties.get(name));
		}
		return props;
	}
	
	@Override
	public void updateModel(Object model) {
		if(model != null) {
			ModelDefinition def = (ModelDefinition) model;
			for(TableItem item : table.getItems()) {
				String name = item.getText(0);
				ModelRelation r = def.getRelation(name);
				def.addRelation(name, r.type(), r.hasMany(), getProperties(name, r));
				if(validations != null && validations.containsKey(name)) {
					def.addValidation(name, validations.get(name));
				}
			}
			
		}
	}
	
}
