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
package org.oobium.eclipse.esp.outline;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.esp.dom.EspElement;

public class Imports {

	private List<EspElement> children;
	
	public Imports() {
		children = new ArrayList<EspElement>();
	}
	
	public void addChild(EspElement child) {
		children.add(child);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public List<EspElement> getChildren() {
		return children;
	}

}
