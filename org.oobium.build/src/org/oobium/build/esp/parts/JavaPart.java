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
package org.oobium.build.esp.parts;

import static org.oobium.utils.CharStreamUtils.*;

import org.oobium.build.esp.EspPart;


public class JavaPart extends EspPart {

	private char escapeChar = 'h'; // default to HTML escaped;
	private EspPart source;
	
	public JavaPart(EspPart parent, int start, int end) {
		super(parent, Type.JavaPart, start, end);
		int srcStart = forward(ca, start+1, end);
		if(srcStart != -1 && (srcStart+1) < end && ca[srcStart+1] == ' ') {
			if(isEscapeChar(ca[srcStart])) {
				escapeChar = ca[srcStart];
				srcStart = forward(ca, srcStart+2, end);
			} else if(ca[srcStart] == 'r') {
				// "raw" - don't escape
				escapeChar = 0;
				srcStart = forward(ca, srcStart+2, end);
			}
		}
		int srcEnd = reverse(ca, end-2) + 1;
		if(srcEnd > srcStart) {
			source = new EspPart(this, Type.JavaSourcePart, srcStart, srcEnd);
		}
	}

	public char getEscapeChar() {
		return escapeChar;
	}
	
	public String getSource() {
		return (source != null) ? source.getText() : "";
	}
	
	public EspPart getSourcePart() {
		return source;
	}

	private boolean isEscapeChar(char c) {
		switch(c) {
		case 'n':
		case 'h':
		case 'j':
		case 'f':
			return true;
		}
		return false;
	}
	
	public boolean isEscaped() {
		return isEscapeChar(escapeChar);
	}
	
}
