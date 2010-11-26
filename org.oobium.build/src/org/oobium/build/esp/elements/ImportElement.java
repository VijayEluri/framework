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

import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.isEqual;
import static org.oobium.utils.CharStreamUtils.reverse;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;

public class ImportElement extends EspElement {

	private boolean isStatic;
	private EspPart importPart;
	
	public ImportElement(EspPart parent, int start) {
		super(parent, start);
		this.type = Type.ImportElement;
		this.start = forward(ca, start);
		this.end = findEOL(ca, start);
		parse();
	}

	public String getImport() {
		return importPart.getText();
	}

	public EspPart getImportPart() {
		return importPart;
	}
	
	public boolean hasImport() {
		return importPart != null;
	}

	public boolean isStatic() {
		return isStatic;
	}
	
	private void parse() {
		new EspPart(this, Type.ImportPart, start, start+6);
		int s1 = forward(ca, start+7, end);
		if(s1 != -1) {
			int s2 = s1;
			while(s2 < end && !Character.isWhitespace(ca[s2++]));
			if(isEqual(ca, s1, s2, 's', 't', 'a', 't', 'i', 'c', ' ')) {
				isStatic = true;
				new EspPart(this, Type.ImportPart, s1, s2-1);
				s1 = forward(ca, s2, end);
				s2 = reverse(ca, end-1) + 1;
			}
			if(s1 != -1 && s2 > s1) {
				importPart = new EspPart(this, Type.ImportPart, s1, s2);
			}
		}
	}

}
