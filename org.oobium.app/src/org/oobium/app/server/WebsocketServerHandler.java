package org.oobium.app.server;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.app.controllers.WebsocketController;
import org.oobium.app.request.Request;
import org.oobium.app.routing.Router;
import org.oobium.logging.Logger;
import org.oobium.utils.json.JsonUtils;

public class WebsocketServerHandler extends SimpleChannelUpstreamHandler {

	final Logger logger;
	private final Router router;
	final ChannelHandlerContext ctx;
	final Request request;
	private final Class<? extends WebsocketController> controllerClass;
	final Map<String, Object> params;
	
	private Websocket websocket;
	
	public WebsocketServerHandler(Logger logger, Router router, ChannelHandlerContext ctx, Request request, Class<? extends WebsocketController> controllerClass, Map<String, Object> params) {
		this.logger = logger;
		this.router = router;
		this.ctx = ctx;
		this.request = request;
		this.controllerClass = controllerClass;
		this.params = params;

		WebsocketController controller = getController(ctx, request);
		if(controller != null)  {
			try {
				controller.handleConnect();
			} catch(Exception e) {
				if(logger.isLoggingDebug()) logger.warn(e);
				else logger.warn(e.getLocalizedMessage());
				ctx.getChannel().close();
			}
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
		WebsocketController controller = getController(ctx, request);
		if(controller != null)  {
			try {
				controller.handleDisconnect();
			} catch(Exception e) {
				if(logger.isLoggingDebug()) logger.warn(e);
				else logger.warn(e.getLocalizedMessage());
			}
		}
		if(websocket != null) {
			router.unregisterWebsocket(websocket);
		}
		super.channelDisconnected(ctx, event);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) throws Exception {
		Throwable t = event.getCause();
		WebsocketController controller = getController(ctx, request);
		if(controller != null)  {
			try {
				controller.handleError(t);
			} catch(Exception e) {
				if(logger.isLoggingDebug()) logger.warn(e);
				else logger.warn(e.getLocalizedMessage());
			}
		}
		logger.warn(t.getLocalizedMessage());
		event.getChannel().close();
	}
	
	private WebsocketController getController(ChannelHandlerContext ctx, Request request) {
		try {
			WebsocketController controller = controllerClass.newInstance();
			controller.init(logger, ctx, request, params);
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
		WebsocketController controller = getController(ctx, request);
		if(controller == null)  {
			ctx.getChannel().close();
		} else {
			WebSocketFrame frame = (WebSocketFrame) e.getMessage();
			if(frame.isText()) {
				String text = frame.getTextData();
				if(text.length() > 12 && text.startsWith("register:{") && text.charAt(text.length()-1) == '}') {
					Map<String, String> properties = JsonUtils.toStringMap(text.substring(9));
					String name = controller.handleRegistration(properties);
					if(name != null && name.length() > 0) {
						register(name);
					}
					return;
				}
			}
			controller.handleMessage(frame);
		}
	}
	
	private void register(String name) {
		if(this.websocket != null) {
			router.unregisterWebsocket(websocket);
		}
		this.websocket = new Websocket(this, name);
		router.registerWebsocket(websocket);
	}
	
}
