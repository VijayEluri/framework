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


public abstract class ScriptFile extends DynamicAsset {

	public abstract boolean hasInitializer();
	
	public void addExternalScript(String src) {
		renderer.addExternalScript(src);
	}
	
	public void addExternalScript(ScriptFile asset) {
		renderer.addExternalScript(asset);
	}
	
	protected void includeScriptModel(Class<? extends Model> modelClass) {
		includeScriptModel(modelClass, false);
	}
	
	protected void includeScriptModel(Class<? extends Model> modelClass, boolean includeHasMany) {
		renderer.includeScriptModel(modelClass, includeHasMany);
	}
	
	protected void includeScriptModels() {
		includeScriptModels(false);
	}
	
	protected void includeScriptModels(boolean includeHasMany) {
		renderer.includeScriptModel(Model.class, includeHasMany);
	}
}
