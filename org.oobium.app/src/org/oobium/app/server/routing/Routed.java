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
package org.oobium.app.server.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Routed {

	private final Router router;
	private final Route[] routes;
	
	Routed(Router router, Route...routes) {
		this.router = router;
		this.routes = routes;
	}
	
	Routed(Router router, Collection<Routed> routed) {
		this.router = router;
		this.routes = new Route[routed.size()];
		List<Route> list = new ArrayList<Route>();
		for(Routed r : routed) {
			list.addAll(Arrays.asList(r.routes));
		}
	}
	
	public Routed publish() {
		for(Route route : routes) {
			router.publish(route);
		}
		return this;
	}
	
}
