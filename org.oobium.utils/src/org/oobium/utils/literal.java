package org.oobium.utils;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class literal {

	public static class Entry<K, V> {
		public final K key;
		public final V value;
		private Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
	
	public static <E> E[] Array(E...elements) {
		return elements;
	}
	
	public static <K, V> Entry<K, V> e(K key, V value) {
		return new Entry<K, V>(key , value);
	}
	
	public static <K, V> Entry<K, V> entry(K key, V value) {
		return e(key , value);
	}
	
	public static <K, V> Map<K, V> LinkedMap() {
		return new LinkedHashMap<K, V>(0);
	}

	public static <K, V> Map<K, V> LinkedMap(Entry<K, V>...entries) {
		Map<K, V> map = new LinkedHashMap<K, V>();
		for(Entry<K, V> entry : entries) {
			map.put(entry.key, entry.value);
		}
		return map;
	}
	
	public static <E> Set<E> LinkedSet(E...elements) {
		return new LinkedHashSet<E>(asList(elements));
	}
	
	public static <E> List<E> List(E...elements) {
		return new ArrayList<E>(asList(elements));
	}
	
	public static <K, V> Map<K, V> Map() {
		return new HashMap<K, V>(0);
	}
	
	public static <K, V> Map<K, V> Map(Entry<? extends K, ? extends V> entry1, Entry<? extends K, ? extends V> entry2) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(entry1.key, entry1.value);
		map.put(entry2.key, entry2.value);
		return map;
	}

	public static <K, V> Map<K, V> Map(Entry<? extends K, ? extends V> entry1, Entry<? extends K, ? extends V> entry2, Entry<? extends K, ? extends V> entry3) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(entry1.key, entry1.value);
		map.put(entry2.key, entry2.value);
		map.put(entry3.key, entry3.value);
		return map;
	}

	public static <K, V> Map<K, V> Map(Entry<? extends K, ? extends V> entry1, Entry<? extends K, ? extends V> entry2, Entry<? extends K, ? extends V> entry3, Entry<? extends K, ? extends V> entry4) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(entry1.key, entry1.value);
		map.put(entry2.key, entry2.value);
		map.put(entry3.key, entry3.value);
		map.put(entry4.key, entry4.value);
		return map;
	}

	public static <K, V> Map<K, V> Map(Entry<? extends K, ? extends V> entry1, Entry<? extends K, ? extends V> entry2, Entry<? extends K, ? extends V> entry3, Entry<? extends K, ? extends V> entry4, Entry<? extends K, ? extends V> entry5) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(entry1.key, entry1.value);
		map.put(entry2.key, entry2.value);
		map.put(entry3.key, entry3.value);
		map.put(entry4.key, entry4.value);
		map.put(entry5.key, entry5.value);
		return map;
	}
	
	public static <K, V> Map<K, V> Map(K key, V value) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key, value);
		return map;
	}

	public static <K, V> Dictionary<K, V> Dictionary(K key, V value) {
		Hashtable<K, V> table = new Hashtable<K, V>();
		table.put(key, value);
		return table;
	}
	
	public static Properties Properties(Entry<?,?>...entries) {
		Properties props = new Properties();
		for(Entry<?, ?> entry : entries) {
			props.put(entry.key, entry.value);
		}
		return props;
	}

	public static Properties Properties(Object key, Object value) {
		Properties props = new Properties();
		props.put(key, value);
		return props;
	}

	public static Properties Properties(Entry<?, ?> entry1, Entry<?, ?> entry2) {
		Properties props = new Properties();
		props.put(entry1.key, entry1.value);
		props.put(entry2.key, entry2.value);
		return props;
	}

	public static <E> Set<E> Set(E...elements) {
		return new HashSet<E>(asList(elements));
	}
	
}
