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
package org.oobium.manager.controllers;

import static org.oobium.manager.controllers.BundleController.createEvent;
import static org.osgi.framework.FrameworkEvent.ERROR;
import static org.osgi.framework.FrameworkEvent.PACKAGES_REFRESHED;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class RefreshListener implements FrameworkListener {
	
	protected long[] ids;
	protected String[] names;
	
	public RefreshListener(Bundle[] bundles) {
		ids = new long[bundles.length];
		names = new String[bundles.length];
		for(int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
			names[i] = bundles[i].getSymbolicName() + "_" + bundles[i].getVersion();
		}
	}
	
	@Override
	public void frameworkEvent(FrameworkEvent event) {
		int type = event.getType();
		if(type == PACKAGES_REFRESHED) {
			createEvent("refreshed", names, ids, null);
		} else if(type == ERROR) {
			createEvent("refreshed", names, ids, event.getThrowable().getLocalizedMessage());
		}
	}
	
}
