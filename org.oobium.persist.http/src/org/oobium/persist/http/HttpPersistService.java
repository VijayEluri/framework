package org.oobium.persist.http;

import static org.oobium.http.constants.Action.create;
import static org.oobium.http.constants.Action.destroy;
import static org.oobium.http.constants.Action.show;
import static org.oobium.http.constants.Action.showAll;
import static org.oobium.http.constants.Action.update;
import static org.oobium.http.constants.ContentType.JSON;
import static org.oobium.persist.http.Cache.expireCache;
import static org.oobium.persist.http.Cache.getCache;
import static org.oobium.persist.http.Cache.setCache;
import static org.oobium.persist.http.PathBuilder.path;
import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.getResourceAsString;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.json.JsonUtils.toJson;
import static org.oobium.utils.json.JsonUtils.toList;
import static org.oobium.utils.json.JsonUtils.toObject;
import static org.oobium.utils.literal.Map;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
		String path;
	}

	
	public static final String DISCOVERY_URL = "org.oobium.persist.http.discovery.url";
	
	
	private final Logger logger;
	private String discoveryUrl;
	private Map<String, Request> requests;

	public HttpPersistService() {
		this.logger = LogProvider.getLogger(HttpPersistService.class);
	}
	
	private void add(String model, String action, RequestType type, String url, String path) {
		Request request = new Request();
		request.type = type;
		request.url = url;
		request.path = path;
		
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
		expireCache();
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
	
	private void create(Model model) throws SQLException {
		if(model == null) {
			throw new SQLException("cannot create null model");
		}
		
		Request request = getRequest(model, create);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": create");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);

			String path = path(request.path, model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.syncRequest(request.type, path, params);
			if(response.isSuccess()) {
				int id = coerce(response.getHeader(Header.ID.key()), int.class);
				model.setId(id);
				setCache(model);
			} else if(response.exceptionThrown()) {
				throw new SQLException(response.getException().getLocalizedMessage());
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
	
	private void destroy(Model model) throws SQLException {
		if(model == null) {
			throw new SQLException("cannot destroy null model");
		}
		
		Request request = getRequest(model, destroy);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": destroy");
		}
		
		Model cache = getCache(model.getClass(), model.getId());

		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(request.path, model);
			
			ClientResponse response = client.syncRequest(request.type, path);
			if(response.isSuccess()) {
				if(cache != null && cache != model) {
					cache.setId(0);
					cache.clear();
				}
			} else if(response.exceptionThrown()) {
				throw new SQLException(response.getException().getLocalizedMessage());
			}
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
			logger.debug("discovery was not successful at " + getUrl(u) + "/" + path);
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
			throw new SQLException("cannot find null class with id: " + id);
		}
		
		return find(clazz, id, null);
	}
	
	private <T extends Model> T find(Class<T> clazz, int id, String include) throws SQLException {
		T model = getCache(clazz, id);
		if(model != null) {
			return model;
		}
		
		Request request = getRequest(clazz, show);
		if(request == null) {
			throw new SQLException("no published route found for " + clazz + ": show");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);

			model = coerce(id, clazz);
			String path = path(request.path, model);

			Map<String, ?> params = null;
			if(include != null) {
				if(include.startsWith("include")) {
					int ix = include.indexOf(':');
					if(ix != -1) {
						include = include.substring(ix+1);
						params = Map("include", include);
					}
				} else {
					params = Map("include", include);
				}
			}
			
			ClientResponse response = client.syncRequest(request.type, path, params);
			if(response.isSuccess()) {
				model.putAll(response.getBody());
				setCache(model);
				return model;
			} else {
				if(response.exceptionThrown()) {
					throw new SQLException(response.getException().getLocalizedMessage());
				}
				return null;
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws SQLException {
		if(clazz == null) {
			throw new IllegalArgumentException("cannot find: null class");
		}

		if(where == null) {
			where = "limit 1";
		} else {
			int ix = where.indexOf("include");
			if(ix == -1) {
				if(where.equals("where id=?") && values.length == 1) {
					return find(clazz, coerce(values[0], int.class));
				}
				where = where + " limit 1";
			} else if(ix == 0) {
				where = "limit 1 " + where;
			} else if(where.startsWith("where id=? include")) {
				return find(clazz, coerce(values[0], int.class), where.substring(ix));
			} else {
				StringBuilder sb = new StringBuilder(where);
				sb.insert(ix, "limit 1 ");
				where = sb.toString();
			}
		}
		
		List<T> models = findAll(clazz, where, values);
		if(models.isEmpty()) {
			return null;
		}
		return models.get(0);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		if(clazz == null) {
			throw new IllegalArgumentException("cannot findAll: null class");
		}

		return findAll(clazz, "findAll", (Map<String,?>) null);
	}

	private <T extends Model> List<T> findAll(Class<T> clazz, String queryString, Map<String, ?> query) throws SQLException {
		List<T> models = getCache(clazz, queryString);
		if(models != null) {
			return models;
		}

		Request request = getRequest(clazz, showAll);
		if(request == null) {
			throw new SQLException("no published route found for " + clazz + ": showAll");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);

			String path = path(request.path, clazz);
			
			ClientResponse response = client.syncRequest(request.type, path, query);
			if(response.isSuccess()) {
				List<Object> list = toList(response.getBody());
				models = new ArrayList<T>();
				for(Object o : list) {
					T model = coerce(o, clazz);
					models.add(model);
				}
				setCache(clazz, queryString, models);
				return models;
			} else {
				if(response.exceptionThrown()) {
					throw new SQLException(response.getException().getLocalizedMessage());
				}
				throw new SQLException("could not retrieve data from the server\nstatus: " + response.getStatus() + "\ncontent: " + response.getBody());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws SQLException {
		if(clazz == null) {
			throw new IllegalArgumentException("cannot findAll: null class, where: " + where);
		}

		if(where.startsWith("where ")) {
			where = where.substring(6);
		}
		
		String include;
		int ix = where.indexOf("include");
		if(ix == -1) {
			include = null;
		} else {
			include = where.substring(ix).trim();
			where = where.substring(0, ix).trim();
			if(where.length() == 0) {
				where = null;
			}
			ix = include.indexOf(':');
			if(ix == -1) {
				include = null;
			} else {
				include = include.substring(ix+1);
			}
		}

		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		if(where != null) {
			map.put("where", where);
		}
		if(include != null) {
			map.put("include", include);
		}
		map.put("values", values);
		query.put("query", map);

		String queryString = toJson(query);
		
		return findAll(clazz, queryString, query);
	}
	
	private String getDiscoveryLocation(Client client) {
		ClientResponse response = client.get();
		if(response.isSuccess()) {
			return response.getHeader(Header.API_LOCATION.key());
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
	
	private String getUrl(URL base) {
		return base.getProtocol() + "://" + base.getHost() + ":" + base.getPort();
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
			add((String) model, action, t, getUrl(url), (String) path);
		}
	}

	@Override
	public void openSession(String name) {
		expireCache();
	}

	// always run the query (this is a reload request), but update the cache with the result
	private void retrieve(Model model) throws SQLException{
		if(model == null) {
			throw new SQLException("cannot retrieve null model");
		}
		
		Request request = getRequest(model, show);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": show");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(request.path, model);
			
			ClientResponse response = client.syncRequest(request.type, path);
			if(response.isSuccess()) {
				model.putAll(response.getBody());
				Model cache = getCache(model.getClass(), model.getId());
				if(cache == null) {
					setCache(model);
				} else if(cache != model) {
					cache.putAll(model);
					model.putAll(cache);
				}
			} else if(response.exceptionThrown()) {
				throw new SQLException(response.getException().getLocalizedMessage());
			}
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
	public void retrieve(Model model, String field) throws SQLException {
		if(model == null) {
			throw new SQLException("cannot retrieve null model:" + field);
		}
		
		Request request = getRequest(model, showAll, field);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": showAll:" + field);
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(request.path, model, field);
			
			ClientResponse response = client.syncRequest(request.type, path);
			if(response.isSuccess()) {
				ModelAdapter adapter = ModelAdapter.getAdapter(model);
				Class<? extends Model> type = adapter.getHasManyMemberClass(field);
				
				Object o = toObject(response.getBody());
				if(o instanceof List) {
					List<Object> list = new ArrayList<Object>();
					for(Object e : (List<?>) o) {
						Model m = coerce(e, type);
						Model cache = getCache(type, m.getId());
						if(cache == null) {
							setCache(m);
						} else {
							cache.putAll(model);
							model.putAll(cache);
						}
						list.add(m);
					}
					model.put(field, list);
				}
			} else if(response.exceptionThrown()) {
				throw new SQLException(response.getException().getLocalizedMessage());
			}
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

	public void setDiscoveryUrl(String url) {
		this.discoveryUrl = url;
	}
	
	private void update(Model model) throws SQLException {
		if(model == null) {
			throw new SQLException("cannot update null model");
		}
		
		Request request = getRequest(model, update);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": update");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON);
			
			String path = path(request.path, model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.syncRequest(request.type, path, params);
			if(response.isSuccess()) {
				Model cache = getCache(model.getClass(), model.getId());
				if(cache == null) {
					setCache(model);
				} else if(cache != model) {
					cache.putAll(model);
					model.putAll(cache);
				}
			} else if(response.exceptionThrown()) {
				throw new SQLException(response.getException().getLocalizedMessage());
			}
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
