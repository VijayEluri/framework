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
package myapp.models;

import org.oobium.persist.Attribute;
import org.oobium.persist.Model;
import org.oobium.persist.ModelDescription;
import org.oobium.persist.Relation;

@ModelDescription(
	attrs = {
		@Attribute(name="description",type=String.class)
	},
	hasOne = {
		@Relation(name="m2", type=Model2.class),
		@Relation(name="parent", type=Model1.class, opposite="children"),
		@Relation(name="m2OneToOne", type=Model2.class, opposite="m1OneToOne")
	},
	hasMany = {
		@Relation(name="m2s", type=Model2.class),
		@Relation(name="children", type=Model1.class, opposite="parent"),
		@Relation(name="m2ManyToMany", type=Model2.class, opposite="m1ManyToMany")
	}
)
public class Model1 extends Model {
	// extended model
}
