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
package org.oobium.server;

import java.nio.channels.SelectionKey;

class Read {
	
	final SelectionKey key;
	final byte[] data;

	Read(SelectionKey key, byte[] data) {
		this.key = key;
		this.data = data;
	}
	
	@Override
	public String toString() {
		return key.toString() + " [" + new String(data) + "]";
	}
	
}
