package org.oobium.client.websockets;

import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.junit.Test;

public class WebsocketsTests {

	@Test
	public void testConnect() throws Exception {
		Websockets.connect("ws://localhost:5000/lowercase", new WebsocketListener() {
			@Override
			public void onMessage(Websocket websocket, WebSocketFrame frame) {
				System.out.println(frame.getTextData());
			}
			
			@Override
			public void onError(Websocket websocket, Throwable t) {
				t.printStackTrace();
			}
			
			@Override
			public void onDisconnect(Websocket websocket) {
				System.out.println("disconnected");
			}
			
			@Override
			public void onConnect(Websocket websocket) {
				System.out.println("connected");
				websocket.register("testClient");
			}
		});
		
		while(true) {
			Thread.sleep(100);
		}
	}

}
