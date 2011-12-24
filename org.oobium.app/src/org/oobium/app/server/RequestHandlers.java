package org.oobium.app.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.netty.channel.ChannelHandler;
import org.oobium.app.handlers.HttpRequest404Handler;
import org.oobium.app.handlers.HttpRequest500Handler;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.handlers.RequestHandler;
import org.oobium.app.request.Request;
import org.oobium.app.response.Response;

public class RequestHandlers implements Iterable<RequestHandler> {

	private List<RequestHandler> requestHandlers;
	private List<HttpRequest404Handler> http404Handlers;
	private List<HttpRequest500Handler> http500Handlers;
	private int rawHandlerCount;

	public Object addRequestHandler(RequestHandler handler) {
		if(requestHandlers == null) {
			requestHandlers = new ArrayList<RequestHandler>();
		}
		requestHandlers.add(handler);
		if(handler instanceof ChannelHandler) {
			rawHandlerCount++;
		}
		return handler;
	}
	
	public Object add404Handler(HttpRequest404Handler handler) {
		if(http404Handlers == null) {
			http404Handlers = new ArrayList<HttpRequest404Handler>();
		}
		http404Handlers.add((HttpRequest404Handler) handler);
		return handler;
	}
	
	public Object add500Handler(HttpRequest500Handler handler) {
		if(http500Handlers == null) {
			http500Handlers = new ArrayList<HttpRequest500Handler>();
		}
		http500Handlers.add((HttpRequest500Handler) handler);
		return handler;
	}

	public void clear() {
		if(requestHandlers != null) {
			requestHandlers.clear();
		}
		if(http404Handlers != null) {
			http404Handlers.clear();
		}
		if(http500Handlers != null) {
			http500Handlers.clear();
		}
		rawHandlerCount = 0;
	}
	
	public Response handle404(Request request) {
		if(http404Handlers != null) {
			for(HttpRequest404Handler handler : http404Handlers) {
				Response response = handler.handle404(request);
				if(response != null) {
					return response;
				}
			}
		}
		return null;
	}
	
	public Response handle500(Request request, Exception exception) {
		if(http500Handlers != null) {
			for(HttpRequest500Handler handler : http500Handlers) {
				Response response = handler.handle500(request, exception);
				if(response != null) {
					return response;
				}
			}
		}
		return null;
	}
	
	public Object handleRequest(Request request) throws Exception {
		if(requestHandlers != null) {
			for(RequestHandler handler : requestHandlers) {
				if(handler instanceof HttpRequestHandler) {
					HttpRequestHandler httpHandler = (HttpRequestHandler) handler;
					request.setHandler(httpHandler);
					Object response = httpHandler.handleRequest(request);
					if(response != null) {
						return response;
					}
				}
			}
		}
		return null;
	}
	
	public boolean hasHttpHandlers() {
		return rawHandlerCount < requestHandlers.size();
	}

	public boolean hasRawHandlers() {
		return rawHandlerCount > 0;
	}

	public boolean isEmpty() {
		return requestHandlers == null;
	}
	
	@Override
	public Iterator<RequestHandler> iterator() {
		return new Iterator<RequestHandler>() {
			int index = 0;
			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove is not supported");
			}
			@Override
			public RequestHandler next() {
				return requestHandlers.get(index++);
			}
			@Override
			public boolean hasNext() {
				return (requestHandlers != null) && index < requestHandlers.size();
			}
		};
	}
	
	public boolean removeRequestHandler(RequestHandler handler) {
		if(requestHandlers != null) {
			if(requestHandlers.remove(handler)) {
				if(requestHandlers.isEmpty()) {
					requestHandlers = null;
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean remove404Handler(HttpRequest404Handler handler) {
		if(http404Handlers != null) {
			if(http404Handlers.remove(handler)) {
				if(http404Handlers.isEmpty()) {
					http404Handlers = null;
				}
				return true;
			}
		}
		return false;
	}

	public boolean remove500Handler(HttpRequest500Handler handler) {
		if(http500Handlers != null) {
			if(http500Handlers.remove(handler)) {
				if(http500Handlers.isEmpty()) {
					http500Handlers = null;
				}
				return true;
			}
		}
		return false;
	}

	public int size() {
		return (requestHandlers != null) ? requestHandlers.size() : 0;
	}
	
}
