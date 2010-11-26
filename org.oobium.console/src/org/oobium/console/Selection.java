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


class Selection {

	int x1, x2, y1, y2;
	
	Selection() {
		x1 = y1 = x2 = y2 = -1;
	}

	boolean isValid() {
		return x1 != -1 && y1 != -1 && x2 != -1 && y2 != -1;
	}
	
	@Override
	public String toString() {
		return "{" + x1 + "," + y1 + "} {" + x2 + "," + y2 + "}";
	}
	
}
