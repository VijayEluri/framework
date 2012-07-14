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

import java.sql.Time;
import java.util.Calendar;
import java.util.Map;

import org.oobium.utils.coercion.TypeCoercer;


public class TimeCoercer extends AbstractCoercer {

	public Time coerce(Object object, Class<?> toType) {
		if(object instanceof Number) {
			return new Time(((Number) object).longValue());
		}
		if(object instanceof Map<?,?>) {
			return coerce((Map<?,?>) object, toType);
		}
		throw new UnsupportedOperationException();
	}

	public Time coerce(Map<?, ?> map, Class<?> toType) {
		return new Time(TypeCoercer.coerce(map).to(Calendar.class).getTimeInMillis());
	}
	
	public Time coerce(String str, Class<?> toType) {
		try {
			if(str.startsWith("/Date(")) {
				return new Time(Long.parseLong(str.substring(6, str.length()-1)));
			}
			return new Time(Long.parseLong(str));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(str + " failed to parse correctly when trying to coerce into a Date)");
		}
	}
	
	@Override
	public Class<?> getType() {
		return Time.class;
	}

}
