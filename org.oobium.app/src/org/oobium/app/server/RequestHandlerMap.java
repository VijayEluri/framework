package org.oobium.app.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RequestHandlerMap<T> {

	private Map<Integer, List<T>> handlers;
	
	public void add(T handler, int port) {
		if(handlers == null) {
			handlers = new HashMap<Integer, List<T>>();
			List<T> portHandlers = new ArrayList<T>();
			portHandlers.add(handler);
			handlers.put(port, portHandlers);
		} else {
			List<T> portHandlers = handlers.get(port);
			if(portHandlers == null) {
				portHandlers = new ArrayList<T>();
				portHandlers.add(handler);
				handlers.put(port, portHandlers);
			} else {
				if(!portHandlers.contains(handler)) { // don't add duplicates
					portHandlers.add(handler);
				}
			}
		}
	}

	public void clear() {
		if(handlers != null) {
			handlers.clear();
		}
	}
	
	public List<T> get(int port) {
		if(handlers != null) {
			return handlers.get(port);
		}
		return new ArrayList<T>(0);
	}
	
	public boolean hasPorts() {
		return handlers != null;
	}
	
	public int[] getPorts() {
		return (handlers != null)  ? coerce(handlers.keySet(), int[].class) : new int[0];
	}

	public boolean isEmpty() {
		return handlers == null;
	}
	
	public boolean remove(Object handler, int port) {
		if(handlers != null) {
			List<?> portHandlers = handlers.get(port);
			if(portHandlers == null) {
			}
			if(portHandlers != null) {
				boolean removed = portHandlers.remove(handler);
				if(portHandlers.isEmpty()) {
					handlers.remove(port);
					if(handlers.isEmpty()) {
						handlers = null;
					}
				}
				return removed;
			}
		}
		return false;
	}
	
	public int size(int port) {
		if(handlers != null) {
			List<?> list = handlers.get(port);
			if(list != null) {
				return list.size();
			}
		}
		return 0;
	}
	
	@Override
	public String toString() {
		Type type = getClass().getGenericSuperclass();
		if(type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			type = pt.getActualTypeArguments()[0];
			return type + " " + handlers;
		} else {
			return "raw " + handlers;
		}
	}
	
}
