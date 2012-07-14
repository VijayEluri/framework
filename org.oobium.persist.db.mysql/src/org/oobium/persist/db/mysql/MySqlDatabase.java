package org.oobium.persist.db.mysql;

import static org.oobium.utils.StringUtils.blank;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.oobium.persist.db.Database;

import com.mysql.jdbc.ConnectionProperties;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class MySqlDatabase extends Database {

	public MySqlDatabase(String client, Map<String, Object> properties) {
		super(client, properties);
	}

	@Override
	protected Map<String, Object> initProperties(Map<String, Object> properties) {
		Map<String, Object> props = new HashMap<String, Object>(properties);
		if(blank(props.get("database"))) {
			throw new IllegalArgumentException("\"database\" field cannot be blank in persist configuration");
		}
		if(props.get("host") == null) {
			props.put("host", "127.0.0.1");
		}
		if(props.get("port") == null) {
			props.put("port", 3306);
		}
		if(props.get("username") == null) {
			props.put("username", "root");
		}
		if(props.get("password") == null) {
			props.put("password", "");
		}
		if(props.get("engine") == null) {
			props.put("engine", "INNODB");
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
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			StringBuilder sb = new StringBuilder();
			sb.append("jdbc:mysql://").append(properties.get("host")).append(':').append(properties.get("port"));
			if(cmd.startsWith("CREATE ") && properties.containsKey("settings")) {
				
			}

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
		MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
		ds.setDatabaseName(coerce(properties.get("database")).to(String.class));
		ds.setServerName(coerce(properties.get("host")).to(String.class));
		ds.setPortNumber(coerce(properties.get("port")).to(int.class));
		ds.setUser(coerce(properties.get("username")).to(String.class));
		ds.setPassword(coerce(properties.get("password")).to(String.class));
		
		for(Method method : ConnectionProperties.class.getMethods()) {
			String name = method.getName();
			if(name.startsWith("set")) {
				String key = varName(name.substring(3));
				if(properties.containsKey(key)) {
					Object val = properties.get(key);
					try {
						Class<?>[] types = method.getParameterTypes();
						Object[] args = new Object[types.length];
						for(int i = 0; i < types.length && i < args.length; i++) {
							args[i] = coerce(val).to(types[i]);
						}
						method.invoke(ds, args);
					} catch(Exception e) {
						logger.error("could not set property '{}' to value of '{}'", key, val);
					}
				}
			}
		}

		return ds;
	}

	@Override
	protected void dropDatabase() throws SQLException {
		exec("DROP DATABASE " + properties.get("database"));
	}

	@Override
	protected String getDatabaseIdentifier() {
		return ((MysqlConnectionPoolDataSource) getDataSource()).getDatabaseName();
	}

}
