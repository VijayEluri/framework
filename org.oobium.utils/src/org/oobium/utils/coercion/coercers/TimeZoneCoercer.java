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

import java.util.TimeZone;

public class TimeZoneCoercer extends AbstractCoercer {

	public TimeZone coerce(String tz, Class<?> toType) {
		if(tz.length() == 0) {
			return coerceNull(toType);
		}
		if(tz.charAt(0) == '-' || tz.charAt(0) == '+') {
			return TimeZone.getTimeZone("GMT" + tz);
		}
		return TimeZone.getTimeZone(tz);
	}

	@Override
	public TimeZone coerceNull(Class<?> toType) {
		return TimeZone.getDefault();
	}

	@Override
	public Class<?> getType() {
		return TimeZone.class;
	}
	
}
