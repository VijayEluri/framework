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

public class LongCoercer extends AbstractCoercer {

	public Long coerce(Object object, Class<?> toType) {
		if(object instanceof Number) {
			return ((Number) object).longValue();
		}
		if(object instanceof JsonModel) {
			return coerce((JsonModel) object, toType);
		}
		if(object instanceof Enum) {
			return coerce((Enum<?>) object, toType);
		}
		throw new UnsupportedOperationException();
	}
	
	public Long coerce(String string, Class<?> toType) {
		return (string.length() == 0) ? coerceNull(toType) : new Long(string);
	}
	
	public Long coerce(Enum<?> e, Class<?> toType) {
		return new Long(e.ordinal());
	}
	
	public Long coerce(Date date, Class<?> toType) {
		return date.getTime();
	}

	public Long coerce(JsonModel model, Class<?> toType) {
		return TypeCoercer.coerce(model.getId(), Long.class);
	}
	
	@Override
	public Long coerceNull(Class<?> toType) {
		return null;
	}

	@Override
	public Class<?> getType() {
		return Long.class;
	}
	
}
