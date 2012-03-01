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

import static org.oobium.build.esp.Constants.DOM_EVENTS;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.findAny;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.reverse;

import org.oobium.build.esp.EspPart;


public class EntryPart extends EspPart {

	private EspPart key;
	private EspPart value;
	private EspPart condition;
	
	public EntryPart(EspPart parent, int start, int end) {
		super(parent, Type.EntryPart, start, end);
		parse();
	}

	public EspPart getCondition() {
		return condition;
	}
	
	public EspPart getKey() {
		return key;
	}

	public EspPart getValue() {
		return value;
	}

	public boolean isConditional() {
		return condition != null;
	}
	
	protected void parse() {
		// due to MarkupElement#parseArgs, we will always have at least a ':' at this point
		int ix = findAny(ca, start, end, ':', '(');
		int s1 = forward(ca, start, end);
		if(s1 < ix) {
			key = new EspPart(this, Type.EntryKeyPart, s1, reverse(ca, ix-1) + 1);
			if(ca[ix] == '(') {
				s1 = forward(ca, ix+1, end);
				if(s1 != -1) {
					ix = closer(ca, ix, end);
					if(ix == -1) {
						ix = end;
					}
					int s2 = reverse(ca, ix-1) + 1;
					if(s1 < s2) {
						condition = new JavaSourcePart(this, Type.JavaSourcePart, s1, s2);
					}
				}
				ix++;
			}
		}
		s1 = forward(ca, ix+1, end);
		if(s1 != -1) {
			int s2 = reverse(ca, end-1) + 1;
			if(key != null) {
				String keyText = key.getText();
				if("style".equals(keyText)) {
					value = new StyleEntryPart(this, s1, s2);
				} else if(DOM_EVENTS.contains(keyText)) {
					value = new ScriptEntryPart(this, s1, s2);
				}
			}
			if(value == null) { // default
				value = new JavaSourcePart(this, Type.JavaSourcePart, s1, s2);
			}
		}
	}

}
