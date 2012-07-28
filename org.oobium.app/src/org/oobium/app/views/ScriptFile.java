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

import org.oobium.persist.Model;


public abstract class ScriptFile {

	private ViewRenderer renderer;
	
	public void addExternalScript(ScriptFile asset) {
		renderer.addExternalScript(asset);
	}
	
	public void addExternalScript(String src) {
		renderer.addExternalScript(src);
	}
	
	protected abstract void doRender(StringBuilder sb) throws Exception;
	
	public abstract boolean hasInitializer();
	
	protected void includeScriptEnvironment() {
		renderer.includeScriptEnvironment = true;
	}
	
	protected String includeScriptModel(Model model, int position) {
		return includeScriptModel(model, position, false);
	}
	
	protected String includeScriptModel(Model model, int position, boolean includeHasMany) {
		return renderer.includeScriptModel(model, position, includeHasMany);
	}
	
	protected void includeScriptModels() {
		includeScriptModels(false);
	}
	
	protected void includeScriptModels(boolean includeHasMany) {
		renderer.includeScriptModels(Model.class, includeHasMany);
	}

	public void render(ViewRenderer renderer, StringBuilder sb) {
		this.renderer = renderer;
		try {
			doRender(sb);
		} catch(Exception e) {
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException("Exception thrown during render", e);
			}
		} finally {
			this.renderer = null;
		}
	}
	
}
