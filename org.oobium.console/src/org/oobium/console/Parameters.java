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
package org.oobium.console;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class Parameters {

	private long flags;
	List<String> list;
	Map<String, String> map;
	
	Parameters(String str) {
		this(str, true);
	}
	
	Parameters(String str, boolean parseFlags) {
		list = new ArrayList<String>();
		map = new LinkedHashMap<String, String>();
		
		if(str != null && str.length() > 0) {
			char[] ca = str.toCharArray();
			
			for(int i = 0; i < ca.length; i++) {
				if(ca[i] != ' ') {
					i = parseParam(ca, i, parseFlags);
				}
			}
		}
	}

	private int escape(char[] ca, int start) {
		for(int i = start; i < ca.length; i++) {
			if(ca[i] == '"' && ca[i-1] != '/') {
				return i;
			}
		}
		return ca.length - 1;
	}

	private long flag(char f) {
		return (1l << ((int) f - 65));
	}

	public void retainFlags(char...f) {
		long tmp = 0;
		for(char c : f) {
			tmp |= flag(c);
		}
		flags &= tmp;
	}
	
	public void clearFlags() {
		flags = 0;
	}

	public boolean hasFlags() {
		return flags != 0;
	}
	
	public int flagCount() {
		int count = 0;
		for(int i = 0; i < 64; i++) {
			if((flags & (1l << (long)i)) != 0) {
				count++;
			}
		}
		return count;
	}
	
	public boolean isSet(char f) {
		return ((flags & flag(f)) != 0);
	}
	
	public void setFlag(char f) {
		flags |= flag(f);
	}
	
	public void unsetFlag(char f) {
		flags &= ~flag(f);
	}
	
	private void addFlags(char[] ca, int start, int end) {
		if(list.isEmpty() && map.isEmpty()) {
			if(flags == 0) {
				for(int i = 0; i < (end-start); i++) {
					setFlag(ca[start+i]);
				}
			} else {
				throw new IllegalArgumentException("flags cannot be set more than once");
			}
		} else {
			throw new IllegalArgumentException("flags must be the first argument, before parameters and map entries");
		}
	}

	private int parseParam(char[] ca, int start, boolean parseFlags) {
		if(parseFlags && ca[start] == '-') {
			for(int i = start+1; i < ca.length; i++) {
				if(!Character.isLetter(ca[i])) {
					if(ca[i] == ' ') {
						addFlags(ca, start+1, i);
						return i;
					} else {
						throw new IllegalArgumentException("illegal flag: '" + ca[i] + "'");
					}
				} else if(i == ca.length-1) {
					addFlags(ca, start+1, i+1);
					return i;
				}
			}
			throw new IllegalStateException("error parsing flags");
		} else {
			int ix = -1;
			for(int i = start; i < ca.length; i++) {
				if(ca[i] == '"') {
					i = escape(ca, i+1);
				} else if(ca[i] == ':' || ca[i] == '=') {
					if(ix == -1) ix = i;
				} else if(ca[i] == ' ' || i == ca.length-1) {
					String s = (ca[i] == ' ') ? new String(ca, start, i-start) : new String(ca, start, i-start+1);
					if(ix == -1) {
						if(map.isEmpty()) {
							list.add(s);
						} else {
							throw new IllegalArgumentException("parameters must be listed before map entries");
						}
					} else {
						map.put(s.substring(0, ix-start), s.substring(ix+1-start));
					}
					return i;
				}
			}
			throw new IllegalStateException("error parsing parameters");
		}
	}
	
}
