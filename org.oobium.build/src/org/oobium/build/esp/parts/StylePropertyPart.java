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

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;

public class StylePropertyPart extends EspPart {

	private EspPart name;
	private EspPart value;
	private boolean isValueJava;
	
	public StylePropertyPart(EspPart parent, int start, int end) {
		super(parent, Type.StylePropertyPart, start, end);
		parse();
	}

	@Override
	public EspElement getElement() {
		return parent.getElement();
	}
	
	public EspPart getName() {
		return name;
	}
	
	public EspPart getValue() {
		return value;
	}

	public boolean isValueJava() {
		return isValueJava;
	}
	
	public boolean hasName() {
		return name != null;
	}
	
	public boolean hasValue() {
		return value != null;
	}
	
	private void parse() {
		int s1 = forward(ca, start, end);
		if(s1 != -1) {
			s1 = commentCheck(this, s1); // before comment
			s1 = forward(ca, s1, end);
			if(s1 != -1) {
				for(int s = s1; s < end; s++) {
					switch(ca[s]) {
					case ':':
						int s2 = reverse(ca, s-1) + 1;
						if(s2 > s1) {
							name = new EspPart(this, Type.StylePropertyNamePart, s1, s2);
						}
						if(s < end-1 && ca[s+1] == '=') {
							isValueJava = true;
							s++;
						}
						s1 = forward(ca, s+1, end);
						if(s1 != -1) {
							s2 = reverse(ca, end-1) + 1;
							if(s2 > s1) {
								value = new EspPart(this, Type.StylePropertyValuePart, s1, s2);
							}
						}
						return;
					}
					s = commentCloser(ca, s);
				}
				
				// no ':' found
				int s2 = s1;
				while(s2 < end) {
					if(Character.isWhitespace(ca[s2])) {
						name = new EspPart(this, Type.StylePropertyNamePart, s1, s2);
						break;
					}
					if(s2 == (end-1)) {
						name = new EspPart(this, Type.StylePropertyNamePart, s1, s2+1);
						break;
					}
					s2 = commentCloser(ca, s2);
					if(s2 >= end) {
						name = new EspPart(this, Type.StylePropertyNamePart, s1, end);
						break;
					}
					s2++;
				}
				
				commentCheck(this, s2+1); // after comment
			}
		}
	}

}
