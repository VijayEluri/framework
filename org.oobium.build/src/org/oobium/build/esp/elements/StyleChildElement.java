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
import static org.oobium.utils.CharStreamUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.CommentPart;
import org.oobium.build.esp.parts.StylePropertyPart;

public class StyleChildElement extends EspElement {

	private List<EspPart> selectorGroups;
	private List<StylePropertyPart> properties;
	private List<EspElement> children;
	
	public StyleChildElement(StyleElement parent, int start) {
		this((EspElement) parent, start);
	}

	public StyleChildElement(StyleChildElement parent, int start) {
		this((EspElement) parent, start);
	}

	private StyleChildElement(EspElement parent, int start) {
		super(parent, start);
		type = Type.StyleChildElement;
		if(start > 0) { // start == 0 if 1st selector is the 1st char of an ESS file
			level = 0;
			for(int i = start; ca[i-1] != '\n'; i--) {
				level++;
			}
		}
		parse();
	}

	private int addChildrenAndProperties(int start) {
		int s1 = start;
		int s2 = start;
		// static CSS files don't use levels
		int eol = dom.isStatic() ? ca.length : findEOL(ca, start);
		while(s2 < eol) {
			s2 = skipEmbeddedJava(ca, s2, eol);
			if(s2 < eol) {
				if(ca[s2] == '}') {
					return addChildOrProperty(s1, s2) + 1;
				}
				if(ca[s2] == ';') {
					s1 = s2 = addChildOrProperty(s1, s2) + 1;
				} else {
					int s = commentCheck(this, s2);
					if(s >= eol) {
						return addChildOrProperty(s1, s2);
					} else {
						s2 = s + 1;
					}
				}
			}
		}
		return addChildOrProperty(s1, s2);
	}
	
	private int addChildOrProperty(int start, int end) {
		int s1 = forward(ca, start, end);
		int s2 = reverse(ca, end-1) + 1;
		if(s1 != -1 && s2 > s1) {
			int ix = find(ca, ':', s1, s2);
			if(ix != -1) {
				if(ix == s1 || (ix+1 < ca.length && ix == s1+1 && ca[s1] == '&')) {
					ix = -1; // it's a pseudo-class: treat as if ':' wasn't found
				}
			}
			if(ix == -1) {
				StyleChildElement child = new StyleChildElement(this, s1);
				if(children == null) {
					children = new ArrayList<EspElement>();
				}
				children.add(child);
				return child.getEnd();
			} else {
				if(properties == null) {
					properties = new ArrayList<StylePropertyPart>();
				}
				properties.add(new StylePropertyPart(this, s1, s2));
			}
		}
		return end;
	}
	
	private void addSelectorGroup(int start, int end) {
		start = forward(ca, start, end);
		end = reverse(ca, end-1) + 1;
		if(start != -1 && end > start) {
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
	
	public List<EspElement> getChildren() {
		return children;
	}
	
	public EspElement getElement() {
		return (EspElement) parent;
	}

	public String getLastSelectorText() {
		return selectorGroups.get(selectorGroups.size()-1).getText();
	}
	
	public List<StylePropertyPart> getProperties() {
		return properties;
	}
	
	public List<EspPart> getSelectorGroups() {
		return selectorGroups;
	}
	
	public boolean hasChildren() {
		return children != null;
	}
	
	public boolean hasProperties() {
		return properties != null;
	}
	
	public boolean hasSelectors() {
		return selectorGroups != null;
	}

	public boolean isMixin() {
		return (parent instanceof StyleChildElement)
				&& !hasProperties() && !hasChildren()
				&& selectorGroups != null && selectorGroups.size() == 1
				&& (selectorGroups.get(0).charAt(0) == '.' || selectorGroups.get(0).charAt(0) == '#');
	}
	
	public boolean isNestedChild() {
		return (parent instanceof StyleChildElement) && (hasProperties() || hasChildren());
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
					s1 = addChildrenAndProperties(s1+1);
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
				s1 = addChildrenAndProperties(s1+1);
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
					s1 = addChildrenAndProperties(s1+1+level);
				} else {
					break;
				}
			}
		}
		
		end = (s1 < ca.length) ? s1 : ca.length;
	}
	
}
