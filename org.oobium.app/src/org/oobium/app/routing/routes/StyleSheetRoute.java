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

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.routing.Route;
import org.oobium.app.views.StyleSheet;

public class StyleSheetRoute extends Route {

	public final Class<? extends StyleSheet> assetClass;
	public final String[][] params;
	
	public StyleSheetRoute(HttpMethod method, String rule, Class<? extends StyleSheet> assetClass) {
		super(Route.DYNAMIC_ASSET, method, rule);
		this.assetClass = assetClass;

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
	
	@Override
	protected String[][] params() {
		return params;
	}
	
	private void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(httpMethod).append(']').append(' ');
		sb.append(isFixed() ? path : pattern.pattern()).append(' ').append('-').append('>').append(' ');
		sb.append(assetClass.getSimpleName());
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
