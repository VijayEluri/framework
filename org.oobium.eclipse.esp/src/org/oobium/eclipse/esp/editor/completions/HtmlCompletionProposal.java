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
package org.oobium.eclipse.esp.editor.completions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class HtmlCompletionProposal extends CompletionProposal implements ICompletionProposalExtension, ICompletionProposalExtension2,
		ICompletionProposalExtension3, ICompletionProposalExtension4, ICompletionProposalExtension5, ICompletionProposalExtension6 {

	public HtmlCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image,
			String displayString, IContextInformation contextInformation, String additionalProposalInfo) {
		super(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, contextInformation, additionalProposalInfo);
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		apply(document);
	}

	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		apply(viewer.getDocument(), trigger, offset);
	}

	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
		return additionalProposalInfo;
	}

	@Override
	public int getContextInformationPosition() {
		return replacementOffset + cursorPosition;
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		return null;
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return null;
	}

	@Override
	public char[] getTriggerCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAutoInsertable() {
		return true;
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void selected(ITextViewer viewer, boolean smartToggle) {
		// TODO Auto-generated method stub
	}

	@Override
	public void unselected(ITextViewer viewer) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
