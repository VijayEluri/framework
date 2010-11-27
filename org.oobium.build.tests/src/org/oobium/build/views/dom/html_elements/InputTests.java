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
package org.oobium.build.views.dom.html_elements;

import static org.junit.Assert.*;
import static org.oobium.utils.StringUtils.getterName;
import static org.oobium.utils.StringUtils.hasserName;

import org.junit.Test;



public class InputTests {

	@Test
	public void testSetValue() throws Exception {
		String model = "member";
		String[] field = { "location", "address1", "street" };
		
		StringBuilder sb = new StringBuilder();
		if(field.length == 1) {
			sb.append(model).append('.').append(getterName(field[0])).append('(').append(')');
		} else {
			int last = field.length - 1;
			for(int i = 0; i < field.length; i++) {
				sb.append(model);
				for(int j = 0; j <= i; j++) {
					sb.append('.').append((j == i && j != last) ? hasserName(field[j]) : getterName(field[j])).append("()");
				}
				if(i < last) {
					sb.append("?(");
				}
			}
			for(int i = 0; i < last; i++) {
				sb.append("):\"\"");
			}
		}
		System.out.println(sb);
		assertEquals("member.hasLocation()?(member.getLocation().hasAddress1()?(member.getLocation().getAddress1().getStreet()):\"\"):\"\"", sb.toString());
	}
	
}
