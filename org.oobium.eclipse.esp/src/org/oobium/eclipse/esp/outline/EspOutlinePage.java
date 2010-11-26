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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * A content outline page which always represents the content of the connected
 * editor in 10 segments.
 */
public class EspOutlinePage extends ContentOutlinePage {

	private IDocument input;

	/**
	 * Creates a content outline page using the given provider and the given
	 * editor.
	 * 
	 * @param provider
	 *            the document provider
	 * @param editor
	 *            the editor
	 */
	public EspOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		super();
	}

	public void createControl(Composite parent) {
		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new EspContentProvider());
		viewer.setLabelProvider(new EspLabelProvider());
		viewer.addSelectionChangedListener(this);

		if(input != null) {
			viewer.setInput(input);
		}
	}

	/**
	 * Sets the input of the outline page
	 * 
	 * @param input
	 *            the input of this outline page
	 */
	public void setInput(IDocument document) {
		this.input = document;
		TreeViewer viewer = getTreeViewer();
		if(viewer != null && !viewer.getControl().isDisposed()) {
			viewer.setInput(this.input);
		}
	}

}
