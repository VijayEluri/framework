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
package org.oobium.app.views;

import java.util.Map;

import org.oobium.app.controllers.HttpController;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.Router;

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
	
	public static Response render(Router router, DynamicAsset asset, Request request, Map<String, Object> params) throws Exception {
		HttpController controller = new HttpController();
		controller.initialize(router, request, params);
		controller.render(asset);
		return controller.getResponse();
	}

	
	protected ViewRenderer renderer;
	
	protected void doRender(StringBuilder sb) throws Exception {
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

	public void setRenderer(ViewRenderer renderer) {
		this.renderer = renderer;
	}
	
}
