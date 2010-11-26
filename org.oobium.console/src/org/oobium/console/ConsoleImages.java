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

import org.eclipse.swt.graphics.Image;

public enum ConsoleImages {

	COPY("/icons/copy.gif"),
	PASTE("/icons/paste.gif"),
	CLOSE("/icons/close.gif"),
	ERROR("/icons/error.gif"),
	NEXT("/icons/next.gif"),
	PREV("/icons/prev.gif");
	
	private String resource;
	
	private ConsoleImages(String resource) {
		this.resource = resource;
	}

	public Image getImage() {
		return Resources.getImage(resource);
	}
	
}
