package org.oobium.eclipse.designer.editor.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public abstract class SimpleDialog extends Dialog {

	protected Shell shell;
	private Button ok;
	private int result;
	
	public SimpleDialog() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
	}

	protected abstract boolean canFinish();
	
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
		
		Button b = new Button(left, SWT.CHECK);
		b.setText("through");
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		
		
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
		setCanFinish(canFinish());
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
	}

	protected abstract void createForm(Composite parent);
	
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
	
	protected void setCanFinish(boolean canFinish) {
		ok.setEnabled(canFinish);
	}

}
