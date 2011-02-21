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
package org.oobium.app.server.routing;

import org.oobium.http.constants.Action;
import org.oobium.persist.Model;

public interface IUrlRouting {

	/**
	 * Get the url to the show all method for the given model class.
	 * A convenience method that is the same as calling
	 * {@link #urlTo(Class, showAll)}.
	 * @param modelClass
	 * @return
	 */
	public abstract String urlTo(Class<? extends Model> modelClass);

	/**
	 * @param modelClass the class of model to get the url to
	 * @param action the action for which to get the url to; valid: {@link Action#create}, {@link Action#showAll}, {@link Action#showNew}
	 * @return
	 * @see Action
	 */
	public abstract String urlTo(Class<? extends Model> modelClass, Action action);
	
	/**
	 * Get the url to the show method for the given model.
	 * A convenience method that is the same as calling
	 * {@link #urlTo(Model, show)}.
	 * @param modelClass
	 * @return
	 */
	public abstract String urlTo(Model model);
	
	/**
	 * @param
	 * @param action the action for which to get the url to; valid: all actions
	 * @return
	 * @see Action
	 */
	public abstract String urlTo(Model model, Action action);

	/**
	 * A convenience method that is the same as calling
	 * {@link #urlTo(Model, String, showAll)} if the given field is a
	 * hasMany relation, otherwise it is the same as
	 * {@link #urlTo(Model, String, show)} .
	 * @param parent
	 * @return
	 */
	public abstract String urlTo(Model parent, String field);
	
	/**
	 * @param
	 * @param
	 * @param action the action for which to get the url to; valid: {@link Action#create}, {@link Action#showAll}, {@link Action#showNew}
	 * @return
	 * @see Action
	 */
	public abstract String urlTo(Model parent, String field, Action action);

	/**
	 * Get the url to a named route.
	 * @param routeName the name of the named route, as given with {@link Router#add(String)}
	 * @return
	 */
	public abstract String urlTo(String routeName);
	
	/**
	 * Get the url to a named route, using the given model to resolve any variables in the route.
	 * @param routeName the name of the named route, as given with {@link Router#add(String)}
	 * @param model the model object
	 * @return
	 */
	public abstract String urlTo(String routeName, Model model);

	/**
	 * Get the url to a named route, using the given params to resolve any variables in the route.
	 * @param routeName the name of the named route, as given with {@link Router#add(String)}
	 * @param params a varargs array of params
	 * @return
	 */
	public abstract String urlTo(String routeName, Object...params);
	
}
