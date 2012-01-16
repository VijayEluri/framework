package org.oobium.eclipse.designer.dialogs;

import static org.oobium.utils.StringUtils.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;

public class CreateModelDialog extends TitleAreaDialog {

	private enum InputType {
		String,
		Text,
		File,
		Boolean,
		Date,
		DateTime,
		Time,
		Integer,
		Decimal,
		Map
	}
	
	private static final Map<String, InputType> types;
	static {
		types = new HashMap<String, InputType>();
		types.put("java.lang.String", InputType.String);
		types.put("java.math.BigDecimal", InputType.Decimal);
		types.put("org.oobium.persist.Binary", InputType.File);
		types.put("boolean", InputType.Boolean);
		types.put("java.lang.Boolean", InputType.Boolean);
		types.put("byte[]", InputType.File);
		types.put("char[]", InputType.File);
		types.put("java.util.Date", InputType.DateTime);
		types.put("java.sql.Date", InputType.Date);
		types.put("double", InputType.Decimal);
		types.put("java.lang.Double", InputType.Decimal);
		types.put("int", InputType.Integer);
		types.put("java.lang.Integer", InputType.Integer);
		types.put("long", InputType.Integer);
		types.put("java.lang.Long", InputType.Integer);
		types.put("java.util.Map", InputType.Map);
		types.put("org.oobium.persist.Password", InputType.String);
		types.put("org.oobium.persist.Text", InputType.Text);
		types.put("java.sql.Time", InputType.Time);
		types.put("java.sql.Timestamp", InputType.DateTime);
	};

	
	private final ModelDefinition def;
	private final Map<String, Object> data;
	
	public CreateModelDialog(Shell parentShell, ModelDefinition def) {
		super(parentShell);
		this.def = def;
		this.data = new HashMap<String, Object>();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Create new " + def.getSimpleName());
		setMessage("Create a new instance of " + def.getSimpleName(), IMessageProvider.INFORMATION);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dlg = (Composite) super.createDialogArea(parent);
		
		Composite comp = new Composite(dlg, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label lbl = new Label(comp, SWT.NONE);
		lbl.setText("Attributes:");
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		for(ModelAttribute a : def.getAttributes()) {
			InputType type = types.get(a.type());
			createRow(comp, a.name(), type);
		}
		
		if(def.hasOne()) {
			lbl = new Label(comp, SWT.NONE);
			lbl.setText("Has One Relationships:");
			GridData griddata = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
			griddata.verticalIndent = 15;
			lbl.setLayoutData(griddata);
			
			for(ModelRelation r : def.getHasOnes()) {
				createRow(comp, r.name(), InputType.Integer);
			}
		}
		
		lbl = new Label(comp, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData griddata = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		griddata.verticalIndent = 10;
		lbl.setLayoutData(griddata);

		return dlg;
	}

	private void createRow(Composite parent, final String name, InputType type) {
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(titleize(name) + ":");
		GridData griddata = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		griddata.horizontalIndent = 15;
		lbl.setLayoutData(griddata);

		switch(type) {
		case String:
			Text s = new Text(parent, SWT.BORDER | SWT.SINGLE);
			s.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			s.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					data.put(name, ((Text) e.widget).getText());
				}
			});
			break;
		case Text:
			griddata.verticalAlignment = SWT.TOP; // this is for the label
			Text t = new Text(parent, SWT.BORDER | SWT.MULTI);
			griddata = new GridData(SWT.FILL, SWT.CENTER, true, false);
			griddata.heightHint = 100;
			t.setLayoutData(griddata);
			t.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					data.put(name, ((Text) e.widget).getText());
				}
			});
			break;
		case Integer:
			Spinner i = new Spinner(parent, SWT.BORDER);
			i.setDigits(0);
			i.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					data.put(name, ((Spinner) e.widget).getSelection());
				}
			});
			break;
		case Date:
			DateTime date = new DateTime(parent, SWT.BORDER | SWT.CALENDAR | SWT.DROP_DOWN);
			date.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					data.put(name, getDate(((DateTime) e.widget)));
				}
			});
			break;
		case DateTime:
			DateTime dt = new DateTime(parent, SWT.BORDER | SWT.CALENDAR | SWT.TIME | SWT.DROP_DOWN);
			dt.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					data.put(name, getDate(((DateTime) e.widget)));
				}
			});
			break;
		case Time:
			DateTime time = new DateTime(parent, SWT.BORDER | SWT.TIME | SWT.DROP_DOWN);
			time.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					data.put(name, getDate(((DateTime) e.widget)));
				}
			});
			break;
		}
	}
	
	private Date getDate(DateTime dt) {
		Calendar cal = Calendar.getInstance();
		cal.set(dt.getYear(), dt.getMonth(), dt.getDay(), dt.getHours(), dt.getMinutes(), dt.getSeconds());
		return cal.getTime();
	}
	
	public Map<String, Object> getData() {
		return data;
	}
	
}
