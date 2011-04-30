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
package org.oobium.app.routing.routes;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.routing.Route;

public class RedirectRoute extends Route {

	public final String to;
	
	public RedirectRoute(String from, String to) {
		super(Route.REDIRECT, HttpMethod.GET, null);
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
		sb.append('[').append(httpMethod).append(']').append(' ');
		sb.append(isFixed() ? path : pattern.pattern()).append(' ').append('-').append('>').append(' ');
		sb.append(to);
		string = sb.toString();
	}
	
}
