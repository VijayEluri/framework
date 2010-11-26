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
package org.oobium.persist.db.internal;

class Cell {

	String column;
	int type;
	Object value;
	boolean isQuery;

	Cell(String column, int type, Object value) {
		this.column = column;
		this.type = type;
		this.value = value;
		this.isQuery = false;
	}
	
	Cell(String column, String query) {
		this.column = column;
		this.value = query;
		this.isQuery = true;
	}

	public String query() {
		return (String) value;
	}
	
	@Override
	public String toString() {
		return column + " <- " + value;
	}
	
}
