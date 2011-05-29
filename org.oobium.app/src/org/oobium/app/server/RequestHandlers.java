package org.oobium.app.server;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.oobium.app.handlers.HttpRequest404Handler;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;

public class RequestHandlers {

	private RequestHandlerMap<ChannelUpstreamHandler> channelHandlers;
	private RequestHandlerMap<HttpRequestHandler> requestHandlers;
	private RequestHandlerMap<HttpRequest404Handler> request404Handlers;
	private RequestHandlerMap<HttpRequest500Handler> request500Handlers;

	public Object addHandler(Object handler, int port) {
		if(handler instanceof ChannelUpstreamHandler) {
			if(channelHandlers == null) {
				channelHandlers = new RequestHandlerMap<ChannelUpstreamHandler>();
			}
			channelHandlers.add((ChannelUpstreamHandler) handler, port);
		}
		if(handler instanceof HttpRequestHandler) {
			if(requestHandlers == null) {
				requestHandlers = new RequestHandlerMap<HttpRequestHandler>();
			}
			requestHandlers.add((HttpRequestHandler) handler, port);
		}
		if(handler instanceof HttpRequest404Handler) {
			if(request404Handlers == null) {
				request404Handlers = new RequestHandlerMap<HttpRequest404Handler>();
			}
			request404Handlers.add((HttpRequest404Handler) handler, port);
		}
		if(handler instanceof HttpRequest500Handler) {
			if(request500Handlers == null) {
				request500Handlers = new RequestHandlerMap<HttpRequest500Handler>();
			}
			request500Handlers.add((HttpRequest500Handler) handler, port);
		}
		
		return handler;
	}

	public void clear() {
		if(channelHandlers != null) {
			channelHandlers.clear();
		}
		if(requestHandlers != null) {
			requestHandlers.clear();
		}
		if(request404Handlers != null) {
			request404Handlers.clear();
		}
		if(request500Handlers != null) {
			request500Handlers.clear();
		}
	}
	
	public List<ChannelUpstreamHandler> getChannelHandlers(int port) {
		if(channelHandlers != null) {
			return channelHandlers.get(port);
		}
		return new ArrayList<ChannelUpstreamHandler>(0);
	}

	public int[] getPorts() {
		return (requestHandlers != null) ? requestHandlers.getPorts() : ((channelHandlers != null) ? channelHandlers.getPorts() : new int[0]);
	}

	public Response handle404(Request request) {
		if(request404Handlers != null) {
			List<HttpRequest404Handler> handlers = request404Handlers.get(request.getPort());
			if(handlers != null) {
				for(HttpRequest404Handler handler : handlers) {
					Response response = handler.handle404(request);
					if(response != null) {
						return response;
					}
				}
			}
		}
		return null;
	}
	
	public Response handle500(Request request, Exception exception) {
		if(request500Handlers != null) {
			List<HttpRequest500Handler> handlers = request500Handlers.get(request.getPort());
			if(handlers != null) {
				for(HttpRequest500Handler handler : handlers) {
					Response response = handler.handle500(request, exception);
					if(response != null) {
						return response;
					}
				}
			}
		}
		return null;
	}
	
	public Object handleRequest(Request request) throws Exception {
		if(requestHandlers != null) {
			List<HttpRequestHandler> handlers = requestHandlers.get(request.getPort());
			if(handlers != null) {
				for(HttpRequestHandler handler : handlers) {
					request.setHandler(handler);
					Object response = handler.handleRequest(request);
					if(response != null) {
						return response;
					}
				}
			}
		}
		return null;
	}
	
	public boolean hasChannelHandlers() {
		return channelHandlers != null;
	}
	
	public boolean hasPorts() {
		return (requestHandlers != null) ? requestHandlers.hasPorts() : ((channelHandlers != null) ? channelHandlers.hasPorts() : false);
	}

	public boolean removeHandler(Object handler, int port) {
		RequestHandlerMap<?> map = null;
		if(handler instanceof ChannelUpstreamHandler) {
			map = channelHandlers;
		} else if(handler instanceof HttpRequestHandler) {
			map = requestHandlers;
		} else if(handler instanceof HttpRequest404Handler) {
			map = request404Handlers;
		} else if(handler instanceof HttpRequest500Handler) {
			map = request500Handlers;
		} else {
			throw new IllegalArgumentException();
		}
		if(map != null) {
			if(map.remove(handler, port)) {
				if(map.isEmpty()) {
					if(handler instanceof ChannelUpstreamHandler) {
						channelHandlers = null;
					} else if(handler instanceof HttpRequestHandler) {
						requestHandlers = null;
					} else if(handler instanceof HttpRequest404Handler) {
						request404Handlers = null;
					} else if(handler instanceof HttpRequest500Handler) {
						request500Handlers = null;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public int size(int port) {
		int cports = (channelHandlers != null) ? channelHandlers.size(port) : 0;
		int rports = (requestHandlers != null) ? requestHandlers.size(port) : 0;
		return cports + rports;
	}
	
}
