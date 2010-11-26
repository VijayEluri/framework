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
import org.oobium.build.esp.parts.ConstructorArg;

public class ConstructorElement extends EspElement {

	private List<ConstructorArg> args;
	
	public ConstructorElement(EspPart parent, int start) {
		super(parent, start);
		this.type = Type.ConstructorElement;
		this.start = forward(ca, start);
		this.end = findEOL(ca, start);
		parse();
	}

	private void addArg(int start, int end) {
		if(args == null) {
			args = new ArrayList<ConstructorArg>();
		}
		args.add(new ConstructorArg(this, start, end));
	}
	
	public List<ConstructorArg> getArgs() {
		return args;
	}
	
	public boolean hasArgs() {
		return args != null && !args.isEmpty();
	}
	
	private void parse() {
		int i = start;
		while(i < ca.length && ca[i] != '(' && !Character.isWhitespace(ca[i])) {
			i++;
		}
		new EspPart(this, Type.TagPart, start, i);
		
		int start = find(ca, '(', this.start, this.end);
		if(start != -1) {
			int end = closer(ca, start, this.end);
			if(end == -1) {
				end = this.end;
			}
			
			int s1 = forward(ca, start+1, end-1);
			if(s1 != -1) {
				int s = s1;
				if(s != -1) {
					while(s < end) {
						switch(ca[s]) {
						case '<':
						case '"':
							s = closer(ca, s, end) + 1;
							if(s == 0) {
								s = end;
							}
							break;
						case ',':
							int s2 = reverse(ca, s-1) + 1;
							addArg(s1, s2);
							s = forward(ca, s+1, end);
							if(s == -1) {
								s = end;
							}
							s1 = s;
							break;
						default:
							s++;
						}
					}
					if(s > s1) {
						int s2 = reverse(ca, s);
						if(s2 == -1) {
							s2 = end;
						}
						addArg(s1, s2);
					}
				}
			}
		}
	}

}
