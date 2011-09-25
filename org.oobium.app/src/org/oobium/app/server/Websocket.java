package org.oobium.app.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.HashSet;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.controllers.IParams;
import org.oobium.app.routing.Router;

public class Websocket implements IParams {

	private final WebsocketServerHandler handler;
	String id;
	String group;
	
	Websocket(WebsocketServerHandler handler, String id, String group) {
		this.handler = handler;
		this.id = id;
		this.group = group;
	}

	public ChannelFuture disconnect() {
		return handler.channel.close();
	}

	/**
	 * Set the unique ID of this Websocket object. Setting the ID to a non-null value
	 * will register it with the {@link Router}, thereby making it accessible to the
	 * controllers, and any other objects that have access to the {@link Router}.
	 * <p>There can only be one Websocket object with a given ID at any time. If there
	 * was already one registered with the given ID then it will be replaced with this
	 * call (but not disconnected).</p>
	 * @param id a String to register the Websocket
	 */
	public void setId(String id) {
		handler.register(id, group);
	}
	
	public void setGroup(String group) {
		handler.register(id, group);
	}
	
	public String getId() {
		return id;
	}
	
	public String getGroup() {
		return group;
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
	
	@Override
	public String toString() {
		if(id == null && group == null) {
			return getClass().getSimpleName();
		}
		if(id != null) {
			return getClass().getSimpleName() + "{id:'" + id + "'}";
		}
		if(group != null) {
			return getClass().getSimpleName() + "{group:'" + group + "'}";
		}
		return getClass().getSimpleName() + "{id:'" + id + "',group:'" + group + "'}";
	}
	
	public ChannelFuture write(int type, byte[] binaryData) {
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(binaryData);
		return write(new DefaultWebSocketFrame(type, buffer));
	}
	
	public ChannelFuture write(int type, ChannelBuffer binaryData) {
		return write(new DefaultWebSocketFrame(type, binaryData));
	}
	
	public ChannelFuture write(String textData) {
		return write(new DefaultWebSocketFrame(textData));
	}
	
	public ChannelFuture write(WebSocketFrame frame) {
		return handler.channel.write(frame);
	}
	
}
