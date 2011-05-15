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

import static org.oobium.build.esp.parts.EmbeddedJavaPart.skipEmbeddedJava;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.EspPart;

public class StyleEntryPart extends EspPart {

	private List<StylePropertyPart> properties;

	public StyleEntryPart(EspPart parent, int start, int end) {
		super(parent, Type.StyleEntryPart, start, end);
		parse();
	}

	private void addProperty(int start, int end) {
		if(properties == null) {
			properties = new ArrayList<StylePropertyPart>();
		}
		properties.add(new StylePropertyPart(this, start, end));
	}
	
	public boolean isSimple() {
		return !hasParts();
	}

	private int findStart() {
		int s = start + 1;
		while(s < end) {
			if(ca[s] == '{') {
				return s;
			}
			s = skipEmbeddedJava(ca, s, end);
			s++;
		}
		return start + 1;
	}
	
	public List<StylePropertyPart> getProperties() {
		return properties;
	}
	
	protected void parse() {
		int s1 = findStart();
		int s2 = s1;
		while(s2 < end - 1) {
			s2 = skipEmbeddedJava(ca, s2, end);
			if(s2 < end) {
				if(ca[s2] == '}') {
					break;
				}
				if(ca[s2] == ';') {
					if(s2 > s1) {
						addProperty(s1, s2);
					}
					s1 = s2 = s2 + 1;
				} else {
					s2++;
				}
			}
		}
		if(s2 > s1) {
			addProperty(s1, s2);
		}
	}

}
