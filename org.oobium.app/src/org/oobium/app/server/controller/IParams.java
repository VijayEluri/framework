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

import java.util.Map;

public interface IParams {

	public abstract Object getParam(String name);
	public abstract <T> T getParam(String name, Class<T> clazz);
	public abstract <T> T getParam(String name, T defaultValue);
	public abstract Map<String, Object> getParams();
	public abstract boolean hasParam(String name);
	public abstract boolean hasParams();
	
	public abstract String param(String name);
	public abstract <T> T param(String name, Class<T> clazz);
	public abstract <T> T param(String name, T defaultValue);
	public abstract Map<String, Object> params();
	
}
