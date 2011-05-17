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
package org.oobium.app.controllers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Filter {

	private final Map<?, ?> map;
	
	Filter(Object object) {
		if(object instanceof Map<?,?>) {
			this.map = (Map<?,?>) object;
		} else {
			this.map = null;
		}
	}

	/**
	 * Clear the underlying map, removing all fields.
	 * @return this {@link Filter} object
	 */
	public Filter clear() {
		if(map != null) {
			map.clear();
		}
		return this;
	}

	/**
	 * Remove the given fields from the map.
	 * @param fields the fields to remove from the map
	 * @return this {@link Filter} object
	 */
	public Filter remove(String...fields) {
		if(map != null) {
			for(String field : fields) {
				map.remove(field);
			}
		}
		return this;
	}

	/**
	 * Select the given fields to pass through the filter and, therefore
	 * remain in the map.
	 * @param fields the fields to keep in the map
	 * @return this {@link Filter} object
	 */
	public Filter select(String...fields) {
		if(map != null) {
			List<String> list = Arrays.asList(fields);
			for(Iterator<?> iter = map.keySet().iterator(); iter.hasNext(); ) {
				Object field = iter.next();
				if(!list.contains(field)) {
					iter.remove();
				}
			}
		}
		return this;
	}
	
}
