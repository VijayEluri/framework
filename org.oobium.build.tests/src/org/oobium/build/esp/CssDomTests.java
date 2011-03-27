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
package org.oobium.build.esp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CssDomTests {

	private EspDom dom(String esp) {
		return new EspDom("application.css", esp);
	}
	
	private EspElement elem(String esp) {
		return elem(esp, 0);
	}
	
	private <T> T elem(String esp, Class<T> type) {
		return (T) elem(esp, 0);
	}
	
	private EspElement elem(String esp, int index) {
		return dom(esp).get(index);
	}
	
	private <T> T elem(String esp, int index, Class<T> type) {
		return (T) dom(esp).get(index);
	}

	
	@Test
	public void testSingleClass() throws Exception {
		assertEquals(1, dom(".test { color: red }").size());
	}
	
}
