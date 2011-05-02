package org.oobium.app.server;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.app.routing.Router;
import org.oobium.logging.Logger;

public class WebsocketServerHandler extends SimpleChannelUpstreamHandler {

	final Logger logger;
	final ChannelHandlerContext ctx;
	private final Router router;
	private final Class<? extends WebsocketController> controllerClass;
	final Map<String, Object> params;
	
	private Websocket websocket;
	
	public WebsocketServerHandler(Logger logger, ChannelHandlerContext ctx, Router router, Class<? extends WebsocketController> controllerClass, Map<String, Object> params) {
		this.logger = logger;
		this.ctx = ctx;
		this.router = router;
		this.controllerClass = controllerClass;
		this.params = params;
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if(websocket != null) {
			router.unregisterWebsocket(websocket);
		}
		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.warn(e.getCause().getLocalizedMessage());
		e.getChannel().close();
	}
	
	private WebsocketController getController(ChannelHandlerContext ctx) {
		try {
			WebsocketController controller = controllerClass.newInstance();
			controller.init(logger, this, ctx, params);
			return controller;
		} catch(Exception e) {
			if(logger.isLoggingDebug()) {
				logger.debug(e);
			} else {
				logger.warn(e.getLocalizedMessage());
			}
			return null;
		}
	}

	public Websocket getWebsocket() {
		return websocket;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		WebsocketController controller = getController(ctx);
		if(controller == null)  {
			ctx.getChannel().close();
		} else {
			controller.frameReceived((WebSocketFrame) e.getMessage());
		}
	}
	
	public void register(String name) {
		if(this.websocket != null) {
			router.unregisterWebsocket(websocket);
		}
		this.websocket = new Websocket(this, name);
		router.registerWebsocket(websocket);
	}
	
}
