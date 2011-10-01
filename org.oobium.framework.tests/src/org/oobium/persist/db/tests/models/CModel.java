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

@ModelDescription(
	attrs = { @Attribute(name="cName", type=String.class) }
)
public class CModel extends Model {

//	DynModel cm = DynClasses.getModel("pkg", "CModel").addAttr("cName", "String.class");

	public String getCName() {
		return (String) get("cName");
	}

	public void setCName(String cName) {
		set("cName", cName);
	}

}
