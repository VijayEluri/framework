/*******************************************************************************
 * Copyright (c) 2011 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.designer.outline;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.oobium.eclipse.designer.editor.models.SiteElement;
import org.oobium.eclipse.designer.outline.actions.SortAction;

public class DesignerOutlinePage extends ContentOutlinePage {

	private DesignerContentProvider contentProvider;
	private SortAction sortAction;
	private SiteElement site;

	private void createActions() {
		sortAction = new SortAction(this);
		sortAction.setChecked(true);
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);

		createActions();
		
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(contentProvider = new DesignerContentProvider(true));
		viewer.setLabelProvider(new DesignerLabelProvider());
		
		viewer.addSelectionChangedListener(this);

		createToolBar();
		
		if(site != null) {
			viewer.setInput(site);
		}
	}
	
	private void createToolBar() {
		IToolBarManager manager = getSite().getActionBars().getToolBarManager();

		manager.add(sortAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				getTreeViewer().refresh();
			}
		});
	}
	
	public void setInput(SiteElement site) {
		this.site = site;
		TreeViewer viewer = getTreeViewer();
		if(viewer != null && !viewer.getControl().isDisposed()) {
			viewer.setInput(site);
		}
	}

	public void setSort(boolean sort) {
		contentProvider.setSort(sort);
	}
	
}
