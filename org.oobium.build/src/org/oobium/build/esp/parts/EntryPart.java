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

import static org.oobium.utils.CharStreamUtils.find;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.reverse;

import org.oobium.build.esp.EspPart;


public class EntryPart extends EspPart {

	private EspPart key;
	private EspPart value;
	
	public EntryPart(EspPart parent, int start, int end) {
		super(parent, Type.EntryPart, start, end);
		parse();
	}

	public EspPart getKey() {
		return key;
	}

	public EspPart getValue() {
		return value;
	}

	protected void parse() {
		int ix = find(ca, ':', start, end);
		int s1 = forward(ca, start, end);
		if(s1 < ix) {
			key = new EspPart(this, Type.EntryKeyPart, s1, reverse(ca, ix-1) + 1);
		}
		s1 = forward(ca, ix+1, end);
		if(s1 != -1) {
			if(key != null && "style".equals(key.getText())) {
				value = new StyleEntryPart(this, Type.StyleEntryPart, s1, reverse(ca, end-1) + 1);
			} else {
				value = new JavaContainerPart(this, Type.EntryValuePart, s1, reverse(ca, end-1) + 1);
			}
		}
	}

}
