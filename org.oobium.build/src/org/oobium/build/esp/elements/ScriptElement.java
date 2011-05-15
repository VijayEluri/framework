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

import static org.oobium.utils.CharStreamUtils.findEOL;
import static org.oobium.utils.CharStreamUtils.forward;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspPart;
import org.oobium.build.esp.parts.ScriptPart;

public class ScriptElement extends MarkupElement {

	private List<ScriptPart> lines;
	
	public ScriptElement(EspPart parent, int start) {
		super(parent, start);
	}

	private void addLine(int start, int end) {
		if(lines == null) {
			lines = new ArrayList<ScriptPart>();
		}
		lines.add(new ScriptPart(this, start, end));
	}
	
	public List<ScriptPart> getLines() {
		return lines;
	}
	
	public boolean hasLines() {
		return lines != null;
	}
	
	protected void parse() {
		if(!dom.isEsp()) {
			level = -1;
		}
		
		int s1 = dom.isScript() ? start : (start + 6);
		int eoe = findEOL(ca, s1);

//		tag = new EspPart(this, Type.TagPart, start, end);
		type = Type.ScriptElement;
		style = JAVA_TYPE | ARGS | ENTRIES | CHILDREN | CLOSING_TAG;

		if(dom.isEsp()) { // if dom is ESP
			addPart(new EspPart(this, Type.TagPart, start, start+6));
			
			s1 = parseJavaType(s1, eoe);
			
			s1 = parseArgsAndEntries(s1, eoe);

			s1 = commentCheck(this, s1);
		}
		
		s1 = forward(ca, s1);
		if(s1 == -1) {
			end = ca.length;
		} else {
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
	}
	
}
