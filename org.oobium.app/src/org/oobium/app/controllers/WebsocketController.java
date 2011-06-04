package org.oobium.app.controllers;

import java.util.Map;

import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.request.Request;
import org.oobium.app.server.Websocket;
import org.oobium.logging.Logger;

public abstract class WebsocketController {

	protected Logger logger;
	protected Request request;
	protected Websocket websocket;
	
	public void handleConnect() {
		// subclasses to implement
	}
	
	public void handleDisconnect() {
		// subclasses to implement
	}
	
	public void handleError(Throwable cause) {
		// subclasses to implement
	}

	public void handleMessage(WebSocketFrame frame) {
		// subclasses to implement
	}

	/**
	 * Handle a Registration request from the client. A Registration request is a textual WebSocket request
	 * which contains a JSON map of one entry: "registration". The contents of the JSON map are parsed and
	 * passed into this method as the properties argument. These properties can then be used to determine
	 * how (by the server) to register the client WebSocket (if at all) by setting the ID or Group of the
	 * given {@link Websocket} object.
	 * <p>Default implementation returns null; subclasses to override.</p>
	 * @param the {@link Websocket} object to be registered; never null.
	 * @param properties a Map of the registration properties sent by the client; never null.
	 * @see Websocket#setId(String)
	 * @see Websocket#setGroup(String)
	 */
	public void handleRegistration(Map<String, String> properties) {
		// subclasses to override
	}

	public final void init(Logger logger, Request request, Websocket websocket) {
		this.logger = logger;
		this.request = request;
		this.websocket = websocket;
	}

}
