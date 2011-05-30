package org.oobium.app.server;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamChannelStateEvent;

public class ServerGatewayHandler implements ChannelUpstreamHandler {

	private final ChannelPipeline pipeline;
	private final RequestHandlers handlers;
	
	public ServerGatewayHandler(ChannelPipeline pipeline, RequestHandlers handlers) {
		this.pipeline = pipeline;
		this.handlers = handlers;
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if(e instanceof UpstreamChannelStateEvent) {
			UpstreamChannelStateEvent event = (UpstreamChannelStateEvent) e;
			if(event.getState() == ChannelState.OPEN) {
				int port = ((InetSocketAddress) ctx.getChannel().getLocalAddress()).getPort();
				
				for(ChannelHandler handler : handlers.getChannelHandlers(port)) {
					pipeline.addFirst(handler.getClass().getName(), handler);
				}
				
				ctx.getPipeline().remove(this);
			}
		}
		ctx.sendUpstream(e);
	}
	
}
