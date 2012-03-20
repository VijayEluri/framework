package org.oobium.app.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.jboss.netty.handler.codec.http.HttpMethod.DELETE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.HEAD;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpMethod.PUT;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.EXPECTATION_FAILED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.PARTIAL_CONTENT;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.oobium.utils.DateUtils.httpDate;
import static org.oobium.utils.StringUtils.blank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.handler.stream.ChunkedStream;
import org.jboss.netty.util.CharsetUtil;
import org.oobium.app.request.Request;
import org.oobium.app.response.StaticResponse;
import org.oobium.app.response.WebsocketUpgrade;
import org.oobium.app.server.netty4.Attribute;
import org.oobium.logging.Logger;
import org.oobium.utils.Config.Mode;

public class ServerHandler extends SimpleChannelUpstreamHandler {

	private final Server server;
	private final Logger logger;
	private final RequestHandlers handlers;
	private final ExecutorService executors;
	private final List<Channel> secureChannels; // TODO: List, Set, or LinkedHashSet?
	
	public ServerHandler(Server server, boolean secure) {
		this.server = server;
		this.logger = server.logger;
		this.handlers = server.handlers;
		this.executors = server.executors;
		if(secure) {
			secureChannels = new ArrayList<Channel>();
		} else {
			secureChannels = null;
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.trace("channel closed");
		super.channelClosed(ctx, e);
	}

	@Override
	public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.trace("channel bound");
		super.channelBound(ctx, e);
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.trace("channel open");
		server.addChannel(e.getChannel());
		super.channelOpen(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.trace("channel connected");
		if(secureChannels != null) {
			SslHandler handler = ctx.getPipeline().get(SslHandler.class);
			ChannelFuture future = handler.handshake();
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture f) throws Exception {
					if(f.isSuccess()) {
						logger.trace("ssl handshake completed successfully");
						secureChannels.add(f.getChannel());
					} else {
						logger.debug("ssl handshake failed");
						f.getChannel().close();
					}
				}
			});
		}
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.trace("channel disconnected");
		if(secureChannels != null) {
			secureChannels.remove(ctx.getChannel());
		}
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.trace("channel unbound");
		super.channelUnbound(ctx, e);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if(logger.isLoggingDebug()) {
			logger.warn(e.getCause());
		} else {
			Throwable t = e.getCause();
			logger.warn(t.getClass() + ": " + t.getLocalizedMessage());
		}
		e.getChannel().close();
	}

	private HttpResponse get404Response(Request request) {
		HttpResponse response = null;
		try {
			response = handlers.handle404(request);
		} catch(Exception e) {
			logger.warn("error handling 404", e);
		}
		if(response == null) {
			// there was either an error in the 404 handler, or no 404 handler exists in the system
			response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
			response.setContent(ChannelBuffers.copiedBuffer(NOT_FOUND.getReasonPhrase(), CharsetUtil.UTF_8));
		}
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
		return response;
	}

	private HttpResponse get500Response(Request request, Throwable cause) {
		logger.warn(cause);
		HttpResponse response = null;
		try {
			response = handlers.handle500(request, cause);
		} catch(Exception e2) {
			logger.warn("error handling 500", e2);
		}
		if(response == null) {
			// there was either an error in the 500 handler, or no 500 handler exists in the system
			response = new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
			response.setContent(ChannelBuffers.copiedBuffer(INTERNAL_SERVER_ERROR.getReasonPhrase(), CharsetUtil.UTF_8));
		}
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
		return response;
	}

	private int[] getRange(Request request, HttpResponse response) {
		String header = request.getHeader(Names.RANGE);
		if(header != null) {
			String[] sa = header.split("\\s*=\\s*", 2);
			if(sa.length == 2 && Names.RANGE.equalsIgnoreCase(sa[0])) {
				sa = sa[1].split("\\s*,\\s*");
				if(sa.length == 1) {
					if(sa[0].length() == 0) {
						logger.warn("empty range");
					} else {
						int clen;
						header = response.getHeader(CONTENT_LENGTH);
						if(header == null) {
							ChannelBuffer content = response.getContent();
							clen = (content != null) ? content.readableBytes() : 0;
						} else {
							clen = Integer.parseInt(header);
						}
						
						if(sa[0].charAt(0) == '-') {
							// bytes=-500
							int len = Integer.parseInt(sa[0].substring(1));
							return new int[] { (len > clen) ? 0 : (len-clen), clen };
						}
						else if(sa[0].charAt(sa[0].length()-1) == '-') {
							// bytes=500-
							int start = Integer.parseInt(sa[0].substring(0, sa[0].length()-1));
							return new int[] { (start > clen) ? -1 : start, clen };
						}
						else {
							// bytes=500-1000
							sa = sa[0].split("\\s*-\\s*", 2);
							int start = Integer.parseInt(sa[0]);
							if(start < clen) {
								int end = Integer.parseInt(sa[1]);
								if(start < end) {
									return new int[] { start, end };
								}
							}
							return new int[] { -1, -1 };
						}
					}
				} else {
					logger.warn("multiple ranges not yet supported");
				}
			}
		}
		return null;
	}
	
	private void handleHttpRequest(final Channel channel, final Request request) {
		if(logger.isLoggingInfo()) {
			logger.info("{} {} -> {}", request.getRemoteAddress(), request.getHeader(HttpHeaders.Names.USER_AGENT), request.getUri());
		}
		if(logger.isLoggingDebug()) {
			logger.debug(request.toString());
		}
		
		if(request.getMethod() == POST) {
			String expect = request.getHeader(HttpHeaders.Names.EXPECT);
			if(!blank(expect)) {
				// TODO support HTTP Header: Expect
				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, EXPECTATION_FAILED);
				response.setContent(ChannelBuffers.copiedBuffer(EXPECTATION_FAILED.getReasonPhrase(), CharsetUtil.UTF_8));
				writeResponse(channel, request, response);
				return;
			}
			Object attr = request.getParameters().get("_method");
			if(attr instanceof Attribute) {
				try {
					String method = ((Attribute) attr).getValue();
					if("DELETE".equalsIgnoreCase(method)) {
						request.setMethod(DELETE);
					} else if("PUT".equalsIgnoreCase(method)) {
						request.setMethod(PUT);
					}
				} catch(IOException e) {
					logger.warn(e);
				}
			}
		}
		
		Object response = null;
		try {
			response = handlers.handleRequest(request);
		} catch(Exception e) {
			logger.warn("exception thrown handling request (" + request.getUri() + ")", e);
			response = get500Response(request, e);
		}
		
		if(response == null) {
			writeResponse(channel, request, null);
		}
		else if(response instanceof WebsocketUpgrade) {
			upgradeToWebsocket(channel, request, (WebsocketUpgrade) response);
		}
		else if(response instanceof HttpResponse) {
			writeResponse(channel, request, (HttpResponse) response);
		}
		else if(response instanceof HandlerTask) {
			HandlerTask task = (HandlerTask) response;
			task.setListener(new HandlerTaskListener() {
				@Override
				public void onComplete(HandlerTask task) {
					logger.trace("task complete");
					if(channel.isConnected()) {
						HttpResponse httpResponse = task.isSuccess() ? task.getResponse() : get500Response(request, task.getCause());
						writeResponse(channel, request, httpResponse);
					} else {
						logger.debug("skipping write - channel not connected");
					}
				}
			});
			logger.trace("submitting task");
			executors.submit(task);
		}
		else {
			throw new IllegalStateException("response: " + response);
		}
	}
	
	private boolean isNotModified(HttpRequest request, HttpResponse response) {
		if(Mode.isNotDEV() && request.getMethod() == GET && response.getStatus() == OK) {
			String lastModified = response.getHeader(HttpHeaders.Names.LAST_MODIFIED);
			if(!blank(lastModified)) {
				String ifMod = request.getHeader(HttpHeaders.Names.IF_MODIFIED_SINCE);
				if(ifMod != null && ifMod.length() > 0) {
					if(ifMod.equals(lastModified)) {
						return true;
					}
					try {
						Date since = httpDate(ifMod);
						Date last = httpDate(lastModified);
						if(!since.after(last)) {
							return true;
						}
					} catch(ParseException e) {
						if(logger.isLoggingTrace()) {
							logger.trace(e.getMessage());
						}
						// fall through
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Request request = (Request) e.getMessage();
		if(secureChannels == null) {
			handleHttpRequest(ctx.getChannel(), request);
		} else {
			for(Channel channel : secureChannels) {
				handleHttpRequest(channel, request);
			}
		}
	}

	private void setContentLength(HttpResponse response) {
		// note that a StaticResponse should always have its ContentLength set by now
		if(!response.containsHeader(CONTENT_LENGTH) && !response.containsHeader(TRANSFER_ENCODING)) {
			ChannelBuffer content = response.getContent();
			response.setHeader(CONTENT_LENGTH, (content != null) ? content.readableBytes() : 0);
		}
	}
	
	private void upgradeToWebsocket(Channel channel, Request request, WebsocketUpgrade upgrade) {
		ChannelPipeline pipeline = channel.getPipeline();
		pipeline.remove("aggregator");
		pipeline.replace("decoder", "wsdecoder", new WebSocketFrameDecoder());
		
		ChannelFuture future = writeResponse(channel, upgrade);
		future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		
		pipeline.replace("encoder", "wsencoder", new WebSocketFrameEncoder());
		pipeline.replace("handler", "wshandler", new WebsocketServerHandler(logger, channel, request, upgrade));
	}
	
	private ChannelFuture writePayload(Channel channel, StaticResponse response) {
		return writePayload(channel, response, null);
	}
	
	private ChannelFuture writePayload(Channel channel, StaticResponse response, int[] range) {
		Object payload = response.getPayload();

		if(payload instanceof ChannelBuffer) {
			if(range != null) {
				((ChannelBuffer) payload).setIndex(range[0], range[1]);
			}
			return channel.write(payload);
		}
		
		if(payload instanceof String) {
			if(range != null) {
				payload = ((String) payload).substring(range[0], range[1]);
			}
			return channel.write(ChannelBuffers.copiedBuffer((String) payload, CharsetUtil.UTF_8));
		}

		if(payload instanceof File) {
			// TODO better file server: http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/file/HttpStaticFileServerHandler.html
			try {
				if(range == null) {
					payload = new ChunkedFile((File) payload);
				} else {
					RandomAccessFile raf = new RandomAccessFile((File) payload, "r");
					payload = new ChunkedFile(raf, range[0], range[1]-range[0], 8192);
				}
				return channel.write(payload);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		if(payload instanceof URL) {
			try {
				payload = ((URL) payload).openStream();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		if(payload instanceof InputStream) {
			if(range == null) {
				payload = new ChunkedStream((InputStream) payload);
			} else {
				try {
					payload = new ChunkedPartialStream((InputStream) payload, range);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
			return channel.write(payload);
		}
		
		throw new UnsupportedOperationException("unsupported payload type: " + payload);
	}
	
	private ChannelFuture writeResponse(Channel channel, HttpResponse response) {
		if(logger.isLoggingDebug()) {
			logger.debug(response.toString());
		}
		return channel.write(response);
	}

	private void writeResponse(Channel channel, Request request, HttpResponse response) {
		if(response == null) {
			response = get404Response(request);
		}
		ChannelFuture future;
		if(isNotModified(request, response)) {
			response.setStatus(NOT_MODIFIED);
			response.setContent(null);
			response.setHeader(CONTENT_LENGTH, 0);
			future = writeResponse(channel, response);
		}
		else {
			if(request.getMethod() == HEAD) {
				setContentLength(response);
				response.setContent(null);
				future = writeResponse(channel, response);
			}
			else {
				int[] range = getRange(request, response);
				if(range == null) {
					setContentLength(response);
					future = writeResponse(channel, response);
					if(response instanceof StaticResponse) {
						future = writePayload(channel, (StaticResponse) response);
					}
				} else {
					if(range[0] == -1) { // invalid range
						response = new DefaultHttpResponse(HTTP_1_1, REQUESTED_RANGE_NOT_SATISFIABLE);
						response.setContent(ChannelBuffers.copiedBuffer(REQUESTED_RANGE_NOT_SATISFIABLE.getReasonPhrase(), CharsetUtil.UTF_8));
						future = writeResponse(channel, response);
					} else {
						response.setStatus(PARTIAL_CONTENT);
						response.setHeader(CONTENT_LENGTH, range[1]-range[0]);
						ChannelBuffer content = response.getContent();
						if(content != null && content.readableBytes() > 0) {
							content.setIndex(range[0], range[1]);
						}
						future = writeResponse(channel, response);
						if(response instanceof StaticResponse) {
							future = writePayload(channel, (StaticResponse) response, range);
						}
					}
				}
			}
		}
		
		if(!isKeepAlive(request)) {
			logger.trace("not keepalive - adding close listener");
			future.addListener(ChannelFutureListener.CLOSE);
		}
		future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

		if(logger.isLoggingTrace()) {
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture arg0) throws Exception {
					logger.trace("write complete");
				}
			});
		}

		request.dispose(); // release any temporary files in the parameters
	}
	
}
