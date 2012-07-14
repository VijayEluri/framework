package org.oobium.app.server;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ServerConfig {

	public static final String ANY_HOST = "*";
	
	private static boolean anyHost(String[] hosts) {
		if(hosts == null || hosts.length == 0) {
			return true;
		}

		for(int i = 0; i < hosts.length; i++) {
			if(ANY_HOST.equals(hosts[i])) {
				return true;
			}
		}

		return false;
	}

	
	private final boolean primary;
	private final String name;
	private final String[] hosts;
	private final boolean anyHost;
	private final int[] ports;
	private final int[] securePorts;
	private final Map<?, ?> ssl;
	private final Map<?, ?> options;
	private final String baseDirectory;
	
	public ServerConfig(String host, int port, boolean secure) {
		primary = true;
		this.name = "testServer";
		hosts = new String[] { host };
		anyHost = ANY_HOST.equals(host);
		ports = secure ? new int[0] : new int[] { port };
		securePorts = secure ? new int[] { port } : new int[0];
		ssl = new HashMap<String, Object>(0);
		options = new HashMap<String, Object>(0);
		baseDirectory = "";
	}
	
	public ServerConfig(String name, Object input) {
		baseDirectory = "";
		if(input == null) {
			throw new IllegalArgumentException("server config cannot be null");
		}
		if(input instanceof String) {
			primary = false;
			this.name = (String) input;
			hosts = null;
			anyHost = true;
			ports = null;
			securePorts = null;
			ssl = null;
			options = null;
		}
		else if(input instanceof Map) {
			Map<?,?> inputMap = (Map<?,?>) input;
			
			if(inputMap.containsKey("extend")) {
				primary = false;
				this.name = (String) inputMap.get("extend");
				hosts = coerce(inputMap.get("host")).to(String[].class);
				anyHost = anyHost(hosts);
				ports = null;
				securePorts = null;
				ssl = null;
				options = null;
			} else {
				primary = true;
				this.name = name;
	
				hosts = coerce(inputMap.get("host")).to(String[].class);
				anyHost = anyHost(hosts);
	
				ports = coerce(inputMap.get("port")).to(int[].class);
	
				Map<?,?> sslMap = (Map<?,?>) inputMap.get("ssl");
				if(sslMap == null) {
					ssl = new HashMap<String, Object>(0);
					securePorts = new int[0];
				} else {
					ssl = Collections.unmodifiableMap(sslMap);
					securePorts = coerce(ssl.get("port")).to(int[].class);
				}
				
				Map<?,?> configMap = (Map<?,?>) inputMap.get("options");
				if(configMap == null) {
					options = new HashMap<String, Object>(0);
				} else {
					options = Collections.unmodifiableMap(configMap);
				}
				
				if(ports.length == 0 && securePorts.length == 0) {
					throw new IllegalArgumentException("no port in server config");
				}
			}
		}
		else {
			primary = true;
			this.name = name;
			hosts = new String[0];
			anyHost = true;
			ports = coerce(input).to(int[].class);
			securePorts = new int[0];
			ssl = new HashMap<String, Object>(0);
			options = new HashMap<String, Object>(0);
			
			if(ports.length == 0 && securePorts.length == 0) {
				throw new IllegalArgumentException("no port in server config");
			}
		}
	}

	public File getFile(String path) {
		File file = new File(baseDirectory, path);
		try {
			if(file.getCanonicalPath().startsWith(baseDirectory)) {
				return file;
			}
		} catch(Exception e) {
			// fall through
		}
		throw new IllegalArgumentException("illegal path: " + path);
	}
	
	public boolean hasHost() {
		return hosts.length > 0;
	}
	
	public boolean hasPorts(boolean secure) {
		return secure ? (securePorts.length > 0) : (ports.length > 0);
	}

	public String host() {
		if(hosts.length == 0) {
			throw new IllegalArgumentException("server does not have a host name configured");
		}
		return hosts[0];
	}

	public String[] hosts() {
		return hosts;
	}

	public boolean isPrimary() {
		return primary;
	}
	
	public String name() {
		return name;
	}
	
	public String option(String name, String defaultValue) {
		Object value = options.get(name);
		if(value == null) {
			return defaultValue;
		} else {
			return value.toString();
		}
	}
	
	public Map<?,?> options() {
		return options;
	}
	
	public int port() {
		return (ports.length > 0) ? ports[0] : securePorts[0];
	}
	
	public int port(boolean secure) {
		if(secure) {
			if(securePorts.length == 0) {
				throw new IllegalArgumentException("server is not configured to have secure ports");
			}
			return securePorts[0];
		}
		if(ports.length == 0) {
			throw new IllegalArgumentException("server is not configured to have non-secure ports");
		}
		return ports[0];
	}

	public int[] ports(boolean secure) {
		return secure ? securePorts : ports;
	}

	public boolean servesAnyHost() {
		return anyHost;
	}
	
	public boolean servesHost(String host) {
		if(host != null && host.length() > 0) {
			for(String h : hosts) {
				if(h == ANY_HOST || h.equals(host)) {
					return true;
				}
			}
		}
		return false;
	}

	public Map<?,?> ssl() {
		return ssl;
	}
	
}
