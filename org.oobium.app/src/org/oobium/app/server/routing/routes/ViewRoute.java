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

import java.util.ArrayList;
import java.util.List;

import org.oobium.app.server.routing.Route;
import org.oobium.app.server.view.View;
import org.oobium.http.HttpRequest.Type;

public class ViewRoute extends Route {

	public final Class<? extends View> viewClass;
	public final String[][] params;
	
	public ViewRoute(Type requestType, String rule, Class<? extends View> viewClass) {
		super(Route.VIEW, requestType, rule);
		this.viewClass = viewClass;

		List<String[]> params = new ArrayList<String[]>();
		parseRules(rule, null, params);
		
		if(params.isEmpty()) {
			this.params = null;
		} else {
			this.params = params.toArray(new String[params.size()][]);
		}
		matchOnFullPath = (pattern != null) ? (pattern.pattern().indexOf('?') != -1) : false;
		
		setString();
	}
	
	private void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(requestType).append(']').append(' ');
		sb.append(isFixed() ? path : pattern.pattern()).append(' ').append('-').append('>').append(' ');
		sb.append(viewClass.getSimpleName());
		if(params != null) {
			sb.append('(');
			for(int i = 0; i < params.length; i++) {
				if(i != 0) sb.append(',');
				sb.append(params[i][0]);
				if(params[i][1] != null) {
					sb.append('=').append(params[i][1]);
				}
			}
			sb.append(')');
		}
		string = sb.toString();
	}
	
}
