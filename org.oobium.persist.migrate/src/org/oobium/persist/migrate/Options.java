package org.oobium.persist.migrate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Options {

	private final Map<String, ? extends Object> options;
	
	public Options(Map<String, ? extends Object> options) {
		this.options = options;
	}
	
	public Object get(String key) {
		return (options != null) ? options.get(key) : null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, T defaultValue) {
		Object option = get(key);
		return (T) ((option != null) ? option : defaultValue);
	}
	
	public boolean has(String key) {
		return options != null && options.containsKey(key);
	}
	
	public boolean hasAny() {
		return options != null && !options.isEmpty();
	}

	public Set<String> getKeys() {
		return options.keySet();
	}
	
	public Map<String, ? extends Object> getMap() {
		if(options != null) {
			return new HashMap<String, Object>(options);
		}
		return new HashMap<String, Object>(0);
	}
	
	public int size() {
		return (options != null) ? options.size() : 0;
	}

	@Override
	public String toString() {
		return String.valueOf(options);
	}
	
}
