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
package org.oobium.app.dev.controllers;

import java.sql.SQLException;

import org.oobium.app.dev.AppDevActivator;
import org.oobium.app.server.controller.Controller;
import org.oobium.logging.LogProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class ShutdownController extends Controller {

	@Override
	public void handleRequest() throws SQLException {
		System.out.println("shutdown");
		Bundle bundle;
		if(hasParam("app")) {
			bundle = AppDevActivator.getActivator(param("app")).getContext().getBundle();
		} else {
			bundle = AppDevActivator.getActivators()[0].getContext().getBundle(0);
		}
		if(bundle != null) {
			try {
				System.out.println("bundle: " + bundle);
				bundle.stop();
			} catch(BundleException e) {
				System.out.println("oophta!");
				LogProvider.getLogger().error("could not shutdown", e);
			}
		}
	}

}
