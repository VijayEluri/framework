package org.oobium.test.internal;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.PersistException;
import org.oobium.persist.PersistService;
import org.oobium.persist.ServiceInfo;

public class SimplePersistService implements PersistService {
	
	private String session;

	private AtomicInteger id;
	private Map<Class<? extends Model>, LinkedHashMap<Object, Map<String, Object>>> db;
	
	public SimplePersistService() {
		id = new AtomicInteger();
		db = new HashMap<Class<? extends Model>, LinkedHashMap<Object, Map<String, Object>>>();
	}
	
	@Override
	public void closeSession() {
		this.session = null;
	}

	@Override
	public int count(Class<? extends Model> clazz) throws PersistException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int count(Class<? extends Model> clazz, Map<String, Object> query, Object... values) throws PersistException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws PersistException {
		if(where == null) {
			Map<Object, Map<String, Object>> models = db.get(clazz);
			if(models != null) {
				return models.size();
			}
		}
		return 0;
	}

	@Override
	public void create(Model... models) throws PersistException {
		for(Model model : models) {
			if(!model.isNew()) {
				throw new PersistException("cannot create - model has already been created: " + model);
			}
			model.setId(id.incrementAndGet());
			store(model);
		}
	}

	private void destroy(Model model) throws PersistException {
		Object removed = null;
		Map<Object, ?> models = db.get(model.getClass());
		if(models != null) {
			removed = models.remove(model.getId());
		}
		if(removed == null) {
			throw new PersistException("cannot destroy - model not found: " + model);
		}
	}

	@Override
	public void destroy(Model... models) throws PersistException {
		for(Model model : models) {
			if(model.isNew()) {
				throw new PersistException("cannot destroy - model has not been saved yet: " + model);
			}
			destroy(model);
		}
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id) throws PersistException {
		return get(clazz, id);
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, Map<String, Object> query, Object... values) throws PersistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id, String include) throws PersistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws PersistException {
		if("where id=? include:?".equals(where) && values.length == 2) {
			return findById(clazz, (Integer) values[0]);
		}
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.find(any(), anyString(), anyVararg()).thenReturn(...))");
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws PersistException {
		Map<Object, Map<String, Object>> models = db.get(clazz);
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
	public <T extends Model> List<T> findAll(Class<T> clazz, Map<String, Object> query, Object... values) throws PersistException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws PersistException {
		throw new UnsupportedOperationException("Stub this method to use: when(persistor.findAll(anyClass(), anyString(), anyVararg()).thenReturn(...))");
	}

	private <T> T get(Class<T> modelClass, Object id) {
		Map<Object, Map<String, Object>> models = db.get(modelClass);
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
			public String getMigrationService() {
				return null;
			}
			@Override
			public String getName() {
				return "Persist service for simple testing purposes";
			}
			@Override
			public String getProvider() {
				return "Oobium.org";
			}
			@Override
			public String getSymbolicName() {
				return "TestPersistor";
			}
			@Override
			public String getVersion() {
				return "1.0.0";
			}
		};
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

	@Override
	public boolean isSessionOpen() {
		return session != null;
	}

	@Override
	public void openSession(String name) {
		this.session = name;
	}

	private void retrieve(Model model) throws PersistException {
		Model saved = get(model);
		if(saved == null) {
			throw new PersistException("cannot retrieve - model not found: " + model);
		}
		model.setAll(saved.getAll());
	}

	@Override
	public void retrieve(Model... models) throws PersistException {
		for(Model model : models) {
			if(model.isNew()) {
				throw new PersistException("cannot retrieve - model has not been saved yet: " + model);
			}
			retrieve(model);
		}
	}

	@Override
	public void retrieve(Model model, String hasMany) throws PersistException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	private void store(Model model) {
		LinkedHashMap<Object, Map<String, Object>> models = db.get(model.getClass());
		if(models == null) {
			models = new LinkedHashMap<Object, Map<String, Object>>();
			db.put(model.getClass(), models);
		}
		models.put(model.getId(), getPersistentMap(model));
	}

	private void update(Model model) throws PersistException {
		Model saved = get(model);
		if(saved == null) {
			throw new PersistException("cannot update - model not found: " + model);
		}
		saved.setAll(getPersistentMap(model));
	}

	@Override
	public void update(Model... models) throws PersistException {
		for(Model model : models) {
			if(model.isNew()) {
				throw new PersistException("cannot update - model has not been saved yet: " + model);
			}
			update(model);
		}
	}

}
