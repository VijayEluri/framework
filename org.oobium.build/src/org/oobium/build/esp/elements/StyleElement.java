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

import static org.oobium.utils.CharStreamUtils.closer;

import org.oobium.build.esp.EspPart;

public class StyleElement extends MarkupElement {

	/**
	 * Find the end of this element. May actually span multiple lines if there is a multi-line comment.
	 */
	public static int findEOE(char[] ca, int offset) {
		for(int i = offset; i < ca.length; i++) {
			switch(ca[i]) {
			case '\n':
				return i;
			case '*':
				if(ca[i-1] == '/') {
					i += 2;
					for( ; i < ca.length; i++) {
						if(ca[i] == '/' && ca[i-1] == '*') {
							break;
						}
					}
				}
				break;
			case '{':
				if(ca[i-1] == '$') {
					i = closer(ca, i, ca.length, true);
					if(i == -1) {
						i = ca.length;
					}
				}
				break;
			}
		}
		return ca.length;
	}

	
	public StyleElement(EspPart parent, int start) {
		super(parent, start);
	}
	
	protected int createChild(int start) {
		int s = commentCheck(this, start);
		if(s != start) {
			// comment found - skip to the start of the next line
			while(s < ca.length) {
				if(ca[s] == '\n') {
					s++;
					break;
				}
				s++;
			}
		}
		if(s < ca.length) {
			StyleChildElement child = new StyleChildElement(this, s);
			addChild(child);
			return child.getEnd();
		}
		return ca.length;
	}

	protected void parse() {
		if(!dom.isEsp()) {
			level = -1;
		}

		int s1 = dom.isStyle() ? start : (start + 5);
		int eol = findEOE(ca, s1);

		type = Type.StyleElement;
		style = JAVA_TYPE | ARGS | ENTRIES | CHILDREN | CLOSING_TAG;

		if(dom.isEsp()) { // if dom is ESP
			addPart(new EspPart(this, Type.TagPart, start, start+5));

			s1 = parseJavaType(s1, eol);
			
			s1 = parseArgsAndEntries(s1, eol);
		}

		s1 = parseChildren(s1, eol);

		end = (s1 < ca.length) ? s1 : ca.length;
	}
	
	protected int parseChildren(int start, int eol) {
		int s1 = commentCheck(this, start);
		while(s1 < eol) {
			if(Character.isWhitespace(ca[s1])) {
				s1++;
			} else {
				s1 = createChild(s1);
			}
		}
		
		if(s1 < ca.length && ca[s1] == '\n') {
			while(s1 < ca.length) {
				int level = 0;
				while((s1+1+level) < ca.length && ca[s1+1+level] == '\t') {
					level++;
				}
				if(this.level < level) {
					s1 = s1 + 1;
					int s2 = s1;
					while(s2 < ca.length) {
						if(ca[s2] == '\n') {
							break; // empty line, skip it and go on to the next one
						} else if(Character.isWhitespace(ca[s2])) {
							s2++;
							if(s2 == ca.length) {
								s1 = ca.length;// empty line and end of document
							}
						} else {
							s1 = createChild(s1);
							break;
						}
					}
				} else {
					break;
				}
			}
		}
		return s1;
	}
	
}
