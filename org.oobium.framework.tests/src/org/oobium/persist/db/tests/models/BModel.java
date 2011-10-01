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
	attrs = { @Attribute(name="bName", type=String.class) },
	hasOne = { @Relation(name="cModel", type=CModel.class) }
)
public class BModel extends Model {

//	DynModel bm = DynClasses.getModel("pkg", "BModel").addAttr("bName", "String.class").addHasOne("cModel", "CModel.class");
	
	public String getBName() {
		return (String) get("bName");
	}

	public void setBName(String bName) {
		set("bName", bName);
	}

	public CModel getCModel() {
		return (CModel) get("cModel");
	}

	public void setCModel(CModel cModel) {
		set("cModel", cModel);
	}

}
