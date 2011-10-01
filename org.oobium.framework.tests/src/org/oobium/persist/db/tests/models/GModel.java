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
	attrs = {
		@Attribute(name="gName", type=String.class)
	},
	hasOne = {
		@Relation(name="hModel", type=HModel.class, opposite="gModels")
	}
)
public class GModel extends Model {

//	DynModel gm = DynClasses.getModel("pkg", "GModel")
//											.addAttr("gName", "String.class")
//											.addHasOne("hModel", "HModel.class", "opposite=gModels");

	public String getGName() {
		return (String) get("gName");
	}

	public void setGName(String gName) {
		set("gName", gName);
	}

	public HModel getHModel() {
		return (HModel) get("hModel");
	}

	public void setHModel(HModel hModel) {
		set("hModel", hModel);
	}

}
