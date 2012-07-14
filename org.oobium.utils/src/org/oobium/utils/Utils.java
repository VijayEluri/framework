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
package org.oobium.utils;

import org.oobium.utils.coercion.TypeCoercer;


public class Utils {

	/**
	 * Checks for equality in a null safe manner.
	 * If the two objects are of difference types, an attempt is made
	 * to coerce o2 into the type of o1.
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean isEqual(Object o1, Object o2) {
		if(o1 == null) {
			return o2 == null;
		} else {
			if(o2 == null) {
				return false;
			}
			if(o1.getClass() == o2.getClass()) {
				return o1.equals(o2);
			}
			return o1.equals(TypeCoercer.coerce(o2).to(o1.getClass()));
		}
	}
	
    public static boolean notEqual(Object o1, Object o2) {
		return ((o1 == null && o2 != null) || (o1 != null && !o1.equals(o2)));
	}

}
