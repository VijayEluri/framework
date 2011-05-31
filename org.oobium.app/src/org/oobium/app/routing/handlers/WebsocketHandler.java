package org.oobium.app.routing.handlers;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY1;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY2;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL;

import java.security.MessageDigest;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;
import org.oobium.app.response.WebsocketUpgrade;
import org.oobium.app.routing.RouteHandler;
import org.oobium.app.routing.Router;

public class WebsocketHandler extends RouteHandler {

	private final Class<? extends WebsocketController> controllerClass;
	private final String group;
	
	public WebsocketHandler(Router router, Class<? extends WebsocketController> controllerClass, String group, String[][] params) {
		super(router, params);
		this.controllerClass = controllerClass;
		this.group = group;
	}

	private String getWebsocketLocation(Request request) {
		return "ws://" + request.getHost() + ":" + request.getPort() + request.getPath();
	}
	
	private Response handleWebsocketHandshake(Request request) throws Exception {
		WebsocketUpgrade response = new WebsocketUpgrade(router, controllerClass, group, getParamMap());
		
		if(isProtocol06(request)) {
			logger.warn("websockets protocol 6 is not yet implemented");
		}
		else if(isProtocol00(request)) {
			response.addHeader(SEC_WEBSOCKET_ORIGIN, request.getHeader(ORIGIN));
			response.addHeader(SEC_WEBSOCKET_LOCATION, getWebsocketLocation(request));
			String protocol = request.getHeader(SEC_WEBSOCKET_PROTOCOL);
			if(protocol != null) {
				response.addHeader(SEC_WEBSOCKET_PROTOCOL, protocol);
			}
			
			String key1 = request.getHeader(SEC_WEBSOCKET_KEY1);
			String key2 = request.getHeader(SEC_WEBSOCKET_KEY2);
			int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
			int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
			long c = request.getContent().readLong();
			ChannelBuffer input = ChannelBuffers.buffer(16);
			input.writeInt(a);
			input.writeInt(b);
			input.writeLong(c);
			ChannelBuffer output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(input.array()));
			response.setContent(output);
		}
		else {
			response.addHeader(WEBSOCKET_ORIGIN, request.getHeader(ORIGIN));
			response.addHeader(WEBSOCKET_LOCATION, getWebsocketLocation(request));
			String protocol = request.getHeader(SEC_WEBSOCKET_PROTOCOL);
			if(protocol != null) {
				response.addHeader(SEC_WEBSOCKET_PROTOCOL, protocol);
			}
		}
		
		return response;
	}
	
	private boolean isProtocol00(Request request) {
		return request.containsHeader(SEC_WEBSOCKET_KEY1) && request.containsHeader(SEC_WEBSOCKET_KEY2);
	}
	
	private boolean isProtocol06(Request request) {
		return "6".equals(request.getHeader("Sec-WebSocket-Version"));
	}
	
	private boolean isWebsocketHandshake(Request request) {
		return Values.UPGRADE.equalsIgnoreCase(request.getHeader(CONNECTION)) && 
					Values.WEBSOCKET.equalsIgnoreCase(request.getHeader(Names.UPGRADE));
	}
	
	@Override
	public Response routeRequest(Request request) throws Exception {
		if(isWebsocketHandshake(request)) {
			return handleWebsocketHandshake(request);
		}
		return null;
	}
	
}
