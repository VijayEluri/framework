package org.oobium.app.handlers;

import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class GatewayDecoder extends SimpleChannelUpstreamHandler implements Gateway {

	private final int port;
	
	public GatewayDecoder(int port) {
		this.port = port;
	}
	
	@Override
	public int getPort() {
		return port;
	}
	
}
