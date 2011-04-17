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

import java.util.Date;

public interface HttpSession {

	public static final String SESSION_ID_KEY = "oobium_session_id";
	public static final String SESSION_UUID_KEY = "oobium_session_uuid";

	public abstract void clearData();
	public abstract boolean destroy();
	public abstract String getData(String key);
	public abstract Date getExpiration();
	public abstract String getUuid();
	public abstract int getId();
	public abstract boolean isDestroyed();
	public abstract void putData(String key, String value);
	public abstract void putData(String key, long value);
	public abstract void putData(String key, double value);
	public abstract void putData(String key, boolean value);
	public abstract String removeData(String key);
	public abstract boolean save();
	public abstract void setExpiration(Date expiration);
	
}
