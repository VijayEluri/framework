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

import static org.oobium.utils.StringUtils.getCalendarField;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.oobium.utils.coercion.TypeCoercer;

public class CalendarCoercer extends AbstractCoercer {

	public Calendar coerce(Object object, Class<?> toType) {
		if(object instanceof Map<?,?>) {
			return coerce((Map<?,?>) object, toType);
		}
		throw new UnsupportedOperationException();
	}
	
	public Calendar coerce(Map<?,?> data, Class<?> toType) {
		Locale locale = TypeCoercer.coerce(data.get("locale"), Locale.class);
		TimeZone zone = TypeCoercer.coerce((String) (data.containsKey("Z") ? data.get("Z") : data.get("z")), TimeZone.class);
		Calendar cal = GregorianCalendar.getInstance(zone, locale);
		for(Entry<?,?> entry : data.entrySet()) {
			String key = (String) entry.getKey();
			int field = getCalendarField(key.charAt(0));
			if(field >= 0 && field < Calendar.FIELD_COUNT) {
				try {
					Integer value = TypeCoercer.coerce(entry.getValue(), Integer.class);
					if(value != null) {
						cal.set(field, value);
					}
				} catch(IllegalArgumentException e) {
					// skip it
				}
			}
		}
		return cal;
	}

	@Override
	public Class<?> getType() {
		return Calendar.class;
	}
	
}
