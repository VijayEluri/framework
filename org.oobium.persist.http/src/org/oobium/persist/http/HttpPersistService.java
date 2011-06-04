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

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.oobium.client.Client;
import org.oobium.client.ClientResponse;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.PersistService;
import org.oobium.persist.ServiceInfo;
import org.oobium.persist.http.HttpApiService.Request;

public class HttpPersistService implements PersistService {

	private final HttpApiService api;

	public HttpPersistService() {
		this.api = HttpApiService.getInstance();
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
		
		Request request = api.getRequest(model, create);
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
		
		Request request = api.getRequest(model, destroy);
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
		
		Request request = api.getRequest(clazz, show);
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

		Request request = api.getRequest(clazz, showAll);
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
		
		Request request = api.getRequest(model, show);
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
		
		Request request = api.getRequest(model, showAll, field);
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
	
	private void update(Model model) throws SQLException {
		if(model == null) {
			throw new SQLException("cannot update null model");
		}
		
		Request request = api.getRequest(model, update);
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
