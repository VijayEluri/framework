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
package org.oobium.app.server.routing.routes;

import static org.oobium.http.constants.RequestType.GET;

import org.oobium.app.server.routing.Route;

public class RedirectRoute extends Route {

	public final String to;
	
	public RedirectRoute(String from, String to) {
		super(Route.REDIRECT, GET, null);
		this.path = from;
		this.to = to;
		
		setString();
	}
	
	@Override
	protected String[][] params() {
		return null;
	}
	
	private void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(requestType).append(']').append(' ');
		sb.append(isFixed() ? path : pattern.pattern()).append(' ').append('-').append('>').append(' ');
		sb.append(to);
		string = sb.toString();
	}
	
}
