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

	public static <T> T first(T[] array) {
		if(array != null && array.length > 0) {
			return array[0];
		}
		return null;
	}

	public static <T> T first(ArrayList<T> list) {
		if(list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public static <T> T first(LinkedList<T> list) {
		if(list != null && !list.isEmpty()) {
			return list.getFirst();
		}
		return null;
	}

	public static <T> T first(List<T> list) {
		if(list instanceof ArrayList) {
			return first((ArrayList<T>) list);
		}
		if(list instanceof LinkedList) {
			return first((LinkedList<T>) list);
		}
		return null;
	}

	public static <T> T last(T[] array) {
		if(array != null && array.length > 0) {
			return array[array.length-1];
		}
		return null;
	}

	public static <T> T last(ArrayList<T> list) {
		if(list != null && !list.isEmpty()) {
			return list.get(list.size()-1);
		}
		return null;
	}

	public static <T> T last(LinkedList<T> list) {
		if(list != null && !list.isEmpty()) {
			return list.getLast();
		}
		return null;
	}

	public static <T> T last(List<T> list) {
		if(list instanceof ArrayList) {
			return last((ArrayList<T>) list);
		}
		if(list instanceof LinkedList) {
			return last((LinkedList<T>) list);
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
