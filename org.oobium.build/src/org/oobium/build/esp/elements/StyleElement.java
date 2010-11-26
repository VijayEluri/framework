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
import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.CharStreamUtils.forward;
import static org.oobium.utils.CharStreamUtils.reverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.EntryPart;
import org.oobium.utils.CharStreamUtils;

public class StyleElement extends EspElement {

	private EspPart javaType;
	private List<EspPart> args;
	private Map<String, EntryPart> entries;
	private List<StyleChildElement> children;
	
	public StyleElement(EspPart parent, int start) {
		super(parent, start);
		type = Type.StyleElement;
		if(!dom.isEsp()) {
			level = -1;
		}
		parse();
	}

	private void addArg(int start, int end) {
		if(args == null) {
			args = new ArrayList<EspPart>();
		}
		args.add(new EspPart(this, Type.ArgPart, start, end));
	}
	
	private void addArg(int start, int end, boolean isEntry) {
		if(isEntry) {
			addEntry(start, end);
		} else {
			addArg(start, end);
		}
	}

	private void addEntry(int start, int end) {
		if(entries == null) {
			entries = new TreeMap<String, EntryPart>();
		}
		EntryPart entry = new EntryPart(this, start, end);
		EspPart key = entry.getKey();
		entries.put((key == null) ? "" : key.getText(), entry);
	}
	
	private int createChild(int start) {
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
			if(children == null) {
				children = new ArrayList<StyleChildElement>();
			}
			children.add(child);
			return child.getEnd();
		}
		return ca.length;
	}

	public EspPart getArg(int index) {
		return args.get(index);
	}
	
	public List<EspPart> getArgs() {
		return args;
	}
	
	public List<StyleChildElement> getChildren() {
		return children;
	}

	public Map<String, EntryPart> getEntries() {
		return entries;
	}
	
	public EntryPart getEntry(String name) {
		return entries.get(name);
	}
	
	public EspPart getEntryKey(String name) {
		EntryPart entry = entries.get(name);
		return (entry != null) ? entry.getKey() : null;
	}

	public String getEntryText(String name) {
		EspPart part = getEntryValue(name);
		return (part != null) ? part.getText() : null;
	}
	
	public EspPart getEntryValue(String name) {
		EntryPart entry = (entries != null) ? entries.get(name) : null;
		return (entry != null) ? entry.getValue() : null;
	}
	
	public String getJavaType() {
		return (javaType != null) ? javaType.getText() : null;
	}
	
	public boolean hasArgs() {
		return args != null;
	}
	
	public boolean hasChildren() {
		return children != null;
	}
	
	public boolean hasEntries() {
		return entries != null;
	}
	
	public boolean hasEntry(String name) {
		return (entries != null) && entries.containsKey(name);
	}

	public boolean hasJavaType() {
		return javaType != null && javaType.getLength() > 0;
	}
	
	private void parse() {
		int s1 = dom.isStyle() ? start : (start + 5);
		int eol = findEOL(ca, s1);

		if(dom.isEsp()) { // if dom is ESP
			addPart(new EspPart(this, Type.TagPart, start, start+5));

			s1 = commentCheck(this, s1);
			if(s1 < eol && ca[s1] == '<') { // Type
				int s2 = closer(ca, s1, eol, true);
				if(s2 == -1) {
					s2 = eol;
				}
				javaType = new EspPart(this, Type.JavaTypePart, ++s1, s2);
				s1 = s2+1;
			}
			
			s1 = commentCheck(this, s1);
			if(s1 < eol && ca[s1] == '(') { // Args
				int s2 = closer(ca, s1, eol);
				if(s2 == -1) {
					s2 = eol;
				}
				s1++;
				if(s2 > s1 && !CharStreamUtils.isWhitespace(ca, s1, s2)) {
					parseArgs(s1, s2);
				}
				s1 = s2 + 1;
			}
		}
		
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
		
		end = (s1 < ca.length) ? s1 : ca.length;
	}
	
	private void parseArgs(int start, int end) {
		boolean isEntry = false;
		int s1 = forward(ca, start, end);
		for(int s = s1; s < end; s++) {
			if(ca[s] == '"' || ca[s] == '{') {
				int s2 = closer(ca, s, end);
				if(s2 == -1) {
					s2 = end;
				}
				s = s2;
				if(s >= end-1) {
					addArg(s1, reverse(ca, end-1) + 1, isEntry);
				}
			} else if(ca[s] == ',') {
				if(s != 0) {
					addArg(s1, reverse(ca, s-1) + 1, isEntry);
					isEntry = false;
					s1 = forward(ca, s + 1, end);
				}
			} else if(ca[s] == ':') {
				isEntry = true;
				if(s == end-1) {
					addArg(s1, s + 1, isEntry);
				}
			} else if(s == end-1) {
				addArg(s1, reverse(ca, s) + 1, isEntry);
			}
		}
	}

}
