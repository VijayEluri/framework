package org.oobium.app.server;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.oobium.app.request.Request;

public class OobiumHttpRequestDecoder extends HttpMessageDecoder {

	private Channel channel;
	
	@Override
	protected HttpMessage createMessage(String[] initialLine) throws Exception {
		HttpVersion version = HttpVersion.valueOf(initialLine[2]);
		HttpMethod method = HttpMethod.valueOf(initialLine[0]);
		String uri = initialLine[1];
		
		return new Request(version, method, uri, channel);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, State state) throws Exception {
		this.channel = channel;
		return super.decode(ctx, channel, buffer, state);
	}
	
	@Override
	protected boolean isDecodingRequest() {
		return true;
	}

}
