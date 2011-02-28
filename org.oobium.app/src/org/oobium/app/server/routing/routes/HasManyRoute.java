package org.oobium.app.server.routing.routes;

import org.oobium.app.server.controller.Controller;
import org.oobium.http.constants.Action;
import org.oobium.http.constants.RequestType;
import org.oobium.persist.Model;

public class HasManyRoute extends ControllerRoute {

	public final Class<? extends Model> parentClass;
	public final String hasManyField;
	
	public HasManyRoute(RequestType requestType, String rule, Class<? extends Model> parentClass, String hasManyField, Class<? extends Model> modelClass, Class<? extends Controller> controllerClass, Action action) {
		super(requestType, rule, modelClass, controllerClass, action);
		this.parentClass = parentClass;
		this.hasManyField = hasManyField;
	}

}
