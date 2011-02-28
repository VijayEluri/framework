package org.oobium.persist.http;

import static org.oobium.persist.http.PathBuilder.path;
import static org.oobium.http.constants.ContentType.JSON;
import static org.oobium.http.constants.Action.*;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.getResourceAsString;
import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.literal.*;
import static org.oobium.utils.json.JsonUtils.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.http.constants.Action;
import org.oobium.http.constants.Header;
import org.oobium.http.constants.RequestType;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.PersistService;
import org.oobium.persist.ServiceInfo;

public class HttpPersistService implements PersistService {

	private class Request {
		RequestType type;
		String url;
	}

	
	public static final String DISCOVERY_URL = "org.oobium.persist.http.discovery.url";
	
	
	private final Logger logger;
	private Map<String, Request> requests;
	
	public HttpPersistService() {
		this.logger = LogProvider.getLogger(HttpPersistService.class);
	}

	private void add(String model, String action, RequestType type, String url) {
		Request request = new Request();
		request.type = type;
		request.url = url;
		
		if(requests == null) {
			requests = new HashMap<String, Request>();
		}
		requests.put(key(model, action), request);
		
		if(logger.isLoggingDebug()) {
			logger.debug("added request: " + key(model, action) + " -> " + type + " " + url);
		}
	}
	
	@Override
	public void closeSession() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void commit() throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	private void create(Model model) {
		if(model == null) {
			// throw something?
			return;
		}
		
		Request request = getRequest(model, create);
		if(request == null) {
			// throw something?
			return;
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);

			String path = path(client.getPath(), model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.syncRequest(request.type, path, params);
			if(response.isSuccess()) {
				model.setId(coerce(response.getHeader(Header.ID.key()), int.class));
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public void create(Model... models) throws SQLException {
		for(Model model : models) {
			create(model);
		}
	}

	private void destroy(Model model) {
		if(model == null) {
			// throw something?
			return;
		}
		
		Request request = getRequest(model, destroy);
		if(request == null) {
			// throw something?
			return;
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(client.getPath(), model);
			
			ClientResponse response = client.syncRequest(request.type, path);
			System.out.println(response);
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public void destroy(Model... models) throws SQLException {
		for(Model model : models) {
			destroy(model);
		}
	}
	
	public String discover(String url) throws MalformedURLException {
		Client client = new Client(url);
		client.setAccepts(JSON);

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
			logger.debug("discovery was not successful at " + getUrl(u, path));
		}
		return null;
	}
	
	@Override
	public List<Map<String, Object>> executeQuery(String sql, Object... values) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	@Override
	public List<List<Object>> executeQueryLists(String sql, Object... values) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object executeQueryValue(String sql, Object... values) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	@Override
	public int executeUpdate(String sql, Object... values) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	private String fieldKey(String model, String field) {
		StringBuilder sb = new StringBuilder(model.length() + field.length() + 5);
		return sb.append(model).append('[').append(field).append(']').toString();
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
		if(clazz == null) {
			// throw something?
			return null;
		}
		
		Request request = getRequest(clazz, show);
		if(request == null) {
			// throw something?
			return null;
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);

			T model = coerce(id, clazz);
			String path = path(client.getPath(), model);
			
			ClientResponse response = client.syncRequest(request.type, path);
			if(response.isSuccess()) {
				model.putAll(response.getBody());
				return model;
			} else {
				return null;
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		if(clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null");
		}
		
		Request request = getRequest(clazz, showAll);
		if(request == null) {
			throw new SQLException("no published route found for " + clazz + ": showAll");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);

			String path = path(client.getPath(), clazz);
			
			ClientResponse response = client.syncRequest(request.type, path);
			if(response.isSuccess()) {
				List<Object> list = toList(response.getBody());
				List<T> models = new ArrayList<T>();
				for(Object o : list) {
					models.add(coerce(o, clazz));
				}
				return models;
			} else {
				if(response.exceptionThrown()) {
					throw new SQLException(response.getException());
				}
				throw new SQLException("could not retrieve data from the server\nstatus: " + response.getStatus() + "\ncontent: " + response.getBody());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws SQLException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}

	private String getDiscoveryLocation(Client client) {
		ClientResponse response = client.get();
		if(response.isSuccess()) {
			return response.getHeader(Header.API_LOCATION.key());
		}
		return null;
	}
	
	@Override
	public ServiceInfo getInfo() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	private Map<String, String> getParams(Model model) {
		String name = varName(model.getClass());
		ModelAdapter adapter = ModelAdapter.getAdapter(model);
		Map<String, Object> fields = model.getAll();
		Map<String, String> params = new HashMap<String, String>();
		for(Entry<String, Object> entry : fields.entrySet()) {
			String field = entry.getKey();
			if(adapter.hasField(field) && !adapter.isVirtual(field)) {
				String key = fieldKey(name, field);
				String value = coerce(entry.getValue(), String.class);
				params.put(key, value);
			}
		}
		return params;
	}

	private Request getRequest(Class<?> clazz, Action action) {
		return getRequest(clazz, action.name());
	}
	
	private Request getRequest(Class<?> clazz, String action) {
		if(requests == null) {
			for(String url : getDiscoveryUrl()) {
				try {
					discover(url);
				} catch(MalformedURLException e) {
					logger.error("bad URL: " + url);
				}
			}
			if(requests != null) {
				return requests.get(key(clazz, action));
			} else {
				return null;
			}
		} else {
			return requests.get(key(clazz, action));
		}
	}
	
	private Request getRequest(Model model, Action action) {
		return getRequest(model.getClass(), action);
	}

	private Request getRequest(Model model, Action action, String hasMany) {
		return getRequest(model.getClass(), action.name() + ":" + hasMany);
	}

	private String[] getDiscoveryUrl() {
		String s = getResourceAsString(getClass(), "/oobium.server");
		if(s == null) {
			s = System.getProperty(DISCOVERY_URL);
		}
		if(s != null) {
			return s.split("\\s*,\\s*");
		}
		return new String[0];
	}

	private String getUrl(URL base, Object path) {
		return base.getProtocol() + "://" + base.getHost() + ":" + base.getPort() + path;
	}

	@Override
	public boolean isSessionOpen() {
		throw new UnsupportedOperationException();
	}

	private String key(Class<?> clazz, String action) {
		return key(clazz.getName(), action);
	}

	private String key(String model, String action) {
		return model + ":" + action;
	}

	private void load(URL url, String model, String action, Map<?,?> map) {
//		model = simpleName(model);
		Object method = map.get("method");
		Object path = map.get("path");
		if(method instanceof String && path instanceof String) {
			RequestType t = null;
			try {
				t = RequestType.valueOf((String) method);
			} catch(IllegalArgumentException e) {
				logger.debug("invalid request type: " + method);
				return;
			}
			add((String) model, action, t, getUrl(url, path));
		}
	}

	@Override
	public void openSession(String name) {
		throw new UnsupportedOperationException();
	}
	
	private void retrieve(Model model) {
		if(model == null) {
			// throw something?
			return;
		}
		
		Request request = getRequest(model, show);
		if(request == null) {
			// throw something?
			return;
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(client.getPath(), model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.syncRequest(request.type, path, params);
			if(response.isSuccess()) {
				model.putAll(response.getBody());
			}
			System.out.println(response);
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}

	@Override
	public void retrieve(Model... models) throws SQLException {
		for(Model model : models) {
			retrieve(model);
		}
	}
	
	@Override
	public void retrieve(Model model, String hasMany) throws SQLException {
		if(model == null) {
			// throw something?
			return;
		}
		
		Request request = getRequest(model, show, hasMany);
		if(request == null) {
			// throw something?
			return;
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(client.getPath(), model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.syncRequest(request.type, path, params);
			if(response.isSuccess()) {
				model.put(hasMany, response.getBody());
			}
			System.out.println(response);
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}

	@Override
	public void rollback() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	private void update(Model model) {
		if(model == null) {
			// throw something?
			return;
		}
		
		Request request = getRequest(model, update);
		if(request == null) {
			// throw something?
			return;
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(client.getPath(), model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.syncRequest(request.type, path, params);
			System.out.println(response);
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public void update(Model... models) throws SQLException {
		for(Model model : models) {
			update(model);
		}
	}

}
