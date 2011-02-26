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

import org.oobium.build.model.ModelRelation;
import org.oobium.utils.StringUtils;

public class JoinTable {

	public final String name;
	
	public final String table1;
	public final String column1;
	public final String table2;
	public final String column2;

	public JoinTable(ModelRelation relation1, ModelRelation relation2) {
		table1 = relation1.model.getSimpleType();
		column1 = relation1.name;
		table2 = relation1.getSimpleType();
		column2 = (relation2 != null) ? relation2.name : "null";
		
		name = StringUtils.tableName(table1, column1, table2, column2);
	}

}
