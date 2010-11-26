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
package org.oobium.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ArrayUtils {

	public static Object first(Object[] array) {
		if(array != null && array.length > 0) {
			return array[0];
		}
		return null;
	}

	public static Object first(ArrayList<?> list) {
		if(list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public static Object first(LinkedList<?> list) {
		if(list != null && !list.isEmpty()) {
			return list.getFirst();
		}
		return null;
	}

	public static Object first(List<?> list) {
		if(list instanceof ArrayList) {
			return first((ArrayList<?>) list);
		}
		if(list instanceof LinkedList) {
			return first((LinkedList<?>) list);
		}
		return null;
	}

	public static Object last(Object[] array) {
		if(array != null && array.length > 0) {
			return array[array.length-1];
		}
		return null;
	}

	public static Object last(ArrayList<?> list) {
		if(list != null && !list.isEmpty()) {
			return list.get(list.size()-1);
		}
		return null;
	}

	public static Object last(LinkedList<?> list) {
		if(list != null && !list.isEmpty()) {
			return list.getLast();
		}
		return null;
	}

	public static Object last(List<?> list) {
		if(list instanceof ArrayList) {
			return last((ArrayList<?>) list);
		}
		if(list instanceof LinkedList) {
			return last((LinkedList<?>) list);
		}
		return null;
	}

	public static boolean isFirst(Object[] array, Object obj) {
		return obj == first(array);
	}
	
	public static boolean isFirst(List<?> list, Object obj) {
		return obj == first(list);
	}

	public static boolean isLast(Object[] array, Object obj) {
		return obj == last(array);
	}
	
	public static boolean isLast(List<?> list, Object obj) {
		return obj == last(list);
	}

}
