package org.oobium.app.routing.routes;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.controllers.Controller;
import org.oobium.app.http.Action;
import org.oobium.persist.Model;

public class HasManyRoute extends ControllerRoute {

	public final Class<? extends Model> parentClass;
	public final String hasManyField;
	
	public HasManyRoute(HttpMethod method, String rule, Class<? extends Model> parentClass, String hasManyField, Class<? extends Model> modelClass, Class<? extends Controller> controllerClass, Action action) {
		super(method, rule, modelClass, controllerClass, action);
		this.parentClass = parentClass;
		this.hasManyField = hasManyField;
	}

}
