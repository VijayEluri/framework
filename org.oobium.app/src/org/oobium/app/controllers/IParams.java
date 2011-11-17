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
package org.oobium.app.controllers;

import java.util.Set;

public interface IParams {

	public abstract Object getParam(String name);
	public abstract <T> T getParam(String name, Class<T> clazz);
	public abstract <T> T getParam(String name, T defaultValue);
	
	/**
	 * A set of all the parameter names.
	 * @return a Set<String> of parameter names, and empty Set if there are none; never null.
	 * @see #params()
	 */
	public abstract Set<String> getParams();
	public abstract boolean hasParam(String name);
	public abstract boolean hasParams();
	
	/**
	 * Get the parameter with the given name and return it as a String.
	 * @param name the name of the parameter
	 * @return a String representation of the parameter, or null if no parameter exists with the given name
	 */
	public abstract String param(String name);
	
	/**
	 * Get the parameter with the given name and return it as an instance of the given type.
	 * @param name the name of the parameter
	 * @param type the type to which the parameter will be coerced
	 * @return the parameter coerced into the given type, or null if no parameter exists with the given name
	 */
	public abstract <T> T param(String name, Class<T> type);
	
	/**
	 * Get the parameter with the given name and return it as an instance of the same type as the given defaultValue.
	 * @param name the name of the parameter
	 * @param defaultValue the value to return if no parameter exists with the given name; must not be null
	 * @return the parameter coerced into the type of the given defaultValue, or the defaultValue if no parameter exists with the given name; never null
	 */
	public abstract <T> T param(String name, T defaultValue);

	/**
	 * Get the parameter with the name matching the variable name of the given type, and return it as an instance of the given type.
	 * <p>For example, <code>param(MyModel.class)</code> should be equivalent to <code>param("myModel", MyModel.class)</code></p>
	 * @param type the type to which the parameter will be coerced
	 * @return the parameter coerced into the given type, or null if no parameter exists with the given name
	 * @see #param(String, Class)
	 */
	public abstract <T> T param(Class<T> type);
	
	/**
	 * Get the parameter with the name matching the variable name of the given defaultValue's type, and return it as an instance of the same type as the given defaultValue.
	 * <p>For example, <code>param(new MyModel())</code> should be equivalent to <code>param("myModel", new MyModel())</code></p>
	 * @param defaultValue the value to return if no parameter exists with the given name; must not be null
	 * @return the parameter coerced into the type of the given defaultValue, or the defaultValue if no parameter exists with the given name; never null
	 * @see #param(String, Object)
	 */
	public abstract <T> T param(T defaultValue);

	/**
	 * A set of all the parameter names.
	 * @return a Set<String> of parameter names, and empty Set if there are none; never null.
	 * @see #getParams()
	 */
	public abstract Set<String> params();
	
}
