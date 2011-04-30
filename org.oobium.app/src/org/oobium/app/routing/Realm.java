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
package org.oobium.app.routing;

import java.util.HashMap;
import java.util.Map;

public class Realm {
	
	private final String name;
	private final Map<String, String> users;
	
	public Realm(String name) {
		this.name = name;
		this.users = new HashMap<String, String>();
	}
	
	public void authorize(String username, String password) {
		users.put(username, password);
	}
	
	public boolean isAuthorized(String username, String password) {
		String pass = users.get(username);
		return (pass != null && pass.equals(password));
	}
	
	public boolean isEmpty() {
		return users.isEmpty();
	}
	
	public String name() {
		return name;
	}
	
	public String remove(String username) {
		return users.remove(username);
	}
	
}
