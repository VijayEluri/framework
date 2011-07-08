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
	attrs = {
		@Attribute(name="dName", type=String.class)
	},
	hasOne = {
		@Relation(name="fModel", type=FModel.class, opposite="dModels")
	},
	hasMany = {
		@Relation(name="eModels", type=EModel.class, opposite="dModels")
	}
)
public class DModel extends Model {

	public String getDName() {
		return (String) get("dName");
	}

	public void setDName(String dName) {
		set("dName", dName);
	}

	public FModel getFModel() {
		return (FModel) get("fModel");
	}

	public void setFModel(FModel fModel) {
		set("fModel", fModel);
	}

	public Set<EModel> eModels() {
		return (Set<EModel>) get("eModels");
	}

}
