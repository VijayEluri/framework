package org.oobium.persist.db.postgresql;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.oobium.persist.db.Database;
import org.postgresql.ds.PGConnectionPoolDataSource;

public class PostgreSqlDatabase extends Database {

	public PostgreSqlDatabase(String client, Map<String, Object> properties) {
		super(client, properties);
	}

	@Override
	protected Map<String, Object> initProperties(Map<String, Object> properties) {
		Map<String, Object> props = new HashMap<String, Object>(properties);
		if(props.get("host") == null) {
			props.put("host", "127.0.0.1");
		}
		if(props.get("port") == null) {
			props.put("port", 5432);
		}
		if(props.get("username") == null) {
			props.put("username", "root");
		}
		if(props.get("password") == null) {
			props.put("password", "");
		}
		return props;
	}

	@Override
	protected void createDatabase() throws SQLException {
		exec("CREATE DATABASE " + properties.get("database"));
	}
	
	private void exec(String cmd) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		try {
			Class.forName("org.postgresql.Driver").newInstance();

			StringBuilder sb = new StringBuilder();
			sb.append("jdbc:postgresql://").append(properties.get("host")).append(':').append(properties.get("port"));

			String dbURL = sb.toString();
			if(logger.isLoggingDebug()) {
				logger.debug(cmd + ": " + dbURL);
			}
			
	    	connection = DriverManager.getConnection(dbURL, (String) properties.get("username"), (String) properties.get("password"));
			statement = connection.createStatement();
			statement.execute(cmd);
		} catch(SQLException e) {
			throw e;
		} catch(Exception e) {
			throw new SQLException("could not create database", e);
		} finally {
			if(statement != null) {
				try {
					statement.close();
				} catch(SQLException e) {
					// discard
				}
			}
			if(connection != null) {
				try {
					connection.close();
				} catch(SQLException e) {
					// discard
				}
			}
		}
	}
	
	@Override
	protected ConnectionPoolDataSource createDataSource() {
		PGConnectionPoolDataSource ds = new PGConnectionPoolDataSource();
		ds.setDatabaseName(coerce(properties.get("database"), String.class));
		ds.setPortNumber(coerce(properties.get("port"), int.class));
		ds.setUser(coerce(properties.get("username"), String.class));
		ds.setPassword(coerce(properties.get("password"), String.class));
		return ds;
	}

	@Override
	protected void dropDatabase() throws SQLException {
		exec("DROP DATABASE " + properties.get("database"));
	}

	@Override
	protected String getDatabaseIdentifier() {
		return ((PGConnectionPoolDataSource) getDataSource()).getDatabaseName();
	}

}
