package org.oobium.framework.tests.dyn;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class DynClass {

	private final DynClasses dynClasses;
	private final String fullName;

	private String source;
	private boolean compiled;

	private Object instance;
	
	DynClass(DynClasses models, String fullName, String source) {
		this.dynClasses = models;
		this.fullName = fullName;
		this.source = source;
	}

	protected void checkState() {
		if(compiled) {
			throw new IllegalStateException(getClass().getSimpleName() + " has already been compiled");
		}
	}

	public String getFullName() {
		return fullName;
	}
	
	public Class<?> getDynamicClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		compiled = true;
		return dynClasses.newModelClass(fullName);
	}
	
	public boolean hasField(String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class<?> clazz = getDynamicClass();
		try {
			return clazz.getDeclaredField(name) != null;
		} catch(SecurityException e) {
		} catch(NoSuchFieldException e) {
		}
		return false;
	}

	public Object get(String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchFieldException {
		Class<?> clazz = instance.getClass();
		Field field = clazz.getDeclaredField(name);
		return field.get(name);
	}

	public void set(String name, Object value) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchFieldException {
		Class<?> clazz = instance.getClass();
		Field field = clazz.getDeclaredField(name);
		field.set(name, value);
	}

	public boolean hasMethod(String name, Class<?>...parameterTypes) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class<?> clazz = getDynamicClass();
		try {
			return clazz.getDeclaredMethod(name, parameterTypes) != null;
		} catch(SecurityException e) {
		} catch(NoSuchMethodException e) {
		}
		return false;
	}
	
	public Object invoke(String name, Object...values) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Class<?> clazz = instance.getClass();
		Class<?>[] parameterTypes = new Class[values.length];
		for(int i = 0; i < values.length; i++) {
			if(values[i] == null) {
				throw new IllegalArgumentException("null value not permitted, use #invoke(String, Class<?>[], Object[]) instead");
			}
			parameterTypes[i] = values[i].getClass();
		}
		Method method = clazz.getDeclaredMethod(name, parameterTypes);
		return method.invoke(instance, values);
	}
	
	public Object invoke(String name, Class<?>[] parameterTypes, Object[] values) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Class<?> clazz = instance.getClass();
		Method method = clazz.getDeclaredMethod(name, parameterTypes);
		return method.invoke(instance, values);
	}
	
	public Object newInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		compiled = true;
		instance = dynClasses.newInstance(fullName);
		return instance;
	}
	
	public String getSource() {
		return source;
	}
	
	@Override
	public String toString() {
		return getSource();
	}
	
}
