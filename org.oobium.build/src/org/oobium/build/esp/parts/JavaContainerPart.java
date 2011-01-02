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

import static org.oobium.utils.CharStreamUtils.closer;

import org.oobium.build.esp.EspPart;

public class JavaContainerPart extends EspPart {

	public JavaContainerPart(EspPart parent, Type type, int start, int end) {
		super(parent, type, start, end);
		parse();
	}

	@Override
	public String getText() {
		return super.getText().replace("\\{", "{");
	}
	
	protected void parse() {
		for(int s1 = start; s1 < end; s1++) {
			if(ca[s1] == '{' && (s1 == 0 || ca[s1-1] != '\\')) {
				int s2 = closer(ca, s1, end) + 1;
				if(s2 == 0) {
					s2 = end;
				}
				new JavaPart(this, s1, s2);
				s1 = s2 - 1;
			}
		}
	}

}
