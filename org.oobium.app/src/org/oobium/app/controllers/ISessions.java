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

import org.oobium.app.sessions.Session;
import org.oobium.persist.Model;

public interface ISessions {

	/**
	 * Get the session object related to the current request.
	 * If one does not exist, then a new session object will be created
	 * and associated with the current request.
	 * @see #getSession(boolean)
	 * @return the session object related to the current request (may be a new session); never null
	 */
	public abstract Session getSession();

	/**
	 * Get the session object related to the current request.
	 * If one does not exist and create is true, then a new session
	 * object will be created and associated with the current request.
	 * @param create true to create a new session if one does not already exist
	 * @see #getSession()
	 * @return the session object related to the current request; null if one
	 * does not exist and create is false.
	 */
	public abstract Session getSession(boolean create);
	
	/**
	 * Find out if a session object is already associated with the current request.<br>
	 * This method should return the same value as getSession(false) != null.
	 * @return true if a session is associated with the current request; false otherwise.
	 * @see #getSession(boolean)
	 */
	public abstract boolean hasSession();
	
	
	public abstract Model getAuthenticated();
	
	public abstract <T extends Model> T getAuthenticated(Class<T> clazz);
	
	public abstract boolean isAuthenticated();
	
	public abstract boolean isAuthenticated(Model model);
	
}
