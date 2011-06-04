package org.oobium.client.websockets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;

public class Websockets {

	
	private static NioClientSocketChannelFactory socketChannelFactory;

	static {
		socketChannelFactory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
	}

	public static Websocket create(String url, WebsocketListener listener) {
		if(url.indexOf("://") == -1) {
			url = "ws://" + url;
		}
		
        URI uri;
		try {
			uri = new URI(url);
		} catch(URISyntaxException e) {
			throw new RuntimeException("invalid url syntax: " + url);
		}
        
        String protocol = uri.getScheme();
        if (!protocol.equals("ws") && !protocol.equals("wss")) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        ClientBootstrap bootstrap = new ClientBootstrap(socketChannelFactory);
        final WebsocketHandler handler = new WebsocketHandler(bootstrap, uri);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new HttpResponseDecoder());
                pipeline.addLast("encoder", new HttpRequestEncoder());
                pipeline.addLast("wshandler", handler);
                return pipeline;
            }
        });

        Websocket socket = handler.socket;
        socket.addListener(listener);
        return socket;
	}

	public static Websocket connect(String url, WebsocketListener listener) {
		Websocket socket = create(url, listener);
		socket.connect();
		return socket;
	}

}
