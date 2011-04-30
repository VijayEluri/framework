package org.oobium.app.server;

import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.oobium.logging.Logger;

public class ServerPipelineFactory implements ChannelPipelineFactory {

	private final Logger logger;
	private final RequestHandlers handlers;
	private final ExecutorService executors;
	
	public ServerPipelineFactory(Logger logger, RequestHandlers handlers, ExecutorService executors) {
		this.logger = logger;
		this.handlers = handlers;
		this.executors = executors;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("decoder", new OobiumHttpRequestDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(1*1024*1024));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		
		pipeline.addLast("handler", new ServerHandler(logger, handlers, executors));
		
		return pipeline;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
