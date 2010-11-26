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
		@Attribute(name="name",type=String.class),
		@Attribute(name="description",type=String.class)
	},
	hasOne = {
		@Relation(name="m1", type=Model1.class),
		@Relation(name="m1OneToOne", type=Model1.class, opposite="m2OneToOne")
	},
	hasMany = {
		@Relation(name="m1s", type=Model1.class),
		@Relation(name="m1ManyToMany", type=Model1.class, opposite="m2ManyToMany")
	}
)
public class Model2 extends Model {
	// extended model
}
