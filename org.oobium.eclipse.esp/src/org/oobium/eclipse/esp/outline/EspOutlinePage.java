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
package org.oobium.eclipse.esp.outline;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.oobium.eclipse.esp.outline.actions.SortAction;

public class EspOutlinePage extends ContentOutlinePage {

	private IDocument document;
	private EspContentProvider contentProvider;
	private SortAction sortAction;

	public EspOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		super();
	}

	private void createActions() {
		sortAction = new SortAction(this);
		sortAction.setChecked(true);
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);

		createActions();
		
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(contentProvider = new EspContentProvider(true));
		viewer.setLabelProvider(new EspLabelProvider());
		viewer.addSelectionChangedListener(this);

		createToolBar();
		
		if(document != null) {
			viewer.setInput(document);
		}
	}
	
	private void createToolBar() {
		IToolBarManager manager = getSite().getActionBars().getToolBarManager();

		manager.add(sortAction);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Sets the input of the outline page
	 * 
	 * @param document
	 *            the input of this outline page
	 */
	public void setInput(IDocument document) {
		this.document = document;
		TreeViewer viewer = getTreeViewer();
		if(viewer != null && !viewer.getControl().isDisposed()) {
			viewer.setInput(this.document);
		}
	}

	public void setSort(boolean sort) {
		contentProvider.setSort(sort);
	}
	
}
