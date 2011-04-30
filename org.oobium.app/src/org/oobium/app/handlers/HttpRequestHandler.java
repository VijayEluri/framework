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
package org.oobium.app.handlers;

import org.oobium.app.request.Request;

public interface HttpRequestHandler {

	/**
	 * The system unique name for this request handler.
	 * Typically, this is the application's Bundle-SymbolicName.
	 * @return the name of this request handler
	 */
	public abstract String getName();

	public abstract int getPort();
	
	/**
	 * Handle the HTTP request.
	 * This method MUST be able to handle being called from multiple threads.
	 * @param request
	 * @return
	 */
	public abstract Object handleRequest(Request request) throws Exception;
	
}
