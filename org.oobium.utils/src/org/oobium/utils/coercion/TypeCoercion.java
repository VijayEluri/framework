package org.oobium.utils.coercion;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TypeCoercion {

	private final Object object;
	
	TypeCoercion(Object object) {
		this.object = object;
	}
	
	/**
	 * Creates a new object that represents the given object once it
	 * has been coerced into the given type.
	 * @param object the object to be coerced
	 * @param type the type to coerce the object into (also the return type)
	 * @return the new coerced object
	 * @throws IllegalArgumentException if the type is null or there is an error coercing the given object into the given type
	 * @throws UnsupportedOperationException if the given object cannot be coerced into the given type
	 */
	public <T> T to(Class<T> type) {
		return coerce(object, type);
	}
	
	/**
	 * If the given object is not null, then creates a new object that represents the given object once it has been coerced 
	 * into the type of the given default value; otherwise, simply returns the default value.
	 * @param object the object to be coerced
	 * @param defaultValue the default to use if the given object is null; also the type to coerce the object into if it is not null.
	 * <b>This value cannot be null</b>.
	 * @return the new coerced object, or the default value if the given object is null
	 * @throws IllegalArgumentException if the defaultValue is null or there is an error coercing the given object into the given type
	 * @throws UnsupportedOperationException if the given object cannot be coerced into the given type
	 */
	public <T> T from(T defaultValue) {
		if(defaultValue != null) {
			Object value = to(defaultValue.getClass());
			if(value != null && defaultValue.getClass().isAssignableFrom(value.getClass())) {
				@SuppressWarnings("unchecked")
				T coercedValue = (T) value;
				return coercedValue;
			} else {
				return defaultValue;
			}
		}
		throw new IllegalArgumentException("defaultValue cannot be null");
	}

	public List<Object> toList() {
		@SuppressWarnings("unchecked")
		List<Object> list = coerce(object, List.class);
		return list;
	}
	
	public <T> List<T> toList(Class<T> elementType) {
		@SuppressWarnings("unchecked")
		List<T> list = coerce(object, List.class);
		if(elementType == Object.class) {
			return list;
		} else {
			List<T> coercedList = new ArrayList<T>();
			for(Object e : list) {
				coercedList.add(coerce(e, elementType));
			}
			return coercedList;
		}
	}
	
	public Map<Object, Object> toMap() {
		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>) coerce(object, Map.class);
		return map;
	}
	
	public <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType) {
		@SuppressWarnings("unchecked")
		Map<Object, Object> map = coerce(object, Map.class);
		if(keyType != String.class || valueType != Object.class) {
			for(Entry<Object, Object> e : map.entrySet()) {
				map.put(coerce(e.getKey(), keyType), coerce(e.getValue(), valueType));
			}
		}
		@SuppressWarnings("unchecked")
		Map<K, V> coercedMap = (Map<K, V>) map;
		return coercedMap;
	}
	
}
