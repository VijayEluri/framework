package org.oobium.app.routing.handlers;

import org.oobium.app.controllers.RtspController;
import org.oobium.app.controllers.RtspController.RtspMethod;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;

public class RtspHandler extends RouteHandler {

	private final Class<? extends RtspController> controllerClass;
	
	public RtspHandler(Router router, Class<? extends RtspController> controllerClass, String[][] params) {
		super(router, params);
		this.controllerClass = controllerClass;
	}

	@Override
	public Response routeRequest(Request request) throws Exception {
		RtspController controller = controllerClass.newInstance();
		controller.initialize(request, getParamMap());
		
		RtspMethod method = RtspMethod.valueOf(request.getMethod().getName());
		
		return controller.execute(method);
	}
	
}
