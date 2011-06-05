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
package org.oobium.app.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.oobium.app.routing.routes.ControllerRoute;

public class Routed {

	protected final Router router;
	protected final Route[] routes;
	
	Routed(Router router, Route...routes) {
		this.router = router;
		this.routes = routes;
	}
	
	Routed(Router router, Collection<Routed> routed) {
		this.router = router;
		List<Route> list = new ArrayList<Route>();
		for(Routed r : routed) {
			list.addAll(Arrays.asList(r.routes));
		}
		this.routes = list.toArray(new Route[list.size()]);
	}
	
	public Routed publish() {
		for(Route route : routes) {
			router.publish(route);
		}
		return this;
	}

	public Routed setRealm(String realm) {
		for(Route route : routes) {
			if(route.isFixed()) {
				router.addBasicAuthentication(route.path, realm);
			} else if(route instanceof ControllerRoute) {
				((ControllerRoute) route).realm = realm;
			} else {
				throw new UnsupportedOperationException("cannot set Realm on pattern route of type: " + route.getClass().getName());
			}
		}
		return this;
	}
	
}
