package org.oobium.eclipse.designer.outline;

import java.util.Map.Entry;

public class Property implements Comparable<Property> {

	public enum Type { Field, Validation }
	
	public final Type type;
	public final String key;
	public final Object value;
	
	public Property(Type type, Entry<String, Object> entry) {
		this.type = type;
		this.key = entry.getKey();
		this.value = entry.getValue();
	}

	@Override
	public int compareTo(Property o) {
		return key.compareTo(o.key);
	}

}
