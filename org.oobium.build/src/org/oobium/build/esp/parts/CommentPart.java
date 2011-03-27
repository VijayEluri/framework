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

import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.reverse;

import org.oobium.build.esp.EspPart;

public class CommentPart extends EspPart {

	private int commentStart;
	private int commentEnd;
	
	public CommentPart(EspPart parent, int start) {
		super(parent, start);
		type = Type.CommentPart;
		commentStart = forward(ca, start+2);
		setEnd();
	}

	private void setEnd() {
		int s = start + 2;
		if(ca[start+1] == '*') { // multi line comment
			while(s < ca.length) {
				if(ca[s] == '/' && ca[s-1] == '*') {
					end = s + 1;
					commentEnd = reverse(ca, s-1) + 1;
					return;
				}
				s++;
			}
		} else { // single line comment
			while(s < ca.length) {
				if(ca[s] == '\n') {
					end = s;
					commentEnd = reverse(ca, s-1) + 1;
					return;
				}
				s++;
			}
		}
		end = ca.length;
		commentEnd = reverse(ca, end-1) + 1;
	}
	
	public String getComment() {
		return new String(ca, commentStart, commentEnd-commentStart);
	}

}
