package org.oobium.app.handlers;

import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface Gateway extends ChannelUpstreamHandler {

	public abstract int getPort();
	
}
