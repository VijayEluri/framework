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
	attrs = { @Attribute(name="jName", type=String.class) },
	hasOne = {
		@Relation(name="bModel", type=BModel.class),
		@Relation(name="includedBModel", type=BModel.class, include=true)
	}
)
public class JModel extends Model {

//	DynModel jm = DynClasses.getModel("pkg", "JModel")
//											.addAttr("jName", "String.class")
//											.addHasOne("bModel", "BModel.class")
//											.addHasMany("includedBModel", "BModel.class", "include=true");

	public String getJName() {
		return (String) get("jName");
	}

	public void setJName(String jName) {
		set("jName", jName);
	}

	public BModel getBModel() {
		return (BModel) get("bModel");
	}

	public void setBModel(BModel bModel) {
		set("bModel", bModel);
	}

	public BModel getIncludedBModel() {
		return (BModel) get("includedBModel");
	}

	public void setIncludedBModel(BModel bModel) {
		set("includedBModel", bModel);
	}

}
