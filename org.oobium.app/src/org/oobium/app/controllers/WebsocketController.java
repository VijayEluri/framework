package org.oobium.app.controllers;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.request.Request;
import org.oobium.logging.Logger;

public abstract class WebsocketController implements IParams {

	protected Logger logger;
	protected Request request;
	private ChannelHandlerContext ctx;
	private Map<String, Object> params;
	
	protected ChannelFuture close() {
		return ctx.getChannel().close();
	}
	
	@Override
	public Object getParam(String name) {
		return (params != null) ? params.get(name) : null;
	}
	
	@Override
	public <T> T getParam(String name, Class<T> clazz) {
		return coerce(getParam(name), clazz);
	}
	
	public <T> T getParam(String name, T defaultValue) {
		return coerce(getParam(name), defaultValue);
	}
	
	@Override
	public Set<String> getParams() {
		return (params != null) ? params.keySet() : new HashSet<String>(0);
	}

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
	 * Register this WebSocket with the system so that it may be accessed and written
	 * to from other controllers and processes.
	 * <p>Note: name must be at least 3 characters.</p>
	 * <p>Default implementation returns null; subclasses to override.</p>
	 * @param properties a Map of the registration properties sent by the client
	 * @return a String that is the name of newly registered client; null if this client is not to be registered
	 */
	public String handleRegistration(Map<String, String> properties) {
		return null;
	}
	
	@Override
	public boolean hasParam(String name) {
		return params != null && params.containsKey(name);
	};
	
	@Override
	public boolean hasParams() {
		return params != null && !params.isEmpty();
	}
	
	public final void init(Logger logger, ChannelHandlerContext ctx, Request request, Map<String, Object> params) {
		this.logger = logger;
		this.ctx = ctx;
		this.request = request;
		this.params = params;
	}
	
	@Override
	public String param(String name) {
		return getParam(name, String.class);
	}
	
	@Override
	public <T> T param(String name, Class<T> clazz) {
		return getParam(name, clazz);
	}
	
	public <T> T param(String name, T defaultValue) {
		return getParam(name, defaultValue);
	}
	
	@Override
	public Set<String> params() {
		return getParams();
	};
	
	protected ChannelFuture write(WebSocketFrame frame) {
		return ctx.getChannel().write(frame);
	}
	
}
