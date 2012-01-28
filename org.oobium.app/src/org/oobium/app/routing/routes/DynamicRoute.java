package org.oobium.app.routing.routes;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.routing.Route;
import org.oobium.app.routing.RouteGetter;

public class DynamicRoute extends Route {

	public final RouteGetter getter;
	
	public DynamicRoute(HttpMethod method, String rule, RouteGetter getter) {
		super(Route.HTTP_CONTROLLER, method, rule);
		this.getter = getter;

		List<String[]> params = new ArrayList<String[]>();
		parseRules(rule, null, params);
		
		matchOnFullPath = (pattern != null) ? (pattern.pattern().indexOf('?') != -1) : false;

		setString();
	}

	@Override
	protected String[][] params() {
		return null;
	}

	private void setString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(httpMethod).append(']').append(' ');
		sb.append(isFixed() ? path : pattern.pattern()).append(' ').append('-').append('>').append(' ');
		sb.append(getter);
		string = sb.toString();
	}

}
