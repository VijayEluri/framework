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
package org.oobium.http;

public interface HttpRequest404Handler {

	/**
	 * The system unique name for this request handler.
	 * Typically, this is the application's Bundle-SymbolicName.
	 * @return the name of this request handler
	 */
	public abstract String getName();

	/**
	 * handle the "<code>404 - Not Found</code>" error condition.
	 * @param request
	 * @return
	 */
	public abstract HttpResponse handle404(HttpRequest request);

}
