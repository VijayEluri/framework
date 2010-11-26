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
package org.oobium.build.gen.db;

import java.util.ArrayList;
import java.util.List;

import org.oobium.build.model.ModelRelation;
import org.oobium.utils.StringUtils;

public class JoinTableTemplate {

	private String name;
	private String[] columns;
	private String[] refTables;
	
	public JoinTableTemplate(ModelRelation relation1, ModelRelation relation2) {
		String m1 = relation1.getModel().getSimpleName();
		String r1 = relation1.getName();
		String m2 = relation1.getSimpleType();
		String r2 = (relation2 != null) ? relation2.getName() : "null";

		name = StringUtils.tableName(m1, r1, m2, r2);
		columns = StringUtils.columnNames(m1, r1, m2, r2);
		refTables = StringUtils.tableNames(m1, m2);
	}

	public String getCreateSQL() {
        return "CREATE TABLE " + name +
        " (" +
    		columns[0] + " INT CONSTRAINT " +
    		name + "__" + columns[0] + "_FK REFERENCES " + refTables[1] + " (id) ON DELETE CASCADE" +
    		", " +
    		columns[1] + " INT CONSTRAINT " +
    		name + "__" + columns[1] + "_FK REFERENCES " + refTables[0] + " (id) ON DELETE CASCADE" +
    	")";
	}

	public List<String> getIndexSQL() {
		List<String> ixs = new ArrayList<String>();
//		Examples:
//			CREATE INDEX idx_categories_posts__posts_categories_categories_posts ON categories_posts__posts_categories(categories_posts)
//			CREATE INDEX idx_categories_posts__posts_categories_posts_categories ON categories_posts__posts_categories(posts_categories)
		ixs.add("CREATE INDEX idx_" + name + "_" + columns[0] + " ON " + name + "(" + columns[0] + ")");
		ixs.add("CREATE INDEX idx_" + name + "_" + columns[1] + " ON " + name + "(" + columns[1] + ")");
		return ixs;
	}

	public boolean hasIndex() {
		return true;
	}

	public String name() {
		return name;
	}

	
}
