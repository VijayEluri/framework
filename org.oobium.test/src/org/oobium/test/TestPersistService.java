package org.oobium.test;

import java.util.List;
import java.util.Map;

import org.oobium.persist.Model;
import org.oobium.persist.PersistService;
import org.oobium.persist.ServiceInfo;
import org.oobium.persist.db.derby.embedded.DerbyEmbeddedPersistService;
import org.oobium.test.internal.SimplePersistService;

public class TestPersistService implements PersistService {

	private static TestPersistService create(PersistService service) {
		TestPersistService persistor = new TestPersistService(service);
		Model.setPersistService(persistor);
		return persistor;
	}
	
	public static TestPersistService useDerby(Class<? extends Model> modelClass) {
		DerbyEmbeddedPersistService service = new DerbyEmbeddedPersistService("testClient", "testDatabase", true);
		return create(service);
	}
	
	public static TestPersistService useSimple() {
		SimplePersistService service = new SimplePersistService();
		return create(service);
	}
	

	private final PersistService service;

	private TestPersistService(PersistService service) {
		this.service = service;
	}
	
	@Override
	public void closeSession() {
		service.closeSession();
	}

	@Override
	public int count(Class<? extends Model> clazz) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int count(Class<? extends Model> clazz, Map<String, Object> query, Object... values) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws Exception {
		return service.count(clazz, where, values);
	}

	@Override
	public void create(Model... models) throws Exception {
		service.create(models);
	}
	
	@Override
	public void destroy(Model... models) throws Exception {
		service.destroy(models);
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id) throws Exception {
		return service.findById(clazz, id);
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id, String include) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws Exception {
		return service.find(clazz, where, values);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws Exception {
		return service.findAll(clazz);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws Exception {
		return service.findAll(clazz, where, values);
	}
	
	@Override
	public ServiceInfo getInfo() {
		return service.getInfo();
	}

	@Override
	public boolean isSessionOpen() {
		return service.isSessionOpen();
	}

	@Override
	public void openSession(String name) {
		service.openSession(name);
	}

	@Override
	public void retrieve(Model... models) throws Exception {
		service.retrieve(models);
	}

	@Override
	public void retrieve(Model model, String hasMany) throws Exception {
		service.retrieve(model, hasMany);
	}

	@Override
	public void update(Model... models) throws Exception {
		service.update(models);
	}

}
