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
package org.oobium.app.server.controller;

import org.oobium.http.constants.Action;
import org.oobium.http.constants.ContentType;

public interface IHelpers {

	public abstract Action getAction();
	public abstract String getActionName();
	public abstract String getControllerName();

	public abstract boolean isAction(Action action);
	public abstract boolean isPath(String path);
	public abstract boolean isXhr();
	
	public abstract boolean accepts(ContentType type);
	public abstract boolean acceptsHtml();
	public abstract boolean acceptsImage();
	public abstract boolean acceptsJS();
	public abstract boolean acceptsJSON();

	public abstract ContentType wants();
	public abstract boolean wants(ContentType type);
	public abstract ContentType wants(ContentType...options);
	public abstract boolean wantsHtml();
	public abstract boolean wantsImage();
	public abstract boolean wantsJS();
	public abstract boolean wantsJSON();
	
}
