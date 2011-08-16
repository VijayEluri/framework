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

import org.oobium.utils.coercion.TypeCoercer;
import org.oobium.utils.json.JsonModel;

public class IntegerCoercer extends AbstractCoercer {

	public Integer coerce(Object object, Class<?> toType) {
		if(object instanceof Number) {
			return ((Number) object).intValue();
		}
		if(object instanceof JsonModel) {
			return coerce((JsonModel) object, toType);
		}
		if(object instanceof Enum) {
			return coerce((Enum<?>) object, toType);
		}
		throw new UnsupportedOperationException();
	}
	
	public Integer coerce(String string, Class<?> toType) {
		if("null".equals(string)) return coerceNull();
		return (string.length() == 0) ? coerceNull() : new Integer(string);
	}
	
	public Integer coerce(Enum<?> e, Class<?> toType) {
		return new Integer(e.ordinal());
	}
	
	// TODO should this only return to the seconds resolution?
	public Integer coerce(Date date, Class<?> toType) {
		return (int) date.getTime();
	}
	
	public Integer coerce(JsonModel model, Class<?> toType) {
		return TypeCoercer.coerce(model.getId(), Integer.class);
	}

	@Override
	public Integer coerceNull() {
		return null;
	}

	@Override
	public Class<?> getType() {
		return Integer.class;
	}

}
