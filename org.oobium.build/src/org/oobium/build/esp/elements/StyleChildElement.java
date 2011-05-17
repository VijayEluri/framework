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

import static org.oobium.build.esp.parts.EmbeddedJavaPart.*;
import static org.oobium.build.esp.elements.StyleElement.findEOL;
import static org.oobium.utils.CharStreamUtils.closer;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.isWhitespace;
import static org.oobium.utils.CharStreamUtils.reverse;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.CommentPart;
import org.oobium.build.esp.parts.StylePropertyPart;

public class StyleChildElement extends EspElement {

	private List<EspPart> selectorGroups;
	private List<StylePropertyPart> properties;
	
	public StyleChildElement(StyleElement parent, int start) {
		super(parent, start);
		type = Type.StyleChildElement;
		parse();
	}

	private int addProperties(int start) {
		int s1 = start;
		int s2 = start;
		// static CSS files don't use levels
		int eol = dom.isStatic() ? ca.length : findEOL(ca, start);
		while(s2 < eol) {
			s2 = skipEmbeddedJava(ca, s2, eol);
			if(s2 < eol) {
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
					int s = commentCheck(this, s2);
					if(s >= eol) {
						addProperty(s1, s2);
						return s;
					} else {
						s2 = s + 1;
					}
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
	
	private void addSelectorGroup(int start, int end) {
		end = reverse(ca, end-1) + 1;
		if(end > start && !isWhitespace(ca, start, end)) {
			if(selectorGroups == null) {
				selectorGroups = new ArrayList<EspPart>();
			}
			selectorGroups.add(new EspPart(this, Type.StyleSelectorPart, start, end));
		}
	}
	
	protected int commentCheck(EspPart parent, char[] ca, int ix) {
		if(ix >= 0) {
			if(ix < ca.length) {
				if(ca[ix] == '"') {
					ix = closer(ca, ix, ca.length, true, true);
					if(ix == -1) {
						ix = ca.length;
					}
				} else if(ca[ix] == '/' && (ca[ix+1] == '*' || ca[ix+1] == '/')) {
					CommentPart comment = new CommentPart(parent, ix);
					ix = comment.getEnd();
				}
			}
		}
		return ix;
	}
	
	public EspElement getElement() {
		return (EspElement) parent;
	}

	public List<StylePropertyPart> getProperties() {
		return properties;
	}
	
	public List<EspPart> getSelectorGroups() {
		return selectorGroups;
	}
	
	public boolean hasProperties() {
		return properties != null;
	}
	
	public boolean hasSelectors() {
		return selectorGroups != null;
	}
	
	private void parse() {
		int s1 = start;
		int eol = findEOL(ca, s1);
		
		int s2 = s1;
		while(s2 < eol) {
			if(ca[s2] == '{') {
				addSelectorGroup(s1, s2);
				s1 = s2;
				break;
			}
			if(ca[s2] == ',') {
				addSelectorGroup(s1, s2);
				s1 = forward(ca, s2+1, eol);
				if(s1 == -1) {
					s2 = eol;
				} else {
					s2 = s1;
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
				if(ca[s2-1] == '$') {
					s2 = closer(ca, s2, ca.length, true);
					if(s2 == -1) {
						s2 = ca.length;
					}
				} else {
					s1 = addProperties(s1+1);
					if(ca[s1-1] == '}') { // element has been completed
						end = s1;
						return;
					}
				}
			} else if(s2 > s1) {
				addSelectorGroup(s1, s2);
			}
		}
		
		s1 = eol;
		if(dom.isStatic()) { // does not use levels...
			while(s1 < ca.length) {
				s1 = forward(ca, s1);
				if(s1 == -1) {
					end = ca.length;
					return;
				}
				s1 = addProperties(s1+1);
				if(ca[s1-1] == '}') { // element has been completed, ignore the rest of line
					end = findEOL(ca, s1);
					return;
				}
			}
		} else {
			while(s1 < ca.length) {
				int level = 0;
				while((s1+1+level) < ca.length && ca[s1+1+level] == '\t') {
					level++;
				}
				if(this.level < level) {
					s1 = addProperties(s1+1+level);
				} else {
					break;
				}
			}
		}
		
		end = (s1 < ca.length) ? s1 : ca.length;
	}
	
}
