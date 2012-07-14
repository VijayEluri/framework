package org.oobium.persist.db.derby.embedded;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.oobium.persist.db.Database;
import org.oobium.utils.FileUtils;

public class DerbyEmbeddedDatabase extends Database {

	private final DerbyEmbeddedPersistService service;
	private boolean memoryDbCreated;
	
	public DerbyEmbeddedDatabase(DerbyEmbeddedPersistService service, String client, Map<String, Object> properties) {
		super(client, properties);
		this.service = service;
	}

	@Override
	protected Map<String, Object> initProperties(Map<String, Object> properties) {
		Map<String, Object> props = new HashMap<String, Object>(properties);

		if(coerce(props.get("memory")).from(false)) {
			if(props.get("database") == null) {
				props.put("database", client);
			}
			if(coerce(props.get("backup")).from(false)) {
				// only valid for an in-memory DB (needs another name...)
				String location = getCanonicalPath(null);
				props.put("location", location);
				logger.debug("backup location: {}", location);
			}
		} else {
			props.put("database", getCanonicalPath(properties.get("database")));
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
	protected ConnectionPoolDataSource createDataSource() {
		EmbeddedConnectionPoolDataSource ds = new EmbeddedConnectionPoolDataSource();
		String databaseName;
		if(inMemory()) {
			StringBuilder sb = new StringBuilder();
			sb.append("memory:").append(properties.get("database"));
			if(wantsBackup()) {
				File file = new File((String) properties.get("location"));
				if(file.exists()) {
					Object location = properties.get("location");
					logger.debug("restoring database from: {}", location);
					sb.append(";createFrom=").append(location).append(';');
					memoryDbCreated = true;
				}
			}
			databaseName = sb.toString();
		} else {
			databaseName = (String) properties.get("database");
		}
		ds.setDatabaseName(databaseName);
		ds.setUser((String) properties.get("username"));
		ds.setPassword((String) properties.get("password"));
		return ds;
	}

	@Override
	protected void createDatabase() throws SQLException {
        try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch(ClassNotFoundException e) {
			logger.error(e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:derby:");
		if(inMemory()) {
			memoryDbCreated = true;
			sb.append("memory:");
			sb.append(properties.get("database"));
			if(wantsBackup()) {
				File file = new File((String) properties.get("location"));
				if(file.exists()) {
					Object location = properties.get("location");
					logger.debug("restoring database from: {}", location);
					sb.append(";createFrom=").append(location).append(';');
				} else {
					sb.append(";create=true;");
				}
			} else {
				sb.append(";create=true;");
			}
		} else {
			sb.append(properties.get("database"));
			sb.append(";create=true;");
		}
		sb.append("user=\"").append(properties.get("username")).append("\";password=\"").append(properties.get("password")).append('"');

		String dbURL = sb.toString();
		logger.debug("create connection: {}", dbURL);
		
    	Connection connection = DriverManager.getConnection(dbURL);
        Statement s = connection.createStatement();
        try {
        	String sql = "SET SCHEMA APP";
        	logger.debug(sql);
	        s.executeUpdate(sql);
        	sql = 
	        	"CREATE PROCEDURE APP.CHECK_UNIQUE(tableName VARCHAR(128), columnName VARCHAR(128), id INTEGER) " +
	        	"PARAMETER STYLE JAVA " +
	        	"READS SQL DATA " +
	        	"LANGUAGE JAVA " +
	        	"EXTERNAL NAME '" + UniqueColumnTrigger.class.getCanonicalName() + ".checkUnique'";
        	logger.debug(sql);
	        s.executeUpdate(sql);
	        
	        if(service.hasContext()) {
		        try {
			        File jar = service.getJar(UniqueColumnTrigger.class);
			        sql = "CALL SQLJ.install_jar('" + jar.getCanonicalPath() + "', 'APP." + UniqueColumnTrigger.class.getSimpleName() + "', 0)";
		        	logger.debug(sql);
			        s.executeUpdate(sql);
	
			        sql = "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.database.classpath', 'APP." + UniqueColumnTrigger.class.getSimpleName() + "')";
		        	logger.debug(sql);
			        s.executeUpdate(sql);
		        } catch(Exception e) {
		        	logger.error(e);
		        }
	        }
        } finally {
        	try {
        		s.close();
        	} catch(SQLException e) {
        		// discard
        	}
        }
	}

	@Override
	protected void dropDatabase() throws SQLException {
		memoryDbCreated = false;
		if(inMemory()) {
			if(wantsBackup()) {
				File file = new File((String) properties.get("location"));
				if(file.exists()) {
					logger.debug("removing backup at: {}", file); // TODO not really a backup...
					FileUtils.delete(file);
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append("jdbc:derby:memory:");
			sb.append(properties.get("database"));
			sb.append(";drop=true;");
			sb.append("user=\"").append(properties.get("username")).append("\";password=\"").append(properties.get("password")).append('"');

			String dbURL = sb.toString();
			logger.debug("drop database: {}", dbURL);

			try {
				DriverManager.getConnection(dbURL);
			} catch(SQLException e) {
				if(e.getSQLState().equals("08006")) { // success
					return;
				}
				if(e.getSQLState().equals("XJ004")) { // database not found (nothing to drop)
					return;
				}
				throw e;
			}
		} else {
			File db = new File(File.separator + properties.get("database"));
			logger.debug("drop database: {}", db);
			dispose();
			if(db.exists()) {
				FileUtils.delete(db);
			}
		}
	}

	@Override
	protected String getDatabaseIdentifier() {
		EmbeddedConnectionPoolDataSource ds = (EmbeddedConnectionPoolDataSource) getDataSource();
		if(ds != null) {
			return ds.getDatabaseName();
		} else {
			return "";
		}
	}

	@Override
	protected void preRemove() {
		if(memoryDbCreated && wantsBackup()) {
			File location = new File((String) properties.get("location"));
			logger.debug("backing up database to: {}", location);
			Connection connection = null;
			CallableStatement statement = null;
			try {
				connection = getConnection();
				statement = connection.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
				statement.setString(1, location.getParent());
				statement.execute();
				logger.debug("database backed up");
			} catch(SQLException e) {
				logger.warn("failed to backup database: {}", e, e.getLocalizedMessage());
			} finally {
				if(statement != null) {
					try {
						statement.close();
					} catch(SQLException e) { /* discard */ }
				}
				if(connection != null) {
					try {
						connection.close();
					} catch(SQLException e) { /* discard */ }
				}
			}
		}
	}
	
	public boolean inMemory() {
		return coerce(properties.get("memory")).from(false);
	}
	
	private String getCanonicalPath(Object o) {
		if(!(o instanceof String)) {
			o = ".." + File.separator + "data" + File.separator + client;
		}
		File file = new File((String) o);
		if(!file.isAbsolute()) {
			file = new File(System.getProperty("user.dir"), (String) o);
		}
		try {
			return file.getCanonicalPath();
		} catch(IOException e) {
			logger.error(e);
		}
		return null;
	}

	private boolean wantsBackup() {
		return coerce(properties.get("backup")).from(false) && (properties.get("location") != null);
	}
	
}
