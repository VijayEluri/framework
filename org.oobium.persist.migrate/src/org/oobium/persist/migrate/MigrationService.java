package org.oobium.persist.migrate;

import org.oobium.persist.PersistService;

public interface MigrationService {
	
	public static final String SERVICE = "service";
	
	
//	public abstract void create(Table table) throws SQLException;
//
//	public abstract void drop(Table table) throws SQLException;
//
//	public abstract void update(Table table) throws SQLException;

	
//	public abstract List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException;
//
//	public abstract List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException;
//
//	public abstract Object executeQueryValue(String sql, Object...values) throws SQLException;
//
//	public abstract int executeUpdate(String sql, Object...values) throws SQLException;
	
	
	public abstract void createDatastore() throws Exception;
	
	public abstract void dropDatastore() throws Exception;
	
	
//	public abstract Table find(String table);
//	
//	public abstract List<Table> findAll();
//	
//	public abstract int getCurrentRevision();

	
	public abstract void setClient(String client);
	
	public abstract void setPersistService(PersistService persistor);
	
}