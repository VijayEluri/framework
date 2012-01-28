package org.oobium.app.routing;

import org.oobium.app.request.Request;

public interface RouteGetter {

	public abstract Route getRoute(Router router, Request request);
	
}
