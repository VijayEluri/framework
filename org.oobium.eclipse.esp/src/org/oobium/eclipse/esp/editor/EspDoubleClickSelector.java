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
package org.oobium.eclipse.esp.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of Java identifier syntax rules.
 */
public class EspDoubleClickSelector implements ITextDoubleClickStrategy {

	private static final char[] brackets= { '{', '}', '(', ')', '[', ']', '"', '"' };

	private ITextViewer text;
	private int pos;
	private int start;
	private int end;

	public EspDoubleClickSelector() {
		super();
	}

	public void doubleClicked(ITextViewer text) {
		pos = text.getSelectedRange().x;

		if(pos < 0) {
			return;
		}

		this.text = text;

		if(!selectBracketBlock())
			selectWord();
	}

	/**
	 * Match the brackets at the current selection. Return <code>true</code> if successful,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if brackets match, <code>false</code> otherwise
	 */
	protected boolean matchBracketsAt() {
		int ix1 = brackets.length;
		int ix2 = brackets.length;

		start = -1;
		end = -1;

		try {

			IDocument doc = text.getDocument();

			char prev = doc.getChar(pos - 1);
			char next = doc.getChar(pos);

			int i;
			for(i = 0; i < brackets.length; i = i + 2) {
				if(prev == brackets[i]) {
					start = pos - 1;
					ix1 = i;
				}
			}
			for(i = 1; i < brackets.length; i = i + 2) {
				if(next == brackets[i]) {
					end = pos;
					ix2 = i;
				}
			}

			if(start > -1 && ix1 < ix2) {
				end = searchForClosingBracket(start, prev, brackets[ix1 + 1], doc);
				if(end > -1) {
					return true;
				}
				start = -1;
			} else if(end > -1) {
				start = searchForOpenBracket(end, brackets[ix2 - 1], next, doc);
				if(start > -1) {
					return true;
				}
				end = -1;
			}

		} catch(BadLocationException x) {
			// discard
		}

		return false;
	}

	/**
	 * Select the word at the current selection location. Return <code>true</code> if successful,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if a word can be found at the current selection location, <code>false</code> otherwise
	 */
	protected boolean matchWord() {
		IDocument doc = text.getDocument();

		try {

			int pos = this.pos;
			char c;

			while(pos >= 0) {
				c = doc.getChar(pos);
				if(!Character.isJavaIdentifierPart(c)) {
					break;
				}
				--pos;
			}

			start = pos;

			pos = this.pos;
			int length = doc.getLength();

			while(pos < length) {
				c = doc.getChar(pos);
				if(!Character.isJavaIdentifierPart(c)) {
					break;
				}
				++pos;
			}

			end = pos;

			return true;

		} catch(BadLocationException x) {
			// discard
		}

		return false;
	}

	/**
	 * Returns the position of the closing bracket after <code>startPosition</code>.
	 * 
	 * @param startPosition - the beginning position
	 * @param openBracket - the character that represents the open bracket
	 * @param closeBracket - the character that represents the close bracket
	 * @param document - the document being searched
	 * @return the location of the closing bracket.
	 * @throws BadLocationException in case <code>startPosition</code> is invalid in the document
	 */
	protected int searchForClosingBracket(int startPosition, char openBracket, char closeBracket, IDocument document) throws BadLocationException {
		int stack = 1;
		int closePosition = startPosition + 1;
		int length = document.getLength();
		char nextChar;

		while(closePosition < length && stack > 0) {
			nextChar = document.getChar(closePosition);
			if(nextChar == openBracket && nextChar != closeBracket)
				stack++;
			else if(nextChar == closeBracket)
				stack--;
			closePosition++;
		}

		if(stack == 0)
			return closePosition - 1;
		return -1;

	}

	/**
	 * Returns the position of the open bracket before <code>startPosition</code>.
	 * 
	 * @param startPosition - the beginning position
	 * @param openBracket - the character that represents the open bracket
	 * @param closeBracket - the character that represents the close bracket
	 * @param document - the document being searched
	 * @return the location of the starting bracket.
	 * @throws BadLocationException in case <code>startPosition</code> is invalid in the document
	 */
	protected int searchForOpenBracket(int startPosition, char openBracket, char closeBracket, IDocument document) throws BadLocationException {
		int stack = 1;
		int openPos = startPosition - 1;
		char nextChar;

		while(openPos >= 0 && stack > 0) {
			nextChar = document.getChar(openPos);
			if(nextChar == closeBracket && nextChar != openBracket)
				stack++;
			else if(nextChar == openBracket)
				stack--;
			openPos--;
		}

		if(stack == 0)
			return openPos + 1;
		return -1;
	}

	/**
	 * Select the area between the selected bracket and the closing bracket.
	 * 
	 * @return <code>true</code> if selection was successful, <code>false</code> otherwise
	 */
	protected boolean selectBracketBlock() {
		if(matchBracketsAt()) {

			if(start == end)
				text.setSelectedRange(start, 0);
			else
				text.setSelectedRange(start + 1, end - start - 1);

			return true;
		}
		return false;
	}

	/**
	 * Select the word at the current selection. 
	 */
	protected void selectWord() {
		if(matchWord()) {

			if(start == end)
				text.setSelectedRange(start, 0);
			else
				text.setSelectedRange(start + 1, end - start - 1);
		}
	}
}
