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
package org.oobium.build.gen.migration;

import static org.oobium.utils.StringUtils.varName;

import org.oobium.utils.StringUtils;

public class JoinTable {

	public final String name;
	
	public final String table1;
	public final String tableVar1;
	public final String column1;
	public final String table2;
	public final String tableVar2;
	public final String column2;

	public JoinTable(String table1, String column1, String table2, String column2) {
		this.table1 = table1;
		this.tableVar1 = varName(table1);
		this.column1 = column1;
		this.table2 = table2;
		this.tableVar2 = varName(table2);
		this.column2 = column2;
		
		name = StringUtils.tableName(table1, column1, table2, column2);
	}

}
