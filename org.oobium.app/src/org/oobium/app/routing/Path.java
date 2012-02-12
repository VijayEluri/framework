package org.oobium.app.routing;

import static org.oobium.utils.StringUtils.attrsEncode;

import java.util.LinkedHashMap;
import java.util.Map;

import org.oobium.app.server.ServerConfig;

public class Path {

	private ServerConfig server;
	private Boolean secure;
	private String protocol;
	private String host;
	private Integer port;
	private String path;
	private Map<String, String> parameters;
	private String anchor;
	
	public Path(String path) {
		this.path = path;
	}

	public String anchor() {
		return anchor;
	}
	
	public Path anchor(String anchor) {
		this.anchor = anchor;
		return this;
	}
	
	public String host() {
		return (host != null) ? host : ((server != null) ? server.host() : null);
	}

	public Path host(String host) {
		this.host = host;
		return this;
	}
	
	public Path include(String include) {
		param("query", "$include:" + include);
		return this;
	}

	public Path param(String key, Object value) {
		if(parameters == null) {
			parameters = new LinkedHashMap<String, String>();
		}
		parameters.put(key, String.valueOf(value));
		return this;
	}

	public String path() {
		return path;
	}

	public Path path(String path) {
		this.path = path;
		return this;
	}
	
	public int port() {
		if(port != null) {
			return port;
		}
		if(server != null) {
			return server.port(secure());
		}
		return secure() ? 24 : 80;
	}
	
	public Path port(Integer port) {
		this.port = port;
		return this;
	}

	public String protocol() {
		return (protocol != null) ? protocol : (secure() ? "https" : "http");
	}
	
	public boolean secure() {
		return (secure != null) ? secure : ((server != null) ? (!server.hasPorts(false)) : false);
	}
	
	public Path secure(Boolean secure) {
		this.secure = secure;
		return this;
	}
	
	public Path server(ServerConfig server) {
		this.server = server;
		return this;
	}
	
	@Override
	public String toString() {
		String path = path();
		String host = host();
		StringBuilder sb = new StringBuilder();
		if(host != null && host.length() > 0) {
			sb.append(protocol()).append("://").append(host);
			int port = port();
			if(port > 0 && port != 80) {
				sb.append(':').append(port);
			}
			if(path != null && path.length() > 0) {
				if(path.charAt(0) != '/') {
					sb.append('/');
				}
				sb.append(path);
				if(parameters != null) {
					sb.append((path.indexOf('?') == -1) ? '?' : '&').append(attrsEncode(parameters));
				}
			}
			else if(parameters != null) {
				sb.append('?').append(attrsEncode(parameters));
			}
		} else {
			if(parameters == null) {
				return path;
			}
			sb.append(path).append(((path.indexOf('?') == -1) ? "?" : "&")).append(attrsEncode(parameters));
		}
		if(anchor != null) {
			sb.append('#').append(anchor);
		}
		return sb.toString();
	}
	
}
