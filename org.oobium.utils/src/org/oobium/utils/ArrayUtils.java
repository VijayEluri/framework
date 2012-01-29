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

	private static <T> void swap(T[] array, int i, int len) {
		T tmp = array[i];
		array[i] = array[len-i-1];
		array[len-i-1] = tmp;
	}
	
	private static <T> void swap(List<T> list, int i, int len) {
		T tmp = list.get(i);
		list.set(i, list.get(len-i-1));
		list.set(len-i-1, tmp);
	}
	
	public static <T> T[] reverse(T[] array) {
		if(array != null && array.length > 1) {
			for(int i = 0; i < array.length/2; i++) {
				swap(array, i, array.length);
			}
		}
		return array;
	}
	
	public static <T> List<T> reverse(List<T> list) {
		int len = list.size();
		if(list != null && len > 1) {
			for(int i = 0; i < len/2; i++) {
				swap(list, i, len);
			}
		}
		return list;
	}
	
}
