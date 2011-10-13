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

import static org.oobium.utils.StringUtils.tableName;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.controllers.HttpController;
import org.oobium.app.routing.Route;
import org.oobium.app.http.Action;
import org.oobium.persist.Model;

public class HttpRoute extends Route {

	public final Class<? extends Model> modelClass;
	public final Class<? extends HttpController> controllerClass;
	public final Action action;
	public final String[][] params;
	
	public String realm;

	// for a hasMany route
	public final Class<? extends Model> parentClass;
	public final String hasManyField;

	
	public HttpRoute(HttpMethod method, String rule, Class<? extends Model> modelClass, Class<? extends HttpController> controllerClass, Action action) {
		this(method, rule, modelClass, controllerClass, action, null, null);
	}

	public HttpRoute(HttpMethod method, String rule, Class<? extends Model> modelClass, Class<? extends HttpController> controllerClass, Action action, Class<? extends Model> parentClass, String hasManyField) {
		super(Route.HTTP_CONTROLLER, method, rule);

		this.modelClass = modelClass;
		this.controllerClass = controllerClass;
		this.parentClass = parentClass;
		this.hasManyField = hasManyField;

		Class<?> clazz = (modelClass != null) ? modelClass : controllerClass;
		
		List<String[]> params = new ArrayList<String[]>();
		parseRules(rule, clazz, params);
		
		this.action = action;
		
		if(params.isEmpty()) {
			this.params = null;
		} else {
			this.params = params.toArray(new String[params.size()][]);
		}
		
		matchOnFullPath = (pattern != null) ? (pattern.pattern().indexOf('?') != -1) : false;
		
		this.rule = rule.replaceAll("\\{\\s*models\\s*\\}", tableName(clazz));
		
		setString();
	}
	
	@Override
	protected String[][] params() {
		return params;
	}
	
	public void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(httpMethod).append(']').append(' ');
		sb.append(isFixed() ? path : pattern.pattern()).append(' ').append('-').append('>').append(' ');
		sb.append(controllerClass.getSimpleName());
		sb.append('#');
		if(action != null) {
			sb.append(action);
		} else {
			sb.append("handleRequest");
		}
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
