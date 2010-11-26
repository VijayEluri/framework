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

import static org.oobium.utils.StringUtils.blank;

import java.util.Locale;
import java.util.Map;

public class LocaleCoercer extends AbstractCoercer {

	public Locale coerce(Object data, Class<?> toType) {
		if(data instanceof Map<?,?>) {
			Map<?,?> map = (Map<?,?>) data;
			String language = (String) map.get("language");
			String country = (String) map.get("country");
			String variant = (String) map.get("variant");
			if(!blank(language)) {
				if(!blank(country)) {
					if(!blank(variant)) {
						return new Locale(language, country, variant);
					}
					return new Locale(language, country);
				}
				return new Locale(language);
			}
		}
		if(data.getClass().isArray() && data.getClass().getComponentType() == String.class) {
			String[] array = (String[]) data;
			switch(array.length) {
			case 1: return new Locale(array[0]);
			case 2: return new Locale(array[0], array[1]);
			case 3: return new Locale(array[0], array[1], array[2]);
			}
		}
		
		throw new UnsupportedOperationException();
	}

	public Locale coerce(String string, Class<?> toType) {
		return new Locale(string);
	}
	
	@Override
	public Locale coerceNull() {
		return Locale.getDefault();
	}

	@Override
	public Class<?> getType() {
		return Locale.class;
	}
	
}
