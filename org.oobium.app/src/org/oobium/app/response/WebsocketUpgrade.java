package org.oobium.app.response;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.app.routing.Router;

public class WebsocketUpgrade extends Response {

	public final Router router;
	public final Class<? extends WebsocketController> controllerClass;
	public final Map<String, Object> params;
	
	public WebsocketUpgrade(Router router, Class<? extends WebsocketController> controllerClass, Map<String, Object> params) throws Exception {
		super(HTTP_1_1, new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
		addHeader(UPGRADE, Values.WEBSOCKET);
		addHeader(CONNECTION, Values.UPGRADE);

		this.router = router;
		this.controllerClass = controllerClass;
		this.params = params;
	}

}
