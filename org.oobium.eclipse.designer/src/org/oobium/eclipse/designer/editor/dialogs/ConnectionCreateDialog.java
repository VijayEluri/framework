package org.oobium.eclipse.designer.editor.dialogs;

import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.varName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;

public class ConnectionCreateDialog extends Dialog {

	protected Shell shell;
	private Button ok;
	private int result;
	
	private String sourceModel;
	private String sourceField;
	private boolean sourceHasMany;
	
	private Combo sourceType;
	private Text sourceText;
	
	private String throughField;
	private String throughSubField;
	private boolean isThrough;
	
	private Button throughBtn;
	private Label throughLabel;
	private Composite throughComposite;
	private Combo throughFieldCombo;
	private Combo throughSubFieldCombo;
	
	private String targetModel;
	private String targetField;
	private boolean targetHasMany;

	private Label targetLabel;
	private Combo targetType;
	private Text targetText;
	
	private boolean defaultSource;
	private boolean defaultTarget;
	
	private String[] relationNames;
	private ModelDefinition model;
	private Map<String, ModelDefinition> models;
	
	public ConnectionCreateDialog(String sourceModel, String sourceField, String targetModel, Map<String, ModelDefinition> models) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		setText("New Connection");
		this.sourceModel = sourceModel;
		this.sourceField = sourceField;
		this.targetModel = targetModel;
		this.models = models;
		
		model = models.get(sourceModel);
		List<ModelRelation> relations = model.getRelations();
		List<String> names = new ArrayList<String>();
		for(ModelRelation r : relations) {
			if(!r.isThrough()) {
				names.add(r.name());
			}
		}
		relationNames = names.toArray(new String[names.size()]);
		Arrays.sort(relationNames);

		defaultSource = true;
		defaultTarget = true;
	}

	protected boolean canFinish() {
		return sourceField != null && sourceField.length() > 0;
	}
	
	private void close(int result) {
		dispose();
		this.result = result;
		shell.close();
	}
	
	private void createButtons(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Composite left = new Composite(parent, SWT.NONE);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
		
		throughBtn = new Button(left, SWT.CHECK);
		throughBtn.setText("through");
		throughBtn.setSelection(isThrough);
		throughBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		throughBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isThrough = throughBtn.getSelection();
				if(isThrough) {
					throughField = throughFieldCombo.getText();
				}
				updateThroughSubFieldCombo();
				updateThroughVisibility();
			}
		});
		
		
		Composite right = new Composite(parent, SWT.NONE);
		right.setLayout(new GridLayout(2, true));
		right.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

		if("win32".equals(SWT.getPlatform())) {
			createOK(right);
			createCancel(right);
		} else {
			createCancel(right);
			createOK(right);
		}
	}
	
	private void createCancel(Composite parent) {
		Button b = new Button(parent, SWT.PUSH);
		b.setText("Cancel");
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		b.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(onCancel()) {
					close(SWT.CANCEL);
				}
			}
		});
	}

	private void createContents(Shell parent) {
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		parent.setLayout(layout);
		
		Composite form = new Composite(parent, SWT.NONE);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createForm(form);
		
		Label sep = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		Composite buttonBar = new Composite(parent, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
		createButtons(buttonBar);

		updateThroughVisibility();
		updateCanFinish();
	}
	
	protected void createForm(Composite parent) {
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 15;
		layout.marginWidth = 20;
		layout.marginHeight = 20;
		parent.setLayout(layout);
		
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(sourceModel + " ");
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		sourceType = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE);
		sourceType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		sourceType.setItems(new String[] { "hasOne", "hasMany" });
		sourceType.select(sourceHasMany ? 1 : 0);
		sourceType.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				sourceHasMany = (sourceType.getSelectionIndex() == 1);
				if(defaultSource) {
					sourceField = varName(targetModel, sourceHasMany);
					sourceText.setText(sourceField);
					defaultSource = true;
				}
			}
		});
		
		sourceText = new Text(parent, SWT.BORDER);
		if(sourceField != null) {
			sourceText.setText(sourceField);
		}
		sourceText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sourceText.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				defaultSource = false;
				sourceField = sourceText.getText();
				updateCanFinish();
			}
		});

		
		throughLabel = new Label(parent, SWT.NONE);
		throughLabel.setText("through ");
		throughLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		throughComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		throughComposite.setLayout(layout);
		throughComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		
		throughFieldCombo = new Combo(throughComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE);
		throughFieldCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		throughFieldCombo.setItems(relationNames);
		throughFieldCombo.select(0);
		throughFieldCombo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				throughField = throughFieldCombo.getItem(throughFieldCombo.getSelectionIndex());
				updateThroughSubFieldCombo();
				updateCanFinish();
			}
		});
		
		lbl = new Label(throughComposite, SWT.NONE);
		lbl.setText("to");
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		throughSubFieldCombo = new Combo(throughComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE);
		throughSubFieldCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		throughSubFieldCombo.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				throughSubField = throughSubFieldCombo.getItem(throughSubFieldCombo.getSelectionIndex());
				updateCanFinish();
			}
		});
		
		
		targetLabel = new Label(parent, SWT.NONE);
		targetLabel.setText(targetModel + " ");
		targetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		targetType = new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE);
		targetType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		targetType.setItems(new String[] { "", "hasOne", "hasMany" });
		targetType.select(blank(targetField) ? 0 : (targetHasMany ? 2 : 1));
		targetType.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int ix = targetType.getSelectionIndex();
				targetHasMany = (ix == 2);
				if(defaultTarget) {
					targetField = (ix == 0) ? "" : varName(sourceModel, targetHasMany);
					targetText.setText(targetField);
					defaultTarget = true;
				}
				else if(ix != 0 && blank(targetField)) {
					targetField = varName(sourceModel, targetHasMany);
					targetText.setText(targetField);
				}
			}
		});
		
		targetText = new Text(parent, SWT.BORDER);
		targetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		targetText.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				defaultTarget = false;
				targetField = targetText.getText();
				if(blank(targetField)) {
					targetType.select(0);
				}
				updateCanFinish();
			}
		});
	}

	private void createOK(Composite parent) {
		ok = new Button(parent, SWT.PUSH);
		ok.setText("OK");
		ok.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(onOK()) {
					close(SWT.OK);
				}
			}
		});
		ok.setEnabled(false);
		shell.setDefaultButton(ok);
	}
	
	/**
	 * subclasses to override
	 */
	public void dispose() {
		// subclasses to override
	}
	
	public String getSourceField() {
		return sourceField;
	}
	
	public String getTargetField() {
		return targetField;
	}

	public String[] getThrough() {
		return isThrough ? new String[] { throughField, throughSubField } : null;
	}
	
	public boolean isSourceHasMany() {
		return sourceHasMany;
	}
	
	public boolean isTargetHasMany() {
		return targetHasMany;
	}

	public boolean isThrough() {
		return isThrough;
	}
	
	/**
	 * subclasses to override if necessary
	 * @return true if this dialog should be closed, false otherwise.
	 * Default implementation always returns true.
	 */
	protected boolean onCancel() {
		return true;
	}
	
	/**
	 * subclasses to override if necessary
	 * @return true if this dialog should be closed, false otherwise.
	 * Default implementation always returns true.
	 */
	protected boolean onOK() {
		return true;
	}
	
	public int open() {
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setText(getText());

		createContents(shell);
		shell.pack();
		
		shell.open();
		Display display = parent.getDisplay();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) display.sleep();
		}
		return result;
	}
	
	public void setSourceHasMany(boolean hasMany) {
		this.sourceHasMany = hasMany;
	}

	public void setTargetHasMany(boolean hasMany) {
		this.targetHasMany = hasMany;
	}

	private void updateCanFinish() {
		boolean canFinish = sourceField.length() > 0;
		if(isThrough && throughSubField == null) canFinish = false;
		ok.setEnabled(canFinish);
	}
	
	private void updateThroughSubFieldCombo() {
		throughSubFieldCombo.removeAll();
		ModelRelation relation = model.getRelation(throughFieldCombo.getText());
		ModelDefinition def = models.get(relation.getSimpleType());
		List<ModelRelation> relations = def.getRelations();
		int ix = 0;
		for(int i = 0; i < relations.size(); i++) {
			ModelRelation r = relations.get(i);
			if(r.getSimpleType().equals(targetModel)) {
				throughSubFieldCombo.add(r.name());
				if(def.getSimpleName().equals(r.name())) {
					ix = i;
				}
			}
		}
		if(throughSubFieldCombo.getItemCount() == 0) {
			throughSubField = null;
			throughSubFieldCombo.setEnabled(false);
		} else {
			throughSubFieldCombo.select(ix);
			throughSubField = throughSubFieldCombo.getText();
			throughSubFieldCombo.setEnabled(true);
		}
	}
	
	private void updateThroughVisibility() {
		((GridData) throughLabel.getLayoutData()).exclude = !isThrough;
		((GridData) throughComposite.getLayoutData()).exclude = !isThrough;
		throughLabel.setVisible(isThrough);
		throughComposite.setVisible(isThrough);

		((GridData) targetLabel.getLayoutData()).exclude = isThrough;
		((GridData) targetType.getLayoutData()).exclude = isThrough;
		((GridData) targetText.getLayoutData()).exclude = isThrough;
		targetLabel.setVisible(!isThrough);
		targetType.setVisible(!isThrough);
		targetText.setVisible(!isThrough);
		
		shell.pack();
	}
	
}	