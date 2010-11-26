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

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.StylePropertyPart;

public class StyleChildElement extends EspElement {

	private List<EspPart> selectors;
	private List<StylePropertyPart> properties;
	
	public StyleChildElement(StyleElement parent, int start) {
		super(parent, start);
		type = Type.StyleChildElement;
		parse();
	}

	private int addProperties(int start) {
		int s1 = start;
		int s2 = start;
		int eol = findEOL(ca, start);
		while(s2 < eol) {
			if(ca[s2] == '}') {
				if(s2 > s1) {
					addProperty(s1, s2);
				}
				return s2 + 1;
			}
			if(ca[s2] == ';') {
				if(s2 > s1) {
					addProperty(s1, s2);
				}
				s1 = s2 = s2 + 1;
			} else {
				int s = commentCloser(ca, s2);
				if(s >= eol) {
					addProperty(s1, eol);
					return s;
				} else {
					s2 = s + 1;
				}
			}
		}
		if(s2 > s1) {
			addProperty(s1, s2);
		}
		return s2;
	}
	
	private void addProperty(int start, int end) {
		if(properties == null) {
			properties = new ArrayList<StylePropertyPart>();
		}
		properties.add(new StylePropertyPart(this, start, end));
	}
	
	private void addSelector(int start, int end) {
		if(selectors == null) {
			selectors = new ArrayList<EspPart>();
		}
		selectors.add(new EspPart(this, Type.StyleSelectorPart, start, end));
	}
	
	public EspElement getElement() {
		return (EspElement) parent;
	}

	public List<StylePropertyPart> getProperties() {
		return properties;
	}
	
	public List<EspPart> getSelectors() {
		return selectors;
	}
	
	public boolean hasProperties() {
		return properties != null;
	}
	
	public boolean hasSelectors() {
		return selectors != null;
	}
	
	private void parse() {
		int s1 = start;
		int eol = findEOL(ca, s1);
		
		int s2 = s1;
		while(s2 < eol) {
			if(ca[s2] == '{') {
				addSelector(s1, s2);
				s1 = s2;
				break;
			}
			if(Character.isWhitespace(ca[s2])) {
				addSelector(s1, s2);
				s1 = s2 = forward(ca, s2, eol);
				if(s1 == -1 || ca[s1] == '{') {
					break;
				}
			} else {
				s2 = commentCheck(this, s2);
				if(s2 >= eol) {
					s2 = eol;
				} else {
					s2++;
				}
			}
		}
		
		if(s1 != -1) {
			if(ca[s1] == '{') {
				s1 = addProperties(s1+1);
				if(ca[s1-1] == '}') { // element has been completed
					end = s1;
					return;
				}
			} else if(s2 > s1) {
				addSelector(s1, s2);
			}
		}
		
		s1 = eol;
		while(s1 < ca.length) {
			int level = 0;
			while((s1+1+level) < ca.length && ca[s1+1+level] == '\t') {
				level++;
			}
			if(this.level < level) {
				s1 = addProperties(s1+1+level);
				if(ca[s1-1] == '}') { // element has been completed, ignore the rest of line
					end = findEOL(ca, s1);
					return;
				}
			} else {
				break;
			}
		}
	
		end = (s1 < ca.length) ? s1 : ca.length;
	}
	
}
