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
/**
 * 
 */
package org.oobium.persist.db.tests.models;

import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;

@ModelDescription(
	attrs = { @Attribute(name="iName", type=String.class) },
	hasOne = {
		@Relation(name="jModel", type=JModel.class),
		@Relation(name="includedJModel", type=JModel.class, include=true)
	}
)
public class IModel extends Model {

	public String getIName() {
		return (String) get("iName");
	}

	public void setIName(String iName) {
		set("iName", iName);
	}

	public JModel getJModel() {
		return (JModel) get("jModel");
	}
	
	public void setJModel(JModel jModel) {
		set("jModel", jModel);
	}
	
	public JModel getIncludedJModel() {
		return (JModel) get("includedJModel");
	}

	public void setIncludedJModel(JModel jModel) {
		set("includedJModel", jModel);
	}

}
