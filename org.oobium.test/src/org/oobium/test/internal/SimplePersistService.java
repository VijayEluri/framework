package org.oobium.test.internal;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.PersistService;
import org.oobium.persist.ServiceInfo;

public class SimplePersistService implements PersistService {
	
	private String session;

	private AtomicInteger id;
	private Map<Class<? extends Model>, LinkedHashMap<Integer, Map<String, Object>>> db;
	
	public SimplePersistService() {
		id = new AtomicInteger();
		db = new HashMap<Class<? extends Model>, LinkedHashMap<Integer, Map<String, Object>>>();
	}
	
	@Override
	public void closeSession() {
		this.session = null;
	}

	@Override
	public void commit() throws SQLException {
		// nothing to do
	}

	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws SQLException {
		if(where == null) {
			Map<Integer, Map<String, Object>> models = db.get(clazz);
			if(models != null) {
				return models.size();
			}
		}
		return 0;
	}

	private Map<String, Object> getPersistentMap(Model model) {
		Map<String, Object> map = model.getAll();
		Map<String, Object> pmap = new HashMap<String, Object>();
		ModelAdapter adapter = ModelAdapter.getAdapter(model);
		for(String field : adapter.getFields()) {
			if(!adapter.isVirtual(field) && map.containsKey(field)) {
				pmap.put(field, map.get(field));
			}
		}
		return pmap;
	}
	
	private void store(Model model) {
		LinkedHashMap<Integer, Map<String, Object>> models = db.get(model.getClass());
		if(models == null) {
			models = new LinkedHashMap<Integer, Map<String, Object>>();
			db.put(model.getClass(), models);
		}
		models.put(model.getId(), getPersistentMap(model));
	}

	@Override
	public void create(Model... models) throws SQLException {
		for(Model model : models) {
			if(!model.isNew()) {
				throw new SQLException("cannot create - model has already been created: " + model);
			}
			model.setId(id.incrementAndGet());
			store(model);
		}
	}

	private void destroy(Model model) throws SQLException {
		Object removed = null;
		Map<Integer, ?> models = db.get(model.getClass());
		if(models != null) {
			removed = models.remove(model.getId());
		}
		if(removed == null) {
			throw new SQLException("cannot destroy - model not found: " + model);
		}
	}

	@Override
	public void destroy(Model... models) throws SQLException {
		for(Model model : models) {
			if(model.isNew()) {
				throw new SQLException("cannot destroy - model has not been saved yet: " + model);
			}
			destroy(model);
		}
	}

	@Override
	public List<Map<String, Object>> executeQuery(String sql, Object... values) throws SQLException {
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.executeQuery(anyString(), anyVararg()).thenReturn(...))");
	}

	@Override
	public List<List<Object>> executeQueryLists(String sql, Object... values) throws SQLException {
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.executeQueryLists(anyString(), anyVararg()).thenReturn(...))");
	}

	@Override
	public Object executeQueryValue(String sql, Object... values) throws SQLException {
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.executeQueryValue(anyString(), anyVararg()).thenReturn(...))");
	}
	
	@Override
	public int executeUpdate(String sql, Object... values) throws SQLException {
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.executeUpdate(anyString(), anyVararg()).thenReturn(...))");
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
		return get(clazz, id);
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws SQLException {
		if("where id=? include:?".equals(where) && values.length == 2) {
			return find(clazz, (Integer) values[0]);
		}
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.find(any(), anyString(), anyVararg()).thenReturn(...))");
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		Map<Integer, Map<String, Object>> models = db.get(clazz);
		if(models != null) {
			List<T> list = new ArrayList<T>();
			for(Map<String, Object> map : models.values()) {
				list.add(coerce(map, clazz));
			}
			return list;
		}
		return new ArrayList<T>(0);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws SQLException {
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.findAll(anyClass(), anyString(), anyVararg()).thenReturn(...))");
	}

	private <T> T get(Class<T> modelClass, int id) {
		Map<Integer, Map<String, Object>> models = db.get(modelClass);
		if(models != null) {
			return coerce(models.get(id), modelClass);
		}
		return null;
	}

	private Model get(Model model) {
		return get(model.getClass(), model.getId());
	}

	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public String getName() {
				return "Persist service for simple testing purposes";
			}
			@Override
			public String getSymbolicName() {
				return "TestPersistor";
			}
			@Override
			public String getProvider() {
				return "Oobium.org";
			}
			@Override
			public String getVersion() {
				return "1.0.0";
			}
			@Override
			public String getMigrationService() {
				return null;
			}
		};
	}

	@Override
	public boolean isSessionOpen() {
		return session != null;
	}

	@Override
	public void openSession(String name) {
		this.session = name;
	}

	private void retrieve(Model model) throws SQLException {
		Model saved = get(model);
		if(saved == null) {
			throw new SQLException("cannot retrieve - model not found: " + model);
		}
		model.setAll(saved.getAll());
	}

	@Override
	public void retrieve(Model... models) throws SQLException {
		for(Model model : models) {
			if(model.isNew()) {
				throw new SQLException("cannot retrieve - model has not been saved yet: " + model);
			}
			retrieve(model);
		}
	}
	
	@Override
	public void retrieve(Model model, String hasMany) throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void rollback() throws SQLException {
		// nothing to do
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		// nothing to do
	}

	private void update(Model model) throws SQLException {
		Model saved = get(model);
		if(saved == null) {
			throw new SQLException("cannot update - model not found: " + model);
		}
		saved.setAll(getPersistentMap(model));
	}

	@Override
	public void update(Model... models) throws SQLException {
		for(Model model : models) {
			if(model.isNew()) {
				throw new SQLException("cannot update - model has not been saved yet: " + model);
			}
			update(model);
		}
	}

}
