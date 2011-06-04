package org.oobium.client.websockets;

import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

public interface WebsocketListener {

	public abstract void onMessage(Websocket websocket, WebSocketFrame frame);
	
	public abstract void onError(Websocket websocket, Throwable t);
	
	public abstract void onDisconnect(Websocket websocket);
	
	public abstract void onConnect(Websocket websocket);
	
}
