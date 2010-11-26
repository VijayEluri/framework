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
package org.oobium.eclipse.esp.editor.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class DefineFoldingRegionAction extends TextEditorAction {

	public DefineFoldingRegionAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	private IAnnotationModel getAnnotationModel(ITextEditor editor) {
		return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		ITextEditor editor = getTextEditor();
		ISelection selection = editor.getSelectionProvider().getSelection();
		if(selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			if(!textSelection.isEmpty()) {
				IAnnotationModel model = getAnnotationModel(editor);
				if(model != null) {

					int start = textSelection.getStartLine();
					int end = textSelection.getEndLine();

					try {
						IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
						int offset = document.getLineOffset(start);
						int endOffset = document.getLineOffset(end + 1);
						Position position = new Position(offset, endOffset - offset);
						model.addAnnotation(new ProjectionAnnotation(), position);
					} catch(BadLocationException x) {
						// ignore
					}
				}
			}
		}
	}

}
