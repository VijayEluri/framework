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
package org.oobium.build.esp.elements;

import static org.oobium.utils.CharStreamUtils.*;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.JavaContainerPart;

public class InnerTextElement extends EspElement {

	public static boolean isInnerText(char[] ca, int start) {
		if(ca[start] == '+' && start+1 < ca.length) {
			int i1 = start + 1;
			if(ca[i1] == ' ') {
				return true;
			}
			switch(ca[i1]) {
			case '=':
			case '>':
			case 'w':
				int i2 = i1 + 1;
				return i2 < ca.length && ca[i2] == ' ';
			}
		}
		return false;
	}
	
	
	private EspPart innerText;
	private boolean literal;
	private boolean addLineEnding;
	private boolean addWordEnding;
	
	public InnerTextElement(EspPart parent, int start) {
		super(parent, start);
		type = Type.InnerTextElement;
		end = findEOL(ca, this.start);
		int s1 = this.start + 1;
		switch(ca[s1]) {
		case ' ':
			s1++;
			break;
		case '=':
			s1+=2;
			literal = true;
			break;
		case '>':
			s1+=2;
			literal = true;
			addLineEnding = true;
			break;
		case 'w':
			s1+=2;
			literal = true;
			addWordEnding = true;
			break;
		default:
			throw new IllegalStateException("invalid InnerTextElement: " + new String(ca, this.start, end-this.start));
		}
		if(end > s1) {
			if(literal) {
				innerText = new EspPart(this, Type.InnerTextPart, s1, end, true);
			} else {
				innerText = new JavaContainerPart(this, Type.InnerTextPart, s1, end);
			}
		}
	}

	public boolean isPromptLine() {
		return addLineEnding;
	}
	
	public boolean isWordGroup() {
		return addWordEnding;
	}
	
	public EspPart getInnerText() {
		return innerText;
	}

	public boolean hasInnerText() {
		return innerText != null;
	}

	public boolean isLiteral() {
		return literal;
	}
	
}
