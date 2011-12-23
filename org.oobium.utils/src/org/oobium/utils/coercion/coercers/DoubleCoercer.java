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
package org.oobium.utils.coercion.coercers;

import java.util.Date;

public class DoubleCoercer extends AbstractCoercer {

	public Double coerce(Object object, Class<?> toType) {
		if(object instanceof Number) {
			return ((Number) object).doubleValue();
		}
		throw new UnsupportedOperationException();
	}
	
	public Double coerce(String string, Class<?> toType) {
		return (string.length() == 0) ? coerceNull(toType) : new Double(string);
	}
	
	public Double coerce(Date date, Class<?> toType) {
		return new Double(date.getTime());
	}

	@Override
	public Double coerceNull(Class<?> toType) {
		return null;
	}

	@Override
	public Class<?> getType() {
		return Double.class;
	}

}
