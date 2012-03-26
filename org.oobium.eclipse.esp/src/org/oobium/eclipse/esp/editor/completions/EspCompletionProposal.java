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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


public class EspCompletionProposal implements Comparable<EspCompletionProposal>, ICompletionProposal, ICompletionProposalExtension4, ICompletionProposalExtension6 {

	public static EspCompletionProposal createJavaCompletion(CompletionProposal completion, int start, int end) {
		String rstr = new String(completion.getCompletion());
		
		Image image = getImage(completion);
		StyledString dstr = new StyledString(new String(completion.getName()));
		dstr.append('(');
//		completion.get
		dstr.append(')');
		dstr.append(" : return type", StyledString.QUALIFIER_STYLER);
		
		System.out.println(completion.getSignature());
		
		String dsig = new String(completion.getDeclarationSignature());
		int ix = dsig.lastIndexOf('.');
		if(ix != -1) {
			dsig = dsig.substring(ix + 1);
		}
		if(dsig.endsWith("Model;")) {
			dsig = dsig.substring(0, dsig.length() - 6);
		}

		dstr.append(" - " + dsig, StyledString.QUALIFIER_STYLER);

		EspCompletionProposal proposal = new EspCompletionProposal(rstr, start, end-start, rstr.length(), image, dstr);
		proposal.relevance = completion.getRelevance();

		return proposal;
	}
	
	public static EspCompletionProposal createPackageCompletion(CompletionProposal completion, int start, int end) {
		String text;
		char[] ca = completion.getCompletion();
		if(ca.length > 0 && ca[ca.length-1] == ';') {
			text = new String(ca, 0, ca.length-1);
		} else {
			text = new String(ca);
		}
		
		Image image = getImage(completion);
		
		System.out.println(completion.getKind());
		
		EspCompletionProposal proposal = new EspCompletionProposal(text, start, end-start, text.length(), image, text);
		proposal.relevance = completion.getRelevance();

		return proposal;
	}

	private static Image getImage(CompletionProposal completion) {
		switch(completion.getKind()) {
		case CompletionProposal.PACKAGE_REF: return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
		case CompletionProposal.TYPE_REF:
			int flags = completion.getFlags();
			if(Flags.isInterface(flags)) return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_INTERFACE);
			if(Flags.isAbstract(flags)) {
				return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
			}
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
		case CompletionProposal.METHOD_REF: return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
		default:
			return null;
		}
	}
	
	
	/** The string to be displayed in the completion proposal popup. */
	protected StyledString displayString;
	/** The replacement string. */
	protected String replacementString;
	/** The replacement offset. */
	protected int replacementOffset;
	/** The replacement length. */
	protected int replacementLength;
	/** The cursor position after this proposal has been applied. */
	protected int cursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	protected Image image;
	/** The context information of this proposal. */
	protected IContextInformation contextInformation;
	/** The additional info of this proposal. */
	protected String additionalProposalInfo;
	
	protected int relevance;
	protected boolean autoInsertable;

	public EspCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, null, null);
	}

	public EspCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, image, (displayString == null) ? null : new StyledString(displayString), contextInformation, additionalProposalInfo);
	}

	/**
	 * Creates a new completion proposal based on the provided information. The replacement string is
	 * considered being the display string too. All remaining fields are set to <code>null</code>.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 */
	public EspCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, StyledString displayString) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, null, null);
	}
	
	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * @param contextInformation the context information associated with this proposal
	 * @param additionalProposalInfo the additional information associated with this proposal
	 */
	public EspCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, StyledString displayString, IContextInformation contextInformation, String additionalProposalInfo) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		this.replacementString= replacementString;
		this.replacementOffset= replacementOffset;
		this.replacementLength= replacementLength;
		this.cursorPosition= cursorPosition;
		this.image= image;
		this.displayString= displayString;
		this.contextInformation= contextInformation;
		this.additionalProposalInfo= additionalProposalInfo;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {
		try {
			document.replace(replacementOffset, replacementLength, replacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public int compareTo(EspCompletionProposal o) {
		int r1 = relevance;
		int r2 = o.relevance;
		if(r1 == r2) {
			return getDisplayString().compareTo(o.getDisplayString());
		}
		return r2 - r1;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return additionalProposalInfo;
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return contextInformation;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		if (displayString != null)
			return displayString.getString();
		return replacementString;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return image;
	}
	
	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(replacementOffset + cursorPosition, 0);
	}

	@Override
	public StyledString getStyledDisplayString() {
		return displayString;
	}
	
	@Override
	public boolean isAutoInsertable() {
		return autoInsertable;
	}
	
	public void setAutoInsertable(boolean autoInsertable) {
		this.autoInsertable = autoInsertable;
	}
	
}
