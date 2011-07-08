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

import java.util.Set;

import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;

@ModelDescription(
	attrs = { @Attribute(name="aName", type=String.class) },
	hasOne = {
		@Relation(name="bModel", type=BModel.class),
		@Relation(name="cModel", type=CModel.class)
	},
	hasMany = { @Relation(name="bModels", type=BModel.class) }
)
public class AModel extends Model {

	public String getAName() {
		return (String) get("aName");
	}

	public void setAName(String aName) {
		set("aName", aName);
	}

	public BModel getBModel() {
		return (BModel) get("bModel");
	}

	public void setBModel(BModel bModel) {
		set("bModel", bModel);
	}

	public CModel getCModel() {
		return (CModel) get("cModel");
	}

	public void setCModel(CModel cModel) {
		set("cModel", cModel);
	}

	public Set<BModel> bModels() {
		return (Set<BModel>) get("bModels");
	}

}
