package org.oobium.app.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.HashSet;
import java.util.Set;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.controllers.IParams;

public class Websocket implements IParams {

	private final WebsocketServerHandler handler;
	private final String name;
	
	public Websocket(WebsocketServerHandler handler, String name) {
		this.handler = handler;
		this.name = name;
	}

	public ChannelFuture close() {
		return handler.ctx.getChannel().close();
	}

	public String getName() {
		return name;
	}
	
	@Override
	public Object getParam(String name) {
		return (handler.params != null) ? handler.params.get(name) : null;
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
		return (handler.params != null) ? handler.params.keySet() : new HashSet<String>(0);
	};
	
	@Override
	public boolean hasParam(String name) {
		return handler.params != null && handler.params.containsKey(name);
	}
	
	@Override
	public boolean hasParams() {
		return handler.params != null && !handler.params.isEmpty();
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
	
	public ChannelFuture write(WebSocketFrame frame) {
		return handler.ctx.getChannel().write(frame);
	}
	
}
