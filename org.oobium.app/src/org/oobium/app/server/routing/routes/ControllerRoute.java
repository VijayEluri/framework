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

import static org.oobium.utils.StringUtils.tableName;

import java.util.ArrayList;
import java.util.List;

import org.oobium.app.server.controller.Action;
import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.routing.Route;
import org.oobium.http.HttpRequest.Type;
import org.oobium.persist.Model;

public class ControllerRoute extends Route {

	public final Controller controller;
	public final Class<? extends Model> modelClass;
	public final Class<? extends Controller> controllerClass;
	public final Action action;
	public final String[][] params;

	
	public ControllerRoute(Type requestType, String rule, Controller controller, Action action) {
		this(requestType, rule, controller, null, controller.getClass(), action);
	}
	
	public ControllerRoute(Type requestType, String rule, Class<? extends Model> modelClass, Class<? extends Controller> controllerClass, Action action) {
		this(requestType, rule, null, modelClass, controllerClass, action);
	}
	
	private ControllerRoute(Type requestType, String rule, Controller controller, Class<? extends Model> modelClass, Class<? extends Controller> controllerClass, Action action) {
		super(Route.CONTROLLER, requestType, rule);

		this.controller = controller;
		this.modelClass = modelClass;
		this.controllerClass = controllerClass;

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
	
	public void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(requestType).append(']').append(' ');
		sb.append(isFixed() ? path : pattern.pattern()).append(' ').append('-').append('>').append(' ');
		if(controller == null) {
			sb.append(controllerClass.getSimpleName());
		} else {
			sb.append(controllerClass.getSimpleName()).append('-').append(controller.hashCode());
		}
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
