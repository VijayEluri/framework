package org.oobium.persist.migrate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.oobium.persist.PersistService;
import org.oobium.persist.migrate.defs.Table;

public interface MigrationService {
	
	public static final String SERVICE = "service";
	
	
	public abstract void create(Table table) throws SQLException;

	public abstract void drop(Table table) throws SQLException;

	public abstract void update(Table table) throws SQLException;

	
	public abstract List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException;

	public abstract List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException;

	public abstract Object executeQueryValue(String sql, Object...values) throws SQLException;

	public abstract int executeUpdate(String sql, Object...values) throws SQLException;
	
	
	public abstract void dropAll();
	
	public abstract void dropDatabase();
	
	public abstract void initializeDatabase(Map<String, ? extends Object> options)  throws SQLException;
	
	public abstract Table find(String table);
	
	public abstract List<Table> findAll();
	
	public abstract int getCurrentRevision();

	public abstract void setPersistService(PersistService persistor);
	
}