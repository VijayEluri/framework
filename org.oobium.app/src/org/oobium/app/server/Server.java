package org.oobium.app.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.oobium.app.AppServer;
import org.oobium.app.handlers.HttpRequest404Handler;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.handlers.RequestHandler;
import org.oobium.logging.Logger;

public class Server {

	Logger logger;
	RequestHandlers handlers;
	ExecutorService executors;
	
	private ChannelFactory channelFactory;
//	private ServerBootstrap server;
	private ChannelGroup channels;
	
	private Thread shutdownHook;

	public Server(Logger logger) {
		this.logger = logger;
		this.handlers = new RequestHandlers();
	}
	
	public HttpRequest404Handler add404Handler(HttpRequest404Handler handler) {
		handlers.add404Handler(handler);
		return handler;
	}

	public HttpRequest500Handler add500Handler(HttpRequest500Handler handler) {
		handlers.add500Handler(handler);
		return handler;
	}
	
	public void addChannel(Channel channel) {
		channels.add(channel);
	}
	
	public <T extends RequestHandler> T addHandler(T handler) {
		ServerConfig config = handler.getServerConfig();
		if(config.isPrimary()) {
			create(config);
		}
		handlers.addRequestHandler(handler);
		return handler;
	}
	
	private void addShutdownHook() {
		shutdownHook = new Thread() {
			@Override
			public void run() {
				dispose();
			}
		};
		try {
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		} catch(IllegalStateException e) {
			// discard - virtual machine is shutting down (yes, this can happen...)
		}
	}
	
	private void createServer(ServerConfig config, boolean secure) {
		if(config.hasPorts(secure)) {
			if(secure) {
				SslContextFactory.addSslContext(this, config);
			}
			
			ServerBootstrap server = new ServerBootstrap(channelFactory);
			server.setPipelineFactory(new ServerPipelineFactory(this, secure));
	
	        server.setOption("child.tcpNoDelay", coerce(config.options().get("tcpNoDelay"), true));
	        server.setOption("child.keepAlive", coerce(config.options().get("keepAlive"), true));
	        for(Entry<?, ?> e : config.options().entrySet()) {
	        	String option = (String) e.getKey();
	        	if(!option.equals("tcpNoDelay") && !option.equals("keepAlive")) {
	        		server.setOption(option, e.getValue());
	        	}
	        }

			for(int port : config.ports(secure)) {
				channels.add(server.bind(new InetSocketAddress(port)));
				logger.info("server bound to {} {}", secure ? "secure port" : "port", port);
			}
		}
	}
	
	private void create(ServerConfig config) {
		if(channelFactory != null) {
			throw new IllegalStateException("server already created");
		}
		
		channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		channels = new DefaultChannelGroup();
		executors = Executors.newCachedThreadPool();

		createServer(config, false);
		createServer(config, true);
		
		addShutdownHook();
	}

	public synchronized void dispose() {
		if(channelFactory != null) {
			logger.debug("disposing server...");

			removeShutdownHook();

			executors.shutdownNow();
			channels.close().awaitUninterruptibly();
			channelFactory.releaseExternalResources();

			executors = null;
			channels = null;
			channelFactory = null;
			
			SslContextFactory.removeSslContext(this);
			
			logger.debug("disposed server");
		}
	}

	public void remove404Handler(HttpRequest404Handler handler) {
		handlers.remove404Handler(handler);
	}
	
	public void remove500Handler(HttpRequest500Handler handler) {
		handlers.remove500Handler(handler);
	}
	
	public void removeHandler(RequestHandler handler) {
		if(handlers.removeRequestHandler(handler)) {
			if(handlers.isEmpty()) {
				logger.info("stopping server");
				dispose();
				logger.info("stopped server");
			} else {
				logger.info("stopped serving {}", handler);
			}
		} else {
			logger.warn("not serving: {}", handler);
		}
	}
	
	private void removeShutdownHook() {
		if(shutdownHook != null) {
			Thread hook;
			synchronized(AppServer.class) {
				hook = shutdownHook;
				shutdownHook = null;
			}
			if(hook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(hook);
				} catch(IllegalStateException e) {
					// discard - virtual machine is shutting down anyway
				}
			}
		}
	}
	
}
