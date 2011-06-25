package org.oobium.app.request;

import static org.jboss.netty.handler.codec.http.HttpMethod.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.oobium.app.handlers.HttpRequestHandler;
import org.oobium.app.http.MimeType;
import org.oobium.app.server.netty4.HttpData;
import org.oobium.app.server.netty4.HttpPostRequestDecoder;
import org.oobium.app.server.netty4.HttpPostRequestDecoder.ErrorDataDecoderException;
import org.oobium.app.server.netty4.HttpPostRequestDecoder.IncompatibleDataDecoderException;
import org.oobium.app.server.netty4.HttpPostRequestDecoder.NotEnoughDataDecoderException;
import org.oobium.app.server.netty4.InterfaceHttpData;

public class Request extends DefaultHttpRequest {

	private String host;
	private int port;
	private String remoteAddress;
	private int remotePort;
	private QueryStringDecoder queryDecoder;
	private List<MimeType> accepts;
	private Map<String, Cookie> cookies;
	private Map<String, Object> parameters;

	private HttpRequestHandler handler;
	
	public Request(HttpVersion httpVersion, HttpMethod method, String uri, Channel channel) {
		super(httpVersion, method, uri);
		InetSocketAddress remote = (InetSocketAddress) channel.getRemoteAddress();
		remoteAddress = remote.getAddress().getHostAddress();
		remotePort = remote.getPort();
		InetSocketAddress local = (InetSocketAddress) channel.getLocalAddress();
		port = local.getPort();
	}
	
	public Request(HttpVersion httpVersion, HttpMethod method, String uri, int port) {
		super(httpVersion, method, uri);
		this.port = port;
	}
	
	public void dispose() {
		host = null;
		queryDecoder = null;
		if(accepts != null) {
			accepts.clear();
			accepts = null;
		}
		if(cookies != null) {
			cookies.clear();
			cookies = null;
		}
		if(parameters != null) {
			for(Object o : parameters.values()) {
				if(o instanceof HttpData) {
					((HttpData) o).delete();
				}
			}
			parameters.clear();
			parameters = null;
		}
	}

	public List<MimeType> getAcceptedTypes() {
		if(accepts == null) {
			accepts = new ArrayList<MimeType>();
			String path = getPath();
			int ix = path.indexOf('.');
			if(ix != -1) {
				MimeType type = MimeType.getFromExtension(path.substring(ix+1));
				if(type != null) {
					accepts.add(type);
				}
			}
			for(String header : getHeaders(HttpHeaders.Names.ACCEPT)) {
				accepts.addAll(MimeType.getAll(header));
			}
		}
		return accepts;
	}
	
	public Cookie getCookie(String name) {
		if(cookies == null) {
			loadCookies();
		}
		return cookies.get(name);
	}
	
	public String getCookieValue(String name) {
		Cookie cookie = getCookie(name);
		if(cookie != null) {
			return cookie.getValue();
		}
		return null;
	}
	
	public HttpRequestHandler getHandler() {
		return handler;
	}

	public String getHost() {
		if(host == null) {
			host = getHeader(HttpHeaders.Names.HOST);
			if(host == null) {
				host = "unknown";
			} else {
				int ix = host.indexOf(':');
				if(ix != -1) {
					host = host.substring(0, ix);
				}
			}
		}
		return host;
	}

	public Map<String, Object> getParameters() {
		if(parameters == null) {
			parameters = new HashMap<String, Object>();

			if(getMethod() == POST || getMethod() == PUT) {
				long length = HttpHeaders.getContentLength(this);
				if(length > 0) {
					try {
						HttpPostRequestDecoder bodyDecoder = new HttpPostRequestDecoder(this);
						for(InterfaceHttpData data : bodyDecoder.getBodyHttpDatas()) {
							parameters.put(data.getName(), data);
						}
					} catch(ErrorDataDecoderException e) {
						e.printStackTrace();
					} catch(IncompatibleDataDecoderException e) {
						e.printStackTrace();
					} catch(NotEnoughDataDecoderException e) {
						e.printStackTrace();
					}
				}
			}

			// query parameters overwrite body parameters
			if(queryDecoder == null) {
				queryDecoder = new QueryStringDecoder(getUri());
			}
			for(Entry<String, List<String>> entry : queryDecoder.getParameters().entrySet()) {
				List<String> vals = entry.getValue();
				if(vals != null && !vals.isEmpty()) {
					parameters.put(entry.getKey(), vals.get(0));
				}
			}
		}
		
		return parameters;
	}
	
	/**
	 * Get the path without the query string
	 * @return
	 */
	public String getPath() {
		if(queryDecoder == null) {
			queryDecoder = new QueryStringDecoder(getUri());
		}
		return queryDecoder.getPath();
	}

	public int getPort() {
		return port;
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public int getRemotePort() {
		return remotePort;
	}
	
	public boolean hasParameters() {
		return !getParameters().isEmpty();
	}

	public boolean isHome() {
		return "/".equals(getPath());
	}
	
	private void loadCookies() {
		cookies = new HashMap<String, Cookie>();
		for(String header : getHeaders(HttpHeaders.Names.COOKIE)) {
			for(Cookie cookie : new CookieDecoder().decode(header)) {
				cookies.put(cookie.getName(), cookie);
			}
		}
	}
	
	public void setCookie(Cookie cookie) {
		if(cookies == null) {
			loadCookies();
		}
		cookies.put(cookie.getName(), cookie);
	}
	
	public void setCookie(String name, String value) {
	}
	
	public void setHandler(HttpRequestHandler handler) {
		this.handler = handler;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
}
