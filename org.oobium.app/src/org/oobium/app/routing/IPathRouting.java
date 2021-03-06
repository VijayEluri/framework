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
package org.oobium.app.routing;

import org.oobium.app.http.Action;
import org.oobium.persist.Model;

public interface IPathRouting {

	/**
	 * Get the path to the show all method for the given model class.
	 * A convenience method that is the same as calling
	 * {@link #pathTo(Class, showAll)}.
	 * @param modelClass
	 * @return
	 */
	public abstract Path pathTo(Class<? extends Model> modelClass);

	/**
	 * @param modelClass the class of model to get the path to
	 * @param action the action for which to get the path to; valid: {@link Action#create}, {@link Action#showAll}, {@link Action#showNew}
	 * @return
	 * @see Action
	 */
	public abstract Path pathTo(Class<? extends Model> modelClass, Action action);

	/**
	 * Get the path to the show method for the given model.
	 * A convenience method that is the same as calling
	 * {@link #pathTo(Model, show)}.
	 * @param model
	 * @return
	 */
	public abstract Path pathTo(Model model);

	/**
	 * @param
	 * @param action the action for which to get the path to; valid: all actions
	 * @return
	 * @see Action
	 */
	public abstract Path pathTo(Model model, Action action);

	/**
	 * A convenience method that is the same as calling
	 * {@link #getPathTo(Model, String, showAll)} if the given field is a
	 * hasMany relation, otherwise it is the same as
	 * {@link #getPathTo(Model, String, show)} .
	 * @param parent
	 * @return
	 */
	public abstract Path pathTo(Model parent, String field);

	/**
	 * @param
	 * @param action the action for which to get the path to; valid: {@link Action#create}, {@link Action#showAll}, {@link Action#showNew}
	 * @return
	 * @see Action
	 */
	public abstract Path pathTo(Model parent, String field, Action action);

	/**
	 * Get the path to a named route.
	 * @param routeName the name of the named route, as given with {@link Router#add(String)}
	 * @return
	 */
	public abstract Path pathTo(String routeName);

	/**
	 * Get the path to a named route, using the given model to resolve any variables in the route.
	 * @param routeName the name of the named route, as given with {@link Router#add(String)}
	 * @return
	 */
	public abstract Path pathTo(String routeName, Model model);
	
	/**
	 * Get the path to a named route.
	 * @param routeName the name of the named route, as given with {@link Router#add(String)}
	 * @param params
	 * @return
	 */
	public abstract Path pathTo(String routeName, Object...params);
	
}
