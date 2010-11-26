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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class UninstallWorker extends BundleWorker {

	public UninstallWorker(Bundle[] bundles) {
		super(bundles);
	}

	@Override
	protected String getEventName() {
		return "uninstalled";
	}
	
	@Override
	protected void run(int i, Bundle bundle) throws BundleException {
		bundle.uninstall();
	}

}
