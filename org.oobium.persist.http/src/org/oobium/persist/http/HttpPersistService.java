package org.oobium.persist.http;

import static org.oobium.app.http.Action.create;
import static org.oobium.app.http.Action.destroy;
import static org.oobium.app.http.Action.show;
import static org.oobium.app.http.Action.showAll;
import static org.oobium.app.http.Action.update;
import static org.oobium.app.http.MimeType.JSON;
import static org.oobium.persist.SessionCache.*;
import static org.oobium.persist.http.PathBuilder.path;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;
import static org.oobium.utils.json.JsonUtils.toJson;
import static org.oobium.utils.json.JsonUtils.toList;
import static org.oobium.utils.json.JsonUtils.toMap;
import static org.oobium.utils.json.JsonUtils.toObject;
import static org.oobium.utils.literal.Map;
import static org.oobium.utils.literal.e;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.oobium.persist.PersistService;
import org.oobium.persist.RemotePersistService;
import org.oobium.persist.ServiceInfo;
import org.oobium.persist.http.HttpApiService.Route;

public class HttpPersistService extends RemotePersistService implements PersistService {

	private final HttpApiService api;
	private Websocket socket;
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

	public synchronized void addSocketListener() {
		if(socket != null) {
			return; // don't add twice, but do allow calling twice
		}
		
		String url = api.getModelNotificationUrl();
		if(url == null) {
			throw new IllegalArgumentException("no published model notification route found");
		}
		
		socketListener = new WebsocketListener() {
			@Override
			public void onConnect(Websocket websocket) {
				// TODO log
			}
			@Override
			public void onDisconnect(Websocket websocket) {
				// TODO log
			}
			@Override
			public void onError(Websocket websocket, Throwable t) {
				// TODO log
			}
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
		};
		socket = Websockets.connect(url, socketListener);
	}
	
	@Override
	public void closeSession() {
		expireCache();
	}
	
	@Override
	public int count(Class<? extends Model> clazz) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	@Override
	public int count(Class<? extends Model> clazz, Map<String, Object> query, Object... values) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	@Override
	public int count(Class<? extends Model> clazz, String query, Object... values) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	private void create(Model model) throws Exception {
		if(model == null) {
			throw new Exception("cannot create null model");
		}
		
		Route request = api.getRoute(model, create);
		if(request == null) {
			throw new Exception("no published route found for " + model.getClass() + ": create");
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
				throw new Exception(response.getException().getLocalizedMessage());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}

	@Override
	public void create(Model... models) throws Exception {
		for(Model model : models) {
			create(model);
		}
	}
	
	private void destroy(Model model) throws Exception {
		if(model == null) {
			throw new Exception("cannot destroy null model");
		}
		
		Route request = api.getRoute(model, destroy);
		if(request == null) {
			throw new Exception("no published route found for " + model.getClass() + ": destroy");
		}
		
		Model cache = getCacheById(model.getClass(), model.getId());

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
				throw new Exception(response.getException().getLocalizedMessage());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public void destroy(Model... models) throws Exception {
		for(Model model : models) {
			destroy(model);
		}
	}
	
	private String fieldKey(String model, String field) {
		StringBuilder sb = new StringBuilder(model.length() + field.length() + 5);
		return sb.append(model).append('[').append(field).append(']').toString();
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
		if(clazz == null) {
			throw new IllegalArgumentException("cannot find: null class");
		}

		query.put("$limit", 1);

		List<T> models = findAll(clazz, query, values);
		if(models.isEmpty()) {
			return null;
		}
		return models.get(0);
	}
	
	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id) throws Exception {
		if(clazz == null) {
			throw new Exception("cannot find null class with id: " + id);
		}
		
		return findById(clazz, id, null);
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id, String include) throws Exception {
		T model = getCacheById(clazz, id);
		if(model != null) {
			return model;
		}
		
		Route request = api.getRoute(clazz, show);
		if(request == null) {
			throw new Exception("no published route found for " + clazz + ": show");
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
					throw new Exception(response.getException().getLocalizedMessage());
				}
				return null;
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, String query, Object... values) throws Exception {
		return find(clazz, toMap(query), values);
	}
	
	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws Exception {
		return findAll(clazz, (Map<String, Object>) null);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
		if(clazz == null) {
			throw new IllegalArgumentException("cannot findAll: null class");
		}

		Map<String, Object> map = Map( e("query", query), e("values", values) );
		String queryString = toJson(map);
		
		List<T> models = getCacheByQuery(clazz, queryString);
		if(models != null) {
			return models;
		}

		Route request = api.getRoute(clazz, showAll);
		if(request == null) {
			throw new Exception("no published route found for " + clazz + ": showAll");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);

			String path = path(request.path, clazz);
			
			ClientResponse response;
			if(query == null) {
				response = client.request(request.method, path);
			} else {
				response = client.request(request.method, path, map);
			}
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
					throw new Exception(response.getException().getLocalizedMessage());
				}
				throw new Exception("could not retrieve data from the server\nstatus: " + response.getStatus() + "\ncontent: " + response.getBody());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String query, Object... values) throws Exception {
		return findAll(clazz, toMap(query), values);
	}
	
	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public String getMigrationService() {
				return null;
			}
			@Override
			public String getName() {
				return getClass().getSimpleName();
			}
			@Override
			public String getProvider() {
				return "oobium.org";
			}
			@Override
			public String getSymbolicName() {
				return getClass().getName();
			}
			@Override
			public String getVersion() {
				return "0.6.0";
			}
		};
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

	public synchronized void removeSocketListener() {
		if(socket != null) {
			socket.disconnect();
			socket = null;
			socketListener = null;
		}
	}
	
	// always run the query (this is a reload request), but update the cache with the result
	private void retrieve(Model model) throws Exception{
		if(model == null) {
			throw new Exception("cannot retrieve null model");
		}
		
		Route request = api.getRoute(model, show);
		if(request == null) {
			throw new Exception("no published route found for " + model.getClass() + ": show");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);
			
			String path = path(request.path, model);
			
			ClientResponse response = client.request(request.method, path);
			if(response.isSuccess()) {
				model.putAll(response.getBody());
				Model cache = getCacheById(model.getClass(), model.getId());
				if(cache == null) {
					setCache(model);
				} else if(cache != model) {
					cache.putAll(model);
					model.putAll(cache);
				}
			} else if(response.exceptionThrown()) {
				throw new Exception(response.getException().getLocalizedMessage());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public void retrieve(Model... models) throws Exception {
		for(Model model : models) {
			retrieve(model);
		}
	}

	@Override
	public void retrieve(Model model, String field) throws Exception {
		if(model == null) {
			throw new Exception("cannot retrieve null model:" + field);
		}
		
		Route request = api.getRoute(model, showAll, field);
		if(request == null) {
			throw new Exception("no published route found for " + model.getClass() + ": showAll:" + field);
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
						Model cache = getCacheById(type, m.getId());
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
				throw new Exception(response.getException().getLocalizedMessage());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}

	public void setDiscoveryUrl(String url) {
		api.setDiscoveryUrl(url);
	}
	
	private void update(Model model) throws Exception {
		if(model == null) {
			throw new Exception("cannot update null model");
		}
		
		Route request = api.getRoute(model, update);
		if(request == null) {
			throw new Exception("no published route found for " + model.getClass() + ": update");
		}
		
		try {
			Client client = Client.client(request.url);
			client.setAccepts(JSON.acceptsType);
			
			String path = path(request.path, model);
			Map<String, String> params = getParams(model);
			
			ClientResponse response = client.request(request.method, path, params);
			if(response.isSuccess()) {
				Model cache = getCacheById(model.getClass(), model.getId());
				if(cache == null) {
					setCache(model);
				} else if(cache != model) {
					cache.putAll(model);
					model.putAll(cache);
				}
			} else if(response.exceptionThrown()) {
				throw new Exception(response.getException().getLocalizedMessage());
			}
		} catch(MalformedURLException e) {
			throw new IllegalStateException("malformed URL should have been caught earlier!");
		}
	}
	
	@Override
	public void update(Model... models) throws Exception {
		for(Model model : models) {
			update(model);
		}
	}

}
