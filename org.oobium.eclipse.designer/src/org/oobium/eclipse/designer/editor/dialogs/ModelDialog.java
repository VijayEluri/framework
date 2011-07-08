package org.oobium.eclipse.designer.editor.dialogs;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ModelDialog extends SimpleDialog {

	private static final String[][] types = {
		{ "String", "java.lang.String" },
		{ "BigDecimal", "java.math.BigDecimal" },
		{ "Binary", "org.oobium.persist.Binary" },
		{ "boolean", "boolean" },
		{ "Boolean", "java.lang.Boolean" },
		{ "byte[]", "byte[]" },
		{ "char[]", "char[]" },
		{ "Date (java.util.Date)", "java.util.Date" },
		{ "Date (java.sql.Date)", "java.sql.Date" },
		{ "double", "double" },
		{ "Double", "java.lang.Double" },
		{ "int", "int" },
		{ "Integer", "java.lang.Integer" },
		{ "long", "long" },
		{ "Long", "java.lang.Long" },
		{ "Text", "org.oobium.persist.Text" },
		{ "Time", "java.sql.Time" },
		{ "Timestamp", "java.sql.Timestamp" },
	};
	
	private Table table;
	private Image addImage;
	private Image delImage;
	private Image upImage;
	private Image downImage;
	private ToolItem up;
	private ToolItem dn;
	private Control editControl;
	
	private boolean newModel;
	
	private String name;
	private String[][] attributes;
	private boolean timestamps = true;
	private boolean datestamps;
	private boolean allowUpdate = true;
	private boolean allowDelete = true;
	
	public ModelDialog() {
		this(null);
	}
	
	public ModelDialog(String name) {
		super();
		this.name = name;
		if(name == null) {
			newModel = true;
			setText("New Model");
		} else {
			newModel = false;
			setText("Edit " + name);
		}
	}

	/**
	 * must be called before calling open
	 */
	public void addAttribute(String name, String type) {
		String stype = getSimpleType(type);
		if(attributes == null || attributes.length == 0) {
			attributes = new String[][] { { name, stype } };
		} else {
			attributes = Arrays.copyOf(attributes, attributes.length + 1);
			attributes[attributes.length - 1] = new String[] { name, stype };
		}
	}
	
	@Override
	protected boolean canFinish() {
		return name != null && name.length() > 0;
	}
	
	private void cancelEdit() {
		if(editControl != null) {
			editControl.dispose();
			editControl = null;
		}
	}
	
	private void createAttrNameEditor(final TableEditor editor, final TableItem item) {
		final Text text = new Text(table, SWT.NONE);
		editControl = text;
		text.setText(item.getText(0));
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch(event.type) {
				case SWT.FocusOut:
					item.setText(0, text.getText());
					text.dispose();
					break;
				case SWT.Traverse:
					switch(event.detail) {
					case SWT.TRAVERSE_RETURN:
						item.setText(0, text.getText());
					case SWT.TRAVERSE_ESCAPE:
						text.dispose();
						event.doit = false;
						break;
					case SWT.TRAVERSE_TAB_NEXT:
						item.setText(0, text.getText());
						text.dispose();
						createAttrTypeEditor(editor, item);
						break;
					}
				}
			}
		};
		text.addListener(SWT.FocusOut, listener);
		text.addListener(SWT.Traverse, listener);
		
		text.selectAll();
		text.setFocus();
		
		editor.setEditor(text, item, 0);
	}

	private void createAttrTypeEditor(TableEditor editor, final TableItem item) {
		final CCombo combo = new CCombo(table, SWT.DROP_DOWN | SWT.READ_ONLY);
		editControl = combo;
		for(String[] type : types) {
			combo.add(type[0]);
		}
		combo.setText(item.getText(1));
		Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch(event.type) {
				case SWT.FocusOut:
					item.setText(1, combo.getText());
					combo.dispose();
					break;
				case SWT.Traverse:
					switch(event.detail) {
					case SWT.TRAVERSE_RETURN:
						item.setText(1, combo.getText());
					case SWT.TRAVERSE_ESCAPE:
						combo.dispose();
						event.doit = false;
						break;
					}
				}
			}
		};
		combo.addListener(SWT.FocusOut, listener);
		combo.addListener(SWT.Traverse, listener);
		
		combo.setFocus();
		
		editor.setEditor(combo, item, 1);
	}

	@Override
	protected void createForm(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);

		if(newModel) {
			Composite comp = new Composite(parent, SWT.NONE);
			layout = new GridLayout(2, false);
			layout.marginWidth = 10;
			layout.marginHeight = 10;
			comp.setLayout(layout);
			comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			
			Label lbl = new Label(comp, SWT.NONE);
			lbl.setText("Name: ");
			lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			
			final Text txt = new Text(comp, SWT.BORDER);
			txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			txt.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event event) {
					name = txt.getText();
					setCanFinish(name.length() > 0);
				}
			});
			txt.selectAll();
			
			lbl = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
			lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		}		
		
		Composite comp = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite tcomp = new Composite(comp, SWT.BORDER);
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
		data.minimumHeight = 150;
		data.minimumWidth = 300;
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

				if(item.getBounds(0).contains(event.x, event.y)) {
					createAttrNameEditor(editor, item);
				}
				else if(item.getBounds(1).contains(event.x, event.y)) {
					createAttrTypeEditor(editor, item);
				}
			}
		});

		if(attributes != null) {
			for(String[] attribute : attributes) {
				TableItem ti = new TableItem(table, SWT.NONE);
				ti.setText(new String[] { attribute[0], attribute[1] });
			}
		}
		
		for(TableColumn column : table.getColumns()) {
			column.pack();
		}
		
		addImage = new Image(shell.getDisplay(), getClass().getResourceAsStream("add.png"));
		delImage = new Image(shell.getDisplay(), getClass().getResourceAsStream("delete.png"));
		upImage = new Image(shell.getDisplay(), getClass().getResourceAsStream("arrow_up.gif"));
		downImage = new Image(shell.getDisplay(), getClass().getResourceAsStream("arrow_down.gif"));
		
		ToolItem item = new ToolItem(tb, SWT.PUSH);
		item.setImage(addImage);
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelEdit();
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(new String[] { "", types[0][0] });
				createAttrNameEditor(editor, item);
			}
		});

		final ToolItem del = new ToolItem(tb, SWT.PUSH);
		del.setImage(delImage);
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
		up.setImage(upImage);
		up.setEnabled(false);
		up.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveSelectedItemUp();
			}
		});

		dn = new ToolItem(tb, SWT.PUSH);
		dn.setImage(downImage);
		dn.setEnabled(false);
		dn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				moveSelectedItemDown();
			}
		});

		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				del.setEnabled(table.getSelectionCount() > 0);
				up.setEnabled(table.getSelectionCount() == 1 && table.getSelectionIndex() > 0);
				dn.setEnabled(table.getSelectionCount() == 1 && table.getSelectionIndex() < table.getItemCount()-1);
			}
		});

		
		Composite bcomp = new Composite(comp, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		bcomp.setLayout(layout);
		bcomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button b = new Button(bcomp, SWT.CHECK);
		b.setText("Timestamps");
		b.setSelection(timestamps);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				timestamps = ((Button) e.widget).getSelection();
			}
		});

		b = new Button(bcomp, SWT.CHECK);
		b.setText("Allow Update");
		b.setSelection(allowUpdate);
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				allowUpdate = ((Button) e.widget).getSelection();
			}
		});

		b = new Button(bcomp, SWT.CHECK);
		b.setText("Datestamps");
		b.setSelection(datestamps);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				datestamps = ((Button) e.widget).getSelection();
			}
		});
		
		b = new Button(bcomp, SWT.CHECK);
		b.setText("Allow Delete");
		b.setSelection(allowDelete);
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				allowDelete = ((Button) e.widget).getSelection();
			}
		});
	}

	private void moveSelectedItemDown() {
		cancelEdit();
		int ix = table.getSelectionIndex();
		TableItem item = table.getItem(ix);
		String[] sa = new String[] { item.getText(0), item.getText(1) };
		item.dispose();
		ix++;
		item = new TableItem(table, SWT.NONE, ix);
		item.setText(sa);
		table.select(ix);
		if(ix == table.getItemCount()-1) {
			dn.setEnabled(false);
		}
		if(ix > 0) {
			up.setEnabled(true);
		}
	}
	
	private void moveSelectedItemUp() {
		cancelEdit();
		int ix = table.getSelectionIndex();
		TableItem item = table.getItem(ix);
		String[] sa = new String[] { item.getText(0), item.getText(1) };
		item.dispose();
		ix--;
		item = new TableItem(table, SWT.NONE, ix);
		item.setText(sa);
		table.select(ix);
		if(ix == 0) {
			up.setEnabled(false);
		}
		if(ix < table.getItemCount()-1) {
			dn.setEnabled(true);
		}
	}
	
	@Override
	public void dispose() {
		addImage.dispose();
		delImage.dispose();
		upImage.dispose();
		downImage.dispose();
	}

	public boolean getAllowDelete() {
		return allowDelete;
	}

	public boolean getAllowUpdate() {
		return allowUpdate;
	}

	public String[][] getAttributes() {
		return attributes;
	}

	public boolean getDatestamps() {
		return datestamps;
	}

	private String getFullType(String simpleType) {
		for(String[] type : types) {
			if(type[0].equals(simpleType)) {
				return type[1];
			}
		}
		return null;
	}

	private String getSimpleType(String fullType) {
		for(String[] type : types) {
			if(type[1].equals(fullType)) {
				return type[0];
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public boolean getTimestamps() {
		return timestamps;
	}

	@Override
	protected boolean onOK() {
		TableItem[] items = table.getItems();
		attributes = new String[items.length][];
		for(int i = 0; i < items.length; i++) {
			attributes[i] = new String[] { items[i].getText(0), getFullType(items[i].getText(1)) };
		}
		return true;
	}
	
	/**
	 * must be called before calling open
	 */
	public void setAllowDelete(boolean allowDelete) {
		this.allowDelete = allowDelete;
	}

	/**
	 * must be called before calling open
	 */
	public void setAllowUpdate(boolean allowUpdate) {
		this.allowUpdate = allowUpdate;
	}

	/**
	 * must be called before calling open
	 */
	public void setDatestamps(boolean datestamps) {
		this.datestamps = datestamps;
	}
	
	/**
	 * must be called before calling open
	 */
	public void setTimestamps(boolean timestamps) {
		this.timestamps = timestamps;
	}

}
