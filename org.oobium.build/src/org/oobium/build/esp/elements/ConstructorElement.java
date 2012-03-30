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

import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.CharStreamUtils.forward;

import org.oobium.build.esp.EspPart;

public class ConstructorElement extends MethodSignatureElement {

	public ConstructorElement(EspPart parent, int start) {
		super(parent, start);
		this.type = Type.ConstructorElement;
		this.start = forward(ca, start);
		this.end = findEOL(ca, start);
		parse();
	}

	@Override
	public String getMethodName() {
		return dom.getName();
	}
	
	@Override
	public String getReturnType() {
		return "";
	}
	
	@Override
	public boolean isStatic() {
		return false;
	}
	
	private void parse() {
		int i = start;
		while(i < ca.length && ca[i] != '(' && !Character.isWhitespace(ca[i])) {
			i++;
		}
		new EspPart(this, Type.TagPart, start, i);
		
		int start = find(ca, '(', this.start, this.end);
		if(start != -1) {
			parseSignatureArgs(ca, start, this.end);
		}
	}

}
