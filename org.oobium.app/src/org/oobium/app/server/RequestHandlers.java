package org.oobium.app.server;

import java.util.List;

import org.oobium.app.handlers.HttpRequest404Handler;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;

public class RequestHandlers {

	private RequestHandlerMap<HttpRequestHandler> requestHandlers;
	private RequestHandlerMap<HttpRequest404Handler> request404Handlers;
	private RequestHandlerMap<HttpRequest500Handler> request500Handlers;

	public Object addHandler(Object handler, int port) {
		if(handler instanceof HttpRequestHandler) {
			if(requestHandlers == null) {
				requestHandlers = new RequestHandlerMap<HttpRequestHandler>();
			}
			requestHandlers.add((HttpRequestHandler) handler, port);
			return handler;
		}
		if(handler instanceof HttpRequest404Handler) {
			if(request404Handlers == null) {
				request404Handlers = new RequestHandlerMap<HttpRequest404Handler>();
			}
			request404Handlers.add((HttpRequest404Handler) handler, port);
			return handler;
		}
		if(handler instanceof HttpRequest500Handler) {
			if(request500Handlers == null) {
				request500Handlers = new RequestHandlerMap<HttpRequest500Handler>();
			}
			request500Handlers.add((HttpRequest500Handler) handler, port);
			return handler;
		}
		
		throw new IllegalArgumentException("unknown handler type: " + ((handler != null) ? handler.getClass().getName() : "null"));
	}

	public void clear() {
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
	
	public boolean hasPorts() {
		return (requestHandlers != null) ? requestHandlers.hasPorts() : false;
	}
	
	public int[] getPorts() {
		return (requestHandlers != null) ? requestHandlers.getPorts() : new int[0];
	}
	
	public boolean removeHandler(Object handler, int port) {
		RequestHandlerMap<?> map = null;
		if(handler instanceof HttpRequestHandler) {
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
					if(handler instanceof HttpRequestHandler) {
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
		return (requestHandlers != null) ? requestHandlers.size(port) : 0;
	}
	
}
