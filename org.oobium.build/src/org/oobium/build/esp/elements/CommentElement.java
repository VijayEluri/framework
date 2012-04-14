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

import static org.oobium.build.esp.parts.CommentPart.findEnd;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.reverse;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;

public class CommentElement extends EspElement {

	private int commentStart;
	private int commentEnd;
	
	public CommentElement(EspPart parent, int start) {
		super(parent, start);
		type = Type.CommentElement;
		end = findEnd(ca, start);
		commentStart = forward(ca, start+2, end);
		commentEnd = reverse(ca, end-1) + 1;
	}

	public String getComment() {
		return new String(ca, commentStart, commentEnd-commentStart);
	}

	public boolean isJavadoc() {
		int s = start + 2;
		return (s < ca.length && ca[s] == '*');
	}
	
}
