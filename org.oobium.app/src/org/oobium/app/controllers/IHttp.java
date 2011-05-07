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
	
	public abstract boolean accepts(MimeType type);

	public abstract MimeType wants();
	public abstract boolean wants(MimeType type);
	public abstract MimeType.Name wants(MimeType...options);
	
}
