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

import org.oobium.app.http.Action;
import org.oobium.app.http.MimeType;

public interface IHttp {

	public abstract Action getAction();
	public abstract String getActionName();
	public abstract String getControllerName();

	public abstract boolean isAction(Action action);
	public abstract boolean isPath(String path);
	public abstract boolean isXhr();
	
	public abstract String path();
	
	public abstract boolean accepts(MimeType type);

	public abstract MimeType wants();
	public abstract boolean wants(MimeType type);
	
	/**
	 * Compare the Accepted content types from the current request to the given options list of MimeTypes,
	 * and return the best match. The last MimeType in the given options list is considered the "default"
	 * and will be returned if, 1. there is no match, or 2. the best match is the wildcard: * / *.
	 * @param options an array of MimeTypes to compare against the current request's accepted content types.
	 * @return the best match, or the last item in the options if there is no good match.
	 */
	public abstract MimeType.Name wants(MimeType...options);
	
}
