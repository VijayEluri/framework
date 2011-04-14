package org.oobium.persist.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;

public abstract class Database {

	protected final Logger logger;
	protected final String client;
	protected final Map<String, Object> properties;

	private ConnectionPool connectionPool;

	public Database(String client, Map<String, Object> properties) {
		this.logger = LogProvider.getLogger(DbPersistService.class);
		this.client = client;
		this.properties = initProperties(properties);
		this.connectionPool = new ConnectionPool(client, properties, createDataSource(), logger);
	}
	
	protected abstract Map<String, Object> initProperties(Map<String, Object> properties);

	protected abstract void createDatabase() throws SQLException;
	
	protected abstract ConnectionPoolDataSource createDataSource();

	public void dispose() {
		connectionPool.dispose();
	}
	
	protected abstract void dropDatabase() throws SQLException;
	
	protected ConnectionPoolDataSource getDataSource() {
		return connectionPool.getDataSource();
	}
	
	protected abstract String getDatabaseIdentifier();

	public Connection getConnection() throws SQLException {
		return connectionPool.getConnection();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj.getClass() == getClass()) {
			return ((Database) obj).getDatabaseIdentifier().equals(getDatabaseIdentifier());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getDatabaseIdentifier().hashCode();
	}

}
