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
package org.oobium.manager.controllers.workers;

import java.util.ArrayList;
import java.util.List;

import org.oobium.app.AppService;
import org.oobium.app.workers.Worker;
import org.oobium.manager.ManagerService;
import org.oobium.utils.json.JsonUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class InstallWorker extends Worker {

	private String location;
	
	public InstallWorker(String location) {
		this.location = location;
	}
	
	@Override
	protected void run() {
		List<Bundle> bundles = new ArrayList<Bundle>();
		String[] locations = JsonUtils.toStringList(location).toArray(new String[0]);
		ManagerService manager = AppService.getActivator(ManagerService.class);
		try {
			for(String location : locations) {
				Bundle bundle = manager.getContext().installBundle(location);
				bundles.add(bundle);
			}
			manager.send("installed", locations, null);
		} catch(BundleException e) {
			manager.send("installed", locations, e.getLocalizedMessage());
		}
	}
	
}
