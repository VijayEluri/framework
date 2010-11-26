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
package org.oobium.eclipse.esp;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.oobium.eclipse.esp.editor.EspColorProvider;

/**
 * The example java editor plug-in class.
 * 
 * @since 3.0
 */
public class EspPlugin extends AbstractUIPlugin {

	public static final String ID = EspPlugin.class.getCanonicalName();
	public static final String ESP_PARTITIONING = "__esp_partitioning"; //$NON-NLS-1$

	private static EspPlugin instance;

	public static EspPlugin getDefault() {
		return instance;
	}

	private EspColorProvider colorProvider;
	
	/**
	 * Creates a new plug-in instance.
	 */
	public EspPlugin() {
		instance = this;
	}

	public EspColorProvider getEspColorProvider() {
		if(colorProvider == null) {
			colorProvider = new EspColorProvider();
		}
		return colorProvider;
	}

}
