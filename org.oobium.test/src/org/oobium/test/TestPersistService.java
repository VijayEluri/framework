package org.oobium.test;

import java.sql.SQLException;
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
	public void commit() throws SQLException {
		service.commit();
	}

	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws SQLException {
		return service.count(clazz, where, values);
	}

	@Override
	public void create(Model... models) throws SQLException {
		service.create(models);
	}

	@Override
	public void destroy(Model... models) throws SQLException {
		service.destroy(models);
	}

	@Override
	public List<Map<String, Object>> executeQuery(String sql, Object... values) throws SQLException {
		return service.executeQuery(sql, values);
	}

	@Override
	public List<List<Object>> executeQueryLists(String sql, Object... values) throws SQLException {
		return service.executeQueryLists(sql, values);
	}

	@Override
	public Object executeQueryValue(String sql, Object... values) throws SQLException {
		return service.executeQueryValue(sql, values);
	}
	
	@Override
	public int executeUpdate(String sql, Object... values) throws SQLException {
		return service.executeUpdate(sql, values);
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
		return service.find(clazz, id);
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws SQLException {
		return service.find(clazz, where, values);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		return service.findAll(clazz);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws SQLException {
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
	public void retrieve(Model... models) throws SQLException {
		service.retrieve(models);
	}

	@Override
	public void retrieve(Model model, String hasMany) throws SQLException {
		service.retrieve(model, hasMany);
	}
	
	@Override
	public void rollback() throws SQLException {
		service.rollback();
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		service.setAutoCommit(autoCommit);
	}

	@Override
	public void update(Model... models) throws SQLException {
		service.update(models);
	}

}
