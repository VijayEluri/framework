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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.oobium.build.esp.EspPart.Type.*;

import org.junit.Ignore;
import org.junit.Test;
import org.oobium.build.esp.EspDom;
import org.oobium.build.esp.EspElement;
import org.oobium.build.esp.EspPart.Type;
import org.oobium.build.esp.elements.CommentElement;
import org.oobium.build.esp.elements.ConstructorElement;
import org.oobium.build.esp.elements.HtmlElement;
import org.oobium.build.esp.elements.ImportElement;
import org.oobium.build.esp.elements.InnerTextElement;
import org.oobium.build.esp.elements.JavaElement;
import org.oobium.build.esp.parts.JavaPart;

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
