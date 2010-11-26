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

public class ScriptElement extends EspElement {

	private EspPart javaType;
	private List<EspPart> args;
	private Map<String, EntryPart> entries;
	private List<EspPart> lines;
	
	public ScriptElement(EspPart parent, int start) {
		super(parent, start);
		type = Type.ScriptElement;
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
	
	private void addLine(int start, int end) {
		if(lines == null) {
			lines = new ArrayList<EspPart>();
		}
		lines.add(new EspPart(this, Type.ScriptPart, start, end));
	}
	
	public EspPart getArg(int index) {
		return args.get(index);
	}

	public List<EspPart> getArgs() {
		return args;
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
	
	public List<EspPart> getLines() {
		return lines;
	}
	
	public boolean hasArgs() {
		return args != null;
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

	public boolean hasLines() {
		return lines != null;
	}
	
	private void parse() {
		int s1 = dom.isScript() ? start : (start + 6);
		int eoe = findEOL(ca, s1);

		if(dom.isEsp()) { // if dom is ESP
			addPart(new EspPart(this, Type.TagPart, start, start+6));
			
			s1 = commentCheck(this, s1);
			if(s1 < eoe && ca[s1] == '<') { // Type
				int s2 = closer(ca, s1, eoe, true);
				if(s2 == -1) {
					s2 = eoe;
				}
				javaType = new EspPart(this, Type.JavaTypePart, ++s1, s2);
				s1 = s2+1;
			}
			
			s1 = commentCheck(this, s1);
			if(s1 < eoe && ca[s1] == '(') { // Args
				int s2 = closer(ca, s1, eoe);
				if(s2 == -1) {
					s2 = eoe;
				}
				s1++;
				if(s2 > s1 && !CharStreamUtils.isWhitespace(ca, s1, s2)) {
					parseArgs(s1, s2);
				}
				s1 = s2 + 1;
			}

			s1 = commentCheck(this, s1);
		}
		
		if(eoe > s1) {
			addLine(s1, eoe);
		}
		
		s1 = eoe;
		while(s1 < ca.length) {
			int level = 0;
			while((s1+1+level) < ca.length && ca[s1+1+level] == '\t') {
				level++;
			}
			if(this.level < level) {
				int eol = findEOL(ca, s1+1);
				addLine(s1+1, eol);
				s1 = eol;
			} else {
				break;
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
