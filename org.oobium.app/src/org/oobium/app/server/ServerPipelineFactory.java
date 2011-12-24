package org.oobium.app.server;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class ServerPipelineFactory implements ChannelPipelineFactory {

	private final Server server;
	private final boolean secure;
	
	public ServerPipelineFactory(Server server, boolean secure) {
		this.server = server;
		this.secure = secure;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
		if(secure) {
			SSLContext context = SslContextFactory.getSslContext(server);
			SSLEngine engine = context.createSSLEngine();
			engine.setUseClientMode(false);
			pipeline.addLast("ssl", new SslHandler(engine));
		}
		
		if(server.handlers.hasRawHandlers()) {
			pipeline.addLast("raw", new RawServerHandler(pipeline, server.handlers));
		}

		if(server.handlers.hasHttpHandlers()) {
			pipeline.addLast("decoder", new OobiumHttpRequestDecoder(secure));
			pipeline.addLast("aggregator", new HttpChunkAggregator(1*1024*1024));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
			pipeline.addLast("handler", new ServerHandler(server, secure));
		}
		
		return pipeline;
	}

}
