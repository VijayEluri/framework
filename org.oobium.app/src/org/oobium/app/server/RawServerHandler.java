package org.oobium.app.server;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamChannelStateEvent;
import org.oobium.app.handlers.RequestHandler;

public class RawServerHandler implements ChannelUpstreamHandler {

	private final ChannelPipeline pipeline;
	private final RequestHandlers handlers;
	
	public RawServerHandler(ChannelPipeline pipeline, RequestHandlers handlers) {
		this.pipeline = pipeline;
		this.handlers = handlers;
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if(e instanceof UpstreamChannelStateEvent) {
			UpstreamChannelStateEvent event = (UpstreamChannelStateEvent) e;
			if(event.getState() == ChannelState.OPEN) {
				for(RequestHandler handler : handlers) {
					if(handler instanceof ChannelHandler) {
						pipeline.addFirst(handler.getClass().getName(), (ChannelHandler) handler);
					}
				}
				ctx.getPipeline().remove(this);
			}
		}
		ctx.sendUpstream(e);
	}
	
}
