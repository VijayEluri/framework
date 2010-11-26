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

import org.oobium.persist.Model;

public interface IFlash {

	public abstract String flash(String name);
	public abstract <T> T flash(String name, Class<T> type);
	public <T> T flash(String name, T defaultValue);

	public abstract String getFlash(String name);
	public abstract <T> T getFlash(String name, Class<T> type);
	public <T> T getFlash(String name, T defaultValue);
	public abstract boolean hasFlash(String name);
	public abstract void setFlash(String name, Object value);
	
	public abstract String getFlashError();
	public abstract boolean hasFlashError();
	public abstract void setFlashError(Model model);
	public abstract void setFlashError(Model...models);
	public abstract void setFlashError(Object value);
	
	public abstract String getFlashNotice();
	public abstract boolean hasFlashNotice();
	public abstract void setFlashNotice(Object value);
	
	public abstract String getFlashWarning();
	public abstract boolean hasFlashWarning();
	public abstract void setFlashWarning(Object value);
	
}
