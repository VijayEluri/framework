package org.oobium.app.routing;

import java.util.Collection;

import org.oobium.app.http.Action;

public class RoutedRoutes extends Routed {

	private Routes routes;
	
	RoutedRoutes(Routes routes, Collection<Routed> routed) {
		super(routes.router, routed);
		this.routes = routes;
	}
	
	public RoutedRoutes hasMany(String field, Action...actions) {
		return routes.hasMany(field, actions);
	}

}
