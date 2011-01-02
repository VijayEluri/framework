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
package org.oobium.persist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModelDescription {

	public static final String ID = "id";
	public static final String SUPER_ID = "superId";

	public static final String RESOLVED   = "resolved";

	public static final String CREATED_AT = "createdAt";
	public static final String UPDATED_AT = "updatedAt";
	public static final String CREATED_ON = "createdOn";
	public static final String UPDATED_ON = "updatedOn";
	
	Attribute[] attrs() default {};
	Relation[] hasMany() default {};
	Relation[] hasOne() default {};
	Validate[] validations() default {};
	
	boolean datestamps() default false;
	boolean timestamps() default false;
	
	boolean allowDelete() default true;
	boolean allowUpdate() default true;

}
