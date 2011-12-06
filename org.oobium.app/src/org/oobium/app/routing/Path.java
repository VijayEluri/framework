package org.oobium.app.routing;

import static org.oobium.utils.StringUtils.attrsEncode;

import java.util.LinkedHashMap;
import java.util.Map;

public class Path {

	private final String path;
	private String host;
	private int port;
	private Map<String, String> parameters;
	
	public Path(String path) {
		this.path = path;
	}
	
	public Path add(String key, Object value) {
		if(parameters == null) {
			parameters = new LinkedHashMap<String, String>();
		}
		parameters.put(key, String.valueOf(value));
		return this;
	}

	public String host() {
		return host;
	}

	public Path host(String host) {
		this.host = host;
		return this;
	}
	
	public Path include(String include) {
		add("query", "$include:" + include);
		return this;
	}

	public String path() {
		return path;
	}

	public int port() {
		return port;
	}
	
	public Path port(int port) {
		this.port = port;
		return this;
	}
	
	@Override
	public String toString() {
		if(host != null && host.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("http://").append(host);
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
			return sb.toString();
		} else {
			if(parameters == null) {
				return path;
			}
			return path + ((path.indexOf('?') == -1) ? "?" : "&") + attrsEncode(parameters);
		}
	}
	
}
