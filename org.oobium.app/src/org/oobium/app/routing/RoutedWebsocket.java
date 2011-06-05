package org.oobium.app.routing;

import org.oobium.app.routing.routes.WebsocketRoute;

public class RoutedWebsocket extends Routed {

	private final WebsocketRoute route;
	
	RoutedWebsocket(Router router, WebsocketRoute route) {
		super(router, new Route[] { route });
		this.route = route;
	}

	public Routed inGroup(String group) {
		WebsocketRoute wr = (WebsocketRoute) route;
		wr.group = group;
		router.updateModelNotificationPath(wr);
		return this;
	}
	
}
