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

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Indexes {

	/**
	 * <p>An array of indexes to be explicitly created on the table to which this class is persisted.<p>
	 * <p>Use the field names as given in the ModelDefinition. To create a multi-field index use a comma separated list.
	 * To create a Unique index, precede the fields with "Unique-" (case sensitive).<p>
	 * <p>Examples:</br>
	 * <code>@Indexes({"id,lastName","firstName"})</code></br>
	 * <code>@Indexes({"Unique-group,alias"})</code></br>
	 * </p>
	 * <p><b>NOTE:</b> <code>hasMany</code> fields cannot be used in indexes because the actual persisted field is
	 * not contained within the same table as the rest of the class.<p>
	 */
	String[] value();
	
}
