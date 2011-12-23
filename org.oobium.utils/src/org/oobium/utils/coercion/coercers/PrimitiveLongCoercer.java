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


public class PrimitiveLongCoercer extends LongCoercer {

	@Override
	public Long coerceNull(Class<?> toType) {
		return new Long(0);
	}

	@Override
	public Class<?> getType() {
		return long.class;
	}
	
}
