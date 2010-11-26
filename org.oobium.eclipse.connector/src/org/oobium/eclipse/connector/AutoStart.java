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
package org.oobium.eclipse.connector;

import org.eclipse.ui.IStartup;
import org.oobium.app.AppService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

public class AutoStart implements IStartup {

	@Override
	public void earlyStartup() {
		Activator act = AppService.getActivator(Activator.class);
		BundleContext context = act.getContext();

		ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
		if(ref == null) {
			throw new IllegalStateException("Package Admin service is not present");
		} else {
			String symbolicName = "org.oobium.server";
			
			PackageAdmin admin = (PackageAdmin) context.getService(ref);
			Bundle[] bundles = admin.getBundles(symbolicName, null);
			if(bundles.length == 0) {
				throw new IllegalStateException("bundle " + symbolicName + " is not present - cannot continue");
			} else if(bundles.length == 1) {
				try {
					bundles[0].start();
				} catch(BundleException e) {
					act.getLogger().error(e);
				}
			} else {
				throw new IllegalStateException("no more than 2 bundles of " + symbolicName + " may be present to continue");
			}
		}
	}

}
