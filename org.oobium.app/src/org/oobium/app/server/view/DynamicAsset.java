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
package org.oobium.app.server.view;

import java.util.Map;

import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.response.Response;
import org.oobium.app.server.routing.Router;
import org.oobium.http.HttpRequest;

public class DynamicAsset {

	public static String getFileExtension(Class<? extends DynamicAsset> clazz) {
		if(StyleSheet.class.isAssignableFrom(clazz)) {
			return "css";
		}
		if(ScriptFile.class.isAssignableFrom(clazz)) {
			return "js";
		}
		throw new IllegalArgumentException("don't know the file extension for " + clazz);
	}
	
	public static Response render(Router router, DynamicAsset asset, HttpRequest request, Map<String, Object> params) throws Exception {
		Controller controller = new Controller();
		controller.initialize(router, request, params);
		controller.render(asset);
		return controller.getResponse();
	}

	
	public void doRender(StringBuilder sb) throws Exception {
		// subclasses to override if necessary
	}
	
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		render(sb);
		return sb.toString();
	}

	public void render(StringBuilder sb) {
		try {
			doRender(sb);
		} catch(Exception e) {
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException("Exception thrown during render", e);
			}
		}
	}

}
