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

import java.util.HashMap;
import java.util.Map;

import org.oobium.app.server.controller.Controller;
import org.oobium.app.server.response.Response;
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
	
	public static Response render(Class<? extends DynamicAsset> ssClass, HttpRequest request, Map<String, Object> params) throws Exception {
		DynamicAsset ss = ssClass.newInstance();
		return render(ss, request, params);
	}
	
	public static Response render(DynamicAsset ss, HttpRequest request) throws Exception {
		return render(ss, request, new HashMap<String, Object>(0));
	}

	public static Response render(DynamicAsset ss, HttpRequest request, Map<String, Object> params) throws Exception {
		Controller controller = new Controller(request, params);
		controller.render(ss);
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
