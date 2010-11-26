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
package org.oobium.build.gen.model;

import java.util.List;
import java.util.Map;


public abstract class PropertyBuilder {

	protected PropertyDescriptor descriptor;
	
	public PropertyBuilder(PropertyDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * @return Map of variable declarations (Variable Name => Whole Declaration)
	 */
	public abstract Map<String, String> getDeclarations();
	
	/**
	 * @return List of fully qualified (canonical) class names
	 */
	public abstract List<String> getImports();
	
	/**
	 * @return Map of methods (Method Name => Whole Method)
	 */
	public abstract Map<String, String> getMethods();
	
}
