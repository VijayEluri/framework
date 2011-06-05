package org.oobium.persist.http;

import static org.oobium.app.http.MimeType.JSON;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.getResourceAsString;
import static org.oobium.utils.json.JsonUtils.toObject;
import static org.oobium.utils.literal.Map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.oobium.app.http.Action;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;

public class HttpApiService {

	public static class Route {
		HttpMethod method;
		String url;
		String path;
	}

	public static final String DISCOVERY_URL = "org.oobium.persist.http.discovery.url";

	private static final HttpApiService instance = new HttpApiService();

	public static HttpApiService getInstance() {
		return instance;
	}
	
	private String discoveryUrl;
	private String notificationUrl;
	private Map<String, Route> routes;

	private final Logger logger;

	public HttpApiService() {
		this.logger = LogProvider.getLogger(HttpPersistService.class);
	}
	
	private void add(String model, String action, HttpMethod type, String url, String path) {
		Route request = new Route();
		request.method = type;
		request.url = url;
		request.path = path;
		
		if(routes == null) {
			routes = new HashMap<String, Route>();
		}
		routes.put(key(model, action), request);
		
		if(logger.isLoggingDebug()) {
			logger.debug("added request: " + key(model, action) + " -> " + type + " " + url);
		}
	}
	
	private void discover() {
		for(String url : getDiscoveryUrl()) {
			try {
				discover(url);
			} catch(MalformedURLException e) {
				logger.error("bad URL: " + url);
			}
		}
	}

	public String discover(String url) throws MalformedURLException {
		Client client = new Client(url);
		client.setAccepts(JSON.acceptsType);

		URL u = client.getUrl();
		String path = u.getPath();
		
		if(blank(path)) {
			path = getDiscoveryLocation(client);
			if(blank(path)) {
				if(logger.isLoggingDebug()) {
					logger.debug("discovery location not published at " + url);
				}
				return null;
			}
		}
		
		ClientResponse response = client.get(path, Map("type", "models"));
		if(response.isSuccess()) {
			Object r = toObject(response.getBody());
			if(r instanceof Map<?,?>) {
				for(Entry<?,?> e1 : ((Map<?,?>) r).entrySet()) {
					String model = (String) e1.getKey();
					for(Entry<?,?> e2 : ((Map<?,?>) e1.getValue()).entrySet()) {
						String action = (String) e2.getKey();
						Map<?,?> map = (Map<?,?>) e2.getValue();
						load(u, model, action, map);
					}
				}
			}
			return path;
		}

		if(logger.isLoggingDebug()) {
			logger.debug("discovery was not successful at " + getUrl(u) + "/" + path);
		}
		return null;
	}
	
	private String getDiscoveryLocation(Client client) {
		ClientResponse response = client.get();
		if(response.isSuccess()) {
			notificationUrl = client.getUrl() + response.getHeader("API-WS-Location");
			notificationUrl = "ws" + notificationUrl.substring(4);
			return response.getHeader("API-Location");
		}
		return null;
	}

	/**
	 * Get the discovery URLs from the following locations:
	 * <ul>
	 *  <li>System Property: {@value #DISCOVERY_URL}</li>
	 *  <li>Local Resource: "/oobium.server"</li>
	 *  <li>Local Variable: {@link #discoveryUrl}</li>
	 * </ul>
	 * Each location found will be added to the returned array;
	 * each location can also be a comma separated list of locations.
	 * @return the URLs to search for models
	 * @see #setDiscoveryUrl(String)
	 */
	private String[] getDiscoveryUrl() {
		List<String> urls = new ArrayList<String>();
		String s = System.getProperty(DISCOVERY_URL);
		if(s != null) {
			urls.addAll(Arrays.asList(s.split("\\s*,\\s*")));
		}
		s = getResourceAsString(getClass(), "/oobium.server");
		if(s != null) {
			urls.addAll(Arrays.asList(s.split("\\s*,\\s*")));
		}
		s = discoveryUrl;
		if(s != null) {
			urls.addAll(Arrays.asList(s.split("\\s*,\\s*")));
		}
		return urls.toArray(new String[urls.size()]);
	}
	
	public String getModelNotificationUrl() {
		if(notificationUrl == null) {
			discover();
		}
		return notificationUrl;
	}
	
	public Route getRoute(Class<?> clazz, Action action) {
		return getRoute(clazz, action.name());
	}

	private Route getRoute(Class<?> clazz, String action) {
		if(routes == null) {
			discover();
			if(routes != null) {
				return routes.get(key(clazz, action));
			} else {
				return null;
			}
		} else {
			return routes.get(key(clazz, action));
		}
	}

	public Route getRoute(Model model, Action action) {
		return getRoute(model.getClass(), action);
	}
	
	public Route getRoute(Model model, Action action, String hasMany) {
		return getRoute(model.getClass(), action.name() + ":" + hasMany);
	}

	private String getUrl(URL base) {
		return base.getProtocol() + "://" + base.getHost() + ":" + base.getPort();
	}

	private String key(Class<?> clazz, String action) {
		return key(clazz.getName(), action);
	}

	private String key(String model, String action) {
		return model + ":" + action;
	}

	private void load(URL url, String model, String action, Map<?,?> map) {
		Object method = map.get("method");
		Object path = map.get("path");
		if(method instanceof String && path instanceof String) {
			HttpMethod t = null;
			try {
				t = HttpMethod.valueOf((String) method);
			} catch(IllegalArgumentException e) {
				logger.debug("invalid request type: " + method);
				return;
			}
			add((String) model, action, t, getUrl(url), (String) path);
		}
	}

	public void setDiscoveryUrl(String url) {
		this.discoveryUrl = url;
	}
	
}
