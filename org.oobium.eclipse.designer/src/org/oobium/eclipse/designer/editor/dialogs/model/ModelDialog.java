package org.oobium.eclipse.designer.editor.dialogs.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.oobium.build.model.ModelDefinition;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.eclipse.designer.editor.dialogs.model.forms.AttributesForm;
import org.oobium.eclipse.designer.editor.dialogs.model.forms.IndexesForm;
import org.oobium.eclipse.designer.editor.dialogs.model.forms.RelationsForm;
import org.oobium.eclipse.designer.editor.models.ModelElement;

public class ModelDialog extends TitleAreaDialog {

	private ModelDefinition def;
	
	private String name;
	private boolean newModel;
	
	private AttributesForm attributesForm;
	private RelationsForm relationsForm;
	private IndexesForm indexesForm;
	private Button allowUpdateBtn;
	private Button allowDeleteBtn;
	
	private RGB rgb;
	private Color color;

	private final List<String> modelNames;
	private final Map<String, ModelDefinition> systemModels;

	private final String defaultMessage;
	

	public ModelDialog(List<String> otherModels) {
		this(null, otherModels);
	}

	public ModelDialog(ModelElement model, List<String> otherModels) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.modelNames = otherModels;
		if(model == null) {
			newModel = true;
			defaultMessage = "Add a new model description to this application";
		} else {
			newModel = false;
			def = model.getDefinition().getCopy();
			defaultMessage = "Edit this application's existing description of a " + def.getSimpleName();
			rgb = model.getColor();
		}
		systemModels = new LinkedHashMap<String, ModelDefinition>();
		for(ModelDefinition def : ModelDefinition.getSystemDefinitions()) {
			systemModels.put(def.getSimpleName(), def);
		}
	}
	
	public String checkModel() {
		// check for errors
		if(name == null) {
			if(newModel) {
				return "Model name cannot be blank";
			}
		} else {
			if(name.length() == 0) {
				return "Model name cannot be blank";
			}
			if(modelNames.contains(name)) {
				return "Model already exists";
			}
			if(name.charAt(0) == ' ' || name.charAt(name.length()-1) == ' ') {
				return "Type name is not valid. A Java type name must not start or end with a blank";
			}
			if(!Character.isJavaIdentifierStart(name.charAt(0))) {
				return "Type name is not valid. The type name '"+name+"' is not a valid identifier";
			}
			for(char ch : name.toCharArray()) {
				if(!Character.isJavaIdentifierPart(ch)) {
					return "Type name is not valid. The type name '"+name+"' is not a valid identifier";
				}
			}
		}
		
		setMessage(defaultMessage);

		// check for warnings
		if(name != null) { // name is null in edit mode
			if(Character.isLowerCase(name.charAt(0))) {
				setMessage("Type name is discouraged. By convention, Java type names usually start with an uppercase letter", IMessageProvider.WARNING);
			}
		}
		return null;
	}
	
	@Override
	public boolean close() {
		if(color != null) {
			color.dispose();
			color = null;
		}
		return super.close();
	}
	
	@Override
	public void create() {
		super.create();
		if(newModel) {
			setTitle("Add a new Model");
			setTitleImage(DesignerPlugin.getImage(DesignerPlugin.IMG_DIALOG_NEW_MODEL));
			setMessage(defaultMessage);
			getButton(OK).setEnabled(false);
		} else {
			setTitle("Edit " + def.getSimpleName());
			setTitleImage(DesignerPlugin.getImage(DesignerPlugin.IMG_DIALOG_EDIT_MODEL));
			setMessage(defaultMessage);
		}
	}
	
	private Composite createDisplayForm(Composite parent) {
		if(rgb != null) {
			color = new Color(parent.getDisplay(), rgb);
		}
		
		Composite form = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		form.setLayout(layout);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite comp = new Composite(form, SWT.NONE);
		layout = new GridLayout(2, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		final Composite lc = new Composite(comp, SWT.BORDER);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		lc.setLayout(layout);
		lc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));
		lc.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Rectangle r = ((Composite) e.widget).getClientArea();
				if(color == null) {
					Point p = e.gc.textExtent("Default");
					e.gc.drawString("Default", r.x+((r.width-p.x)/2), r.y+((r.height-p.y)/2));
				}
				else {
					e.gc.setBackground(color);
					e.gc.fillRectangle(r.x+5, r.y+5, r.width-10, r.height-10);
				}
			}
		});
		
		Button b = new Button(comp, SWT.PUSH);
		b.setText("Select Color");
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog dlg = new ColorDialog(((Button) e.widget).getShell());
				dlg.setText("Select Model Color");
				if(color == null) {
					dlg.setRGB(ColorConstants.lightBlue.getRGB());
				} else {
					dlg.setRGB(rgb);
				}
				if(dlg.open() != null) {
					rgb = dlg.getRGB();
					if(color != null) {
						color.dispose();
					}
					color = new Color(e.display, rgb);
					lc.redraw();
				}
			}
		});
		
		b = new Button(comp, SWT.PUSH);
		b.setText("Default Color");
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(color != null) {
					color.dispose();
				}
				color = null;
				rgb = null;
				lc.redraw();
			}
		});
		
		return form;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		
		if(newModel) {
			createNameForm(dialogArea);
			Label lbl = new Label(dialogArea, SWT.HORIZONTAL | SWT.SEPARATOR);
			lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		}
		
		Composite detailsArea = new Composite(dialogArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		detailsArea.setLayout(layout);
		detailsArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		CTabFolder folder = new CTabFolder(detailsArea, SWT.BORDER);
		folder.setSimple(false);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText("Attributes");
		item.setControl(attributesForm = new AttributesForm(this, folder));
		
		item = new CTabItem(folder, SWT.NONE);
		item.setText("Relationships");
		item.setControl(relationsForm = new RelationsForm(this, folder));

		item = new CTabItem(folder, SWT.NONE);
		item.setText("Indexes");
		item.setControl(indexesForm = new IndexesForm(this, folder));

		item = new CTabItem(folder, SWT.NONE);
		item.setText("Display");
		item.setControl(createDisplayForm(folder));

		folder.setSelection(0);

		allowUpdateBtn = new Button(detailsArea, SWT.CHECK);
		allowUpdateBtn.setText("Allow Update");
		allowUpdateBtn.setSelection((def != null) ? def.allowUpdate() : true); // disregard defaults
		allowUpdateBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		allowDeleteBtn = new Button(detailsArea, SWT.CHECK);
		allowDeleteBtn.setText("Allow Delete");
		allowDeleteBtn.setSelection((def != null) ? def.allowDelete() : true); // disregard defaults
		allowDeleteBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		
		attributesForm.setModel(def);
		relationsForm.setModel(def);
		indexesForm.setModel(def);

		
		return dialogArea;
	}

	private void createNameForm(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Label lbl = new Label(comp, SWT.NONE);
		lbl.setText("Name: ");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		final Combo combo = new Combo(comp, SWT.BORDER | SWT.DROP_DOWN);
		combo.setItems(systemModels.keySet().toArray(new String[systemModels.size()]));
		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		combo.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				name = combo.getText();
				validate();
			}
		});
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				def = systemModels.get(combo.getText());
				attributesForm.setModel(def);
				relationsForm.setModel(def);
				indexesForm.setModel(def);
				if(def != null) {
					allowDeleteBtn.setSelection(def.allowDelete());
					allowUpdateBtn.setSelection(def.allowUpdate());
				}
			}
		});
	}

	public RGB getColor() {
		return rgb;
	}
	
	public ModelDefinition getDefinition() {
		return def;
	}
	
	public Map<String, ModelDefinition> getSystemModels() {
		return systemModels;
	}

	@Override
	protected void okPressed() {
		if(def == null) {
			def = new ModelDefinition(name);
		}

		attributesForm.updateModel(def);
		relationsForm.updateModel(def);
		indexesForm.updateModel(def);

		def.allowDelete(allowDeleteBtn.getSelection());
		def.allowUpdate(allowUpdateBtn.getSelection());
		super.okPressed();
	}

	@Override
	public void setErrorMessage(String newErrorMessage) {
		super.setErrorMessage(newErrorMessage);
		getButton(OK).setEnabled(newErrorMessage == null);
	}
	
	public void validate() {
		setErrorMessage(checkModel());
		attributesForm.validate();
		relationsForm.validate();
		indexesForm.validate();
	}
	
}
