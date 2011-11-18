/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.dialogs;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.oobium.eclipse.OobiumPlugin;

public class ProjectSelectionDialog extends SelectionStatusDialog {

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
	private TableViewer viewer;
	
	private ViewerFilter filter;
	private boolean doFilter;

	private List<IProject> projects;
	private boolean single;

//	private final static String DIALOG_SETTINGS_SHOW_ALL = "ProjectSelectionDialog.show_all"; //$NON-NLS-1$

	public ProjectSelectionDialog(Shell parentShell, List<IProject> projects) {
		super(parentShell);
		this.projects = projects;

		setTitle("Project Selection");
		setMessage("Select a project and press OK to accept.");

		int shellStyle = getShellStyle();
		setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);

		filter = new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if(element instanceof IJavaProject) {
					return ProjectSelectionDialog.this.projects.contains(((IJavaProject) element).getProject());
				}
				return ProjectSelectionDialog.this.projects.contains(element);
			}
		};

	}

	@Override
	protected void computeResult() {
		// TODO Auto-generated method stub

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Font font = parent.getFont();
		composite.setFont(font);

		createMessageArea(composite);

		int vstyle = SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
		if(single) vstyle |= SWT.SINGLE;
		viewer = new TableViewer(composite, vstyle);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				doSelectionChanged(((IStructuredSelection) event.getSelection()).toArray());
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		viewer.getTable().setLayoutData(data);

		viewer.setLabelProvider(new JavaElementLabelProvider());
		viewer.setContentProvider(new StandardJavaElementContentProvider());
		viewer.setComparator(new JavaElementComparator());
		viewer.getControl().setFont(font);

		Button checkbox = new Button(composite, SWT.CHECK);
		checkbox.setText("Show only applicable projects");
		checkbox.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
		checkbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				updateFilter(((Button) e.widget).getSelection());
			}

			public void widgetSelected(SelectionEvent e) {
				updateFilter(((Button) e.widget).getSelection());
			}
		});
//		IDialogSettings dialogSettings = JavaPlugin.getDefault().getDialogSettings();
//		boolean doFilter = !dialogSettings.getBoolean(DIALOG_SETTINGS_SHOW_ALL) && !fProjectsWithSpecifics.isEmpty();
		checkbox.setSelection(doFilter);
		updateFilter(doFilter);

		IJavaModel input = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		viewer.setInput(input);

		doSelectionChanged(new Object[0]);
		Dialog.applyDialogFont(composite);
		return composite;
	}

	private void doSelectionChanged(Object[] objects) {
		if (objects.length != 1) {
			updateStatus(new Status(IStatus.ERROR, OobiumPlugin.ID, "")); //$NON-NLS-1$
			setSelectionResult(null);
		} else {
			updateStatus(new Status(IStatus.OK, OobiumPlugin.ID, ""));
			setSelectionResult(objects);
		}
	}

	public IJavaProject getJavaProject() {
		Object result = getFirstResult();
		if(result instanceof IJavaProject) {
			return (IJavaProject) result; 
		}
		return null;
	}
	
	public IProject getProject() {
		Object result = getFirstResult();
		if(result instanceof IJavaProject) {
			return ((IJavaProject) result).getProject(); 
		} else if(result instanceof IProject) {
			return (IProject) result;
		}
		return null;
	}

	public void setSingleSelection(boolean single) {
		this.single = single;
	}
	
	public void setShowOnlyApplicableProjects(boolean onlyApplicable) {
		this.doFilter = onlyApplicable;
	}
	
	protected void updateFilter(boolean selected) {
		if (selected) {
			viewer.addFilter(filter);
		} else {
			viewer.removeFilter(filter);
		}
//		JavaPlugin.getDefault().getDialogSettings().put(DIALOG_SETTINGS_SHOW_ALL, !selected);
	}
	
}
