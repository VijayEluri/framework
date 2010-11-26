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

import static org.oobium.manager.controllers.BundleController.createEvent;

import org.oobium.app.workers.Worker;
import org.oobium.manager.ManagerService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public abstract class BundleWorker extends Worker {

	protected long[] ids;
	protected String[] names;
	
	public BundleWorker(Bundle[] bundles) {
		ids = new long[bundles.length];
		names = new String[bundles.length];
		for(int i = 0; i < bundles.length; i++) {
			ids[i] = bundles[i].getBundleId();
			names[i] = bundles[i].getSymbolicName() + "_" + bundles[i].getVersion();
		}
	}

	protected abstract String getEventName();
	
	@Override
	protected void run() {
		BundleContext context = ManagerService.context();
		for(int i = 0; i < ids.length; i++) {
			long id = ids[i];
			Bundle bundle = context.getBundle(id);
			if(bundle == null) {
				createEvent(getEventName(), names, ids, "bundle does not exist for id: " + id);
				return;
			} else {
				// TODO verify the name is still the same?
				try {
					run(i, bundle);
				} catch(BundleException e) {
					createEvent(getEventName(), names, ids, e.getLocalizedMessage());
					return;
				}
			}
		}
		createEvent(getEventName(), names, ids, null);
	}

	protected abstract void run(int i, Bundle bundle) throws BundleException;

}
