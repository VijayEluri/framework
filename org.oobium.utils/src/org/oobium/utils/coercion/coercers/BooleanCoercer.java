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


public class BooleanCoercer extends AbstractCoercer {

	public Boolean coerce(Integer i, Class<?> toType) {
		return i != null && i.intValue() != 0;
	}
	
	public Boolean coerce(Short s, Class<?> toType) {
		return s != null && s.shortValue() != 0;
	}
	
	public Boolean coerce(Byte b, Class<?> toType) {
		return b != null && b.byteValue() != 0;
	}
	
	public Boolean coerce(Long l, Class<?> toType) {
		return l != null && l.longValue() != 0;
	}
	
	public Boolean coerce(Double d, Class<?> toType) {
		return d != null && d.doubleValue() != 0;
	}
	
	public Boolean coerce(Float f, Class<?> toType) {
		return f != null && f.floatValue() != 0;
	}
	
	public Boolean coerce(String string, Class<?> toType) {
		return new Boolean(string);
	}
	
	public Boolean coerce(Boolean b, Class<?> toType) {
		return b;
	}
	
	@Override
	public Class<?> getType() {
		return Boolean.class;
	}

}
