package org.oobium.persist.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;

public abstract class Database {

	protected final Logger logger;
	protected final String client;
	protected final Map<String, Object> properties;

	protected ConnectionPool connectionPool;

	public Database(String client, Map<String, Object> properties) {
		this.logger = LogProvider.getLogger(DbPersistService.class);
		this.client = client;
		this.properties = initProperties(properties);
	}
	
	private void checkConnectionPool() {
		if(connectionPool == null || connectionPool.isDisposed()) {
			connectionPool = new ConnectionPool(client, properties, createDataSource(), logger);
		}
	}

	protected abstract void createDatabase() throws SQLException;
	
	protected abstract ConnectionPoolDataSource createDataSource();
	
	public void dispose() {
		if(connectionPool != null) {
			connectionPool.dispose();
			connectionPool = null;
		}
	}

	protected abstract void dropDatabase() throws SQLException;
	
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.getClass() == getClass()) {
			return ((Database) obj).getDatabaseIdentifier().equals(getDatabaseIdentifier());
		}
		return false;
	}
	
	public Object get(String property) {
		return properties.get(property);
	}
	
	public Connection getConnection() throws SQLException {
		checkConnectionPool();
		return connectionPool.getConnection();
	}
	
	protected abstract String getDatabaseIdentifier();

	protected ConnectionPoolDataSource getDataSource() {
		checkConnectionPool();
		return connectionPool.getDataSource();
	}

	@Override
	public int hashCode() {
		return getDatabaseIdentifier().hashCode();
	}

	protected abstract Map<String, Object> initProperties(Map<String, Object> properties);

}
