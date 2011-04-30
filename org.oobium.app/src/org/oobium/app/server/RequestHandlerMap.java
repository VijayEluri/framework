package org.oobium.app.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RequestHandlerMap<T> {

	private Map<Integer, List<T>> handlers;
	
	public void add(T handler, int port) {
		if(handlers == null) {
			handlers = new HashMap<Integer, List<T>>();
		}
		List<T> portHandlers = handlers.get(port);
		if(portHandlers == null) {
			portHandlers = new ArrayList<T>();
			handlers.put(port, portHandlers);
		}
		portHandlers.add(handler);
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
			if(portHandlers != null) {
				boolean removed = portHandlers.remove(handler);
				if(portHandlers.isEmpty()) {
					handlers.remove(portHandlers);
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
			return (list != null) ? list.size() : 0;
		}
		return 0;
	}
	
}
