package org.oobium.persist.http;

import static org.oobium.app.http.Action.create;
import static org.oobium.app.http.Action.destroy;
import static org.oobium.app.http.Action.show;
import static org.oobium.app.http.Action.showAll;
import static org.oobium.app.http.Action.update;
import static org.oobium.app.http.MimeType.JSON;
import static org.oobium.persist.http.Cache.expireCache;
import static org.oobium.persist.http.Cache.getCache;
import static org.oobium.persist.http.Cache.setCache;
import static org.oobium.persist.http.PathBuilder.path;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.json.JsonUtils.toJson;
import static org.oobium.utils.json.JsonUtils.toList;
import static org.oobium.utils.json.JsonUtils.toObject;
import static org.oobium.utils.literal.Map;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.client.websockets.Websocket;
import org.oobium.client.websockets.WebsocketListener;
import org.oobium.client.websockets.Websockets;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.Observer;
import org.oobium.persist.PersistService;
import org.oobium.persist.RemotePersistService;
import org.oobium.persist.ServiceInfo;
import org.oobium.persist.http.HttpApiService.Route;

public class HttpPersistService extends RemotePersistService implements PersistService {

	private final HttpApiService api;
	private WebsocketListener socketListener;

	public HttpPersistService() {
		this(null, false);
	}

	public HttpPersistService(String discoveryUrl) {
		this(discoveryUrl, false);
	}

	public HttpPersistService(String discoveryUrl, boolean global) {
		this.api = HttpApiService.getInstance();
		if(discoveryUrl != null) {
			setDiscoveryUrl(discoveryUrl);
		}
		if(global) {
			Model.setGlobalPersistService(this);
		}
	}

	private Model getModel(String text) {
		String[] sa = text.split(":", 2);
		if(sa.length == 2) {
			try {
				Class<?> clazz = Class.forName(sa[0]);
				int id = Integer.parseInt(sa[1]);
				return (Model) coerce(id, clazz);
			} catch(Exception e) {
				// TODO log error
			}
		}
		return null;
	}
	
	public void addSocketListener() {
		String url = api.getModelNotificationUrl();
		if(url == null) {
			throw new RuntimeException("no published model notification route found");
		}
		socketListener = new WebsocketListener() {
			@Override
			public void onMessage(Websocket websocket, WebSocketFrame frame) {
				if(frame.isText()) {
					String text = frame.getTextData();
					if(text.startsWith("CREATED ")) {
						// CREATED com.test.ws.models.MyModel:15
						Model model = getModel(text.substring(8));
						if(model != null) {
							notifyCreate(model);
						}
						return;
					}
					if(text.startsWith("UPDATED ")) {
						// UPDATED com.test.ws.models.MyModel:15-field1,field2
						int ix = text.indexOf('-', 8);
						if(ix == -1) {
							Model model = getModel(text.substring(8));
							if(model != null) {
								notifyUpdate(model, new String[0]);
							}
						} else {
							Model model = getModel(text.substring(8, ix));
							if(model != null) {
								notifyUpdate(model, text.substring(ix+1).split(","));
							}
						}
						return;
					}
					if(text.startsWith("DESTROYED ")) {
						// DESTROYED com.test.ws.models.MyModel:15
						String[] sa = text.substring(10).split(":", 2);
						if(sa.length == 2) {
							try {
								int id = Integer.parseInt(sa[1]);
								notifyDestroy(sa[0], id);
							} catch(Exception e) {
								// TODO log error
							}
						}
						return;
					}
				}
			}
			@Override
			public void onError(Websocket websocket, Throwable t) {
				// TODO log
			}
			@Override
			public void onDisconnect(Websocket websocket) {
				// TODO log
			}
			@Override
			public void onConnect(Websocket websocket) {
				// TODO log
			}
		};
		Websockets.connect(url, socketListener);
	}
	
	@Override
	public void closeSession() {
		expireCache();
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
		
		Route request = api.getRoute(model, create);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": create");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);

			String path = path(request.path, model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.request(request.method, path, params);
			if(response.isSuccess()) {
				int id = coerce(response.getHeader("id"), int.class);
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
		
		Route request = api.getRoute(model, destroy);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": destroy");
		}
		
		Model cache = getCache(model.getClass(), model.getId());

		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);
			
			String path = path(request.path, model);
			
			ClientResponse response = client.request(request.method, path);
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
		
		Route request = api.getRoute(clazz, show);
		if(request == null) {
			throw new SQLException("no published route found for " + clazz + ": show");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);

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
			
			ClientResponse response = client.request(request.method, path, params);
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

		Route request = api.getRoute(clazz, showAll);
		if(request == null) {
			throw new SQLException("no published route found for " + clazz + ": showAll");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);

			String path = path(request.path, clazz);
			
			ClientResponse response = client.request(request.method, path, query);
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
	
	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public String getSymbolicName() {
				return getClass().getName();
			}
			@Override
			public String getProvider() {
				return "oobium.org";
			}
			@Override
			public String getVersion() {
				return "0.6.0";
			}
			@Override
			public String getName() {
				return getClass().getSimpleName();
			}
			@Override
			public String getMigrationService() {
				return null;
			}
			@Override
			public boolean isRemote() {
				return true;
			}
		};
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
	
	@Override
	public boolean isSessionOpen() {
		throw new UnsupportedOperationException();
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
		
		Route request = api.getRoute(model, show);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": show");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);
			
			String path = path(request.path, model);
			
			ClientResponse response = client.request(request.method, path);
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
		
		Route request = api.getRoute(model, showAll, field);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": showAll:" + field);
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);
			
			String path = path(request.path, model, field);
			
			ClientResponse response = client.request(request.method, path);
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

	public void setDiscoveryUrl(String url) {
		api.setDiscoveryUrl(url);
	}
	
	private void update(Model model) throws SQLException {
		if(model == null) {
			throw new SQLException("cannot update null model");
		}
		
		Route request = api.getRoute(model, update);
		if(request == null) {
			throw new SQLException("no published route found for " + model.getClass() + ": update");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);
			
			String path = path(request.path, model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.request(request.method, path, params);
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
