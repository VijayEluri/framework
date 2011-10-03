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
package org.oobium.build.workspace;

import java.io.File;
import java.util.jar.Manifest;

public class Fragment extends Bundle {

	/**
	 * the name of this fragment's host (Fragment-Host).
	 */
	public final String host;

	Fragment(Type type, File file, Manifest manifest) {
		super(type, file, manifest);
		this.host = parseHost(manifest);
	}

	private String parseHost(Manifest manifest) {
		String name = (String) manifest.getMainAttributes().getValue("Fragment-Host");
		if(name != null) {
			int ix = name.indexOf(';');
			if(ix == -1) {
				return name.trim();
			} else {
				return name.substring(0, ix).trim();
			}
		}
		return "";
	}

}
