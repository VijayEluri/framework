/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.session.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.oobium.http.HttpSession;
import org.oobium.logging.Logger;
import org.oobium.logging.LogProvider;

public class DbSession implements HttpSession {

	private static final Logger logger = LogProvider.getLogger(DbSessionService.class);
	
	private static Connection connection;
	private static boolean runCreate = true;
	private static final String createSql = "INSERT INTO sessions(uuid,data,expiration) VALUES(?,?,?)";
	private static final String updateSql = "UPDATE sessions set data=?, expiration=? WHERE id=?";
	private static final String retrieveSql = "SELECT data, expiration FROM sessions WHERE id=? AND uuid=? AND CURRENT_TIMESTAMP < expiration";
	private static PreparedStatement retrieveStatement;
	private static PreparedStatement createStatement;
	private static PreparedStatement updateStatement;

	private static Connection createConnection() throws SQLException {
        try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch(ClassNotFoundException e) {
			logger.error(e);
		}

		String dbURL = "jdbc:derby:db/session;create=true;user=\"session\";password=\"session\"";
    	Connection connection = DriverManager.getConnection(dbURL);
        Statement s = connection.createStatement();
        try {
	        s.executeUpdate("SET SCHEMA APP");
	        if(runCreate) {
	        	runCreate = false;
		        try {
		        	s.executeUpdate("CREATE TABLE sessions(" +
		        			"id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
		        			"uuid CHAR(36)," +
		        			"data VARCHAR(32672)," +
		        			"expiration TIMESTAMP)");
		        	s.executeUpdate("CREATE UNIQUE INDEX session_id_uuid ON sessions(id, uuid)");
		        } catch(SQLException e) {
		        	logger.trace("could not create database table for sessions", e);
		        }
	        }
        } finally {
        	try {
        		s.close();
        	} catch(SQLException e) {
	        	logger.warn(e);
        	}
        }
        
        createStatement = null;
        updateStatement = null;
        
        return connection;
    }

	public static Connection getConnection() throws SQLException {
		if(!hasConnection()) {
			try {
				connection = createConnection();
			} catch(SQLException e) {
				connection = null;
				throw e;
			}
		}
    	return connection;
    }
	
	public static boolean hasConnection() {
    	try {
			return (connection != null && !connection.isClosed());
		} catch(SQLException e) {
			logger.warn(e);
			return false;
		}
    }
	
    public static DbSession retrieve(int id, String uuid) {
		if(id > 0 && uuid != null) {
			try {
				Connection connection = getConnection();
				retrieveStatement = connection.prepareStatement(retrieveSql);
				retrieveStatement.clearParameters();
				retrieveStatement.setInt(1, id);
				retrieveStatement.setString(2, uuid);
				ResultSet rs = retrieveStatement.executeQuery();
				if(rs.next()) {
					String data = rs.getString(1);
					Timestamp expiration = rs.getTimestamp(2);
					return new DbSession(id, uuid, data, expiration);
				}
			} catch(SQLException e) {
				logger.warn(e);
			}
		}
		return null;
	}

	private static Map<String, String> toMap(String data) {
		if(data == null || data.length() == 0) {
			return new HashMap<String, String>(0);
		}
		
		Map<String, String> dataMap = new HashMap<String, String>();
		for(String line : data.split("\n")) {
			String[] sa = line.split("=", 2);
			dataMap.put(sa[0], (sa.length == 2) ? sa[1] : "");
		}
		return dataMap;
	}

	
	private static String toString(Map<String, String> dataMap) {
		StringBuilder sb = new StringBuilder();
		for(String key : dataMap.keySet()) {
			sb.append(key).append('=').append(dataMap.get(key)).append('\n');
		}
		return sb.toString();
	}

	
	private int id;
	private String uuid;
	private Map<String, String> dataMap;
	private Timestamp expiration;
	
	public DbSession() {
		this(-1, UUID.randomUUID().toString(), new HashMap<String, String>(), new Timestamp(System.currentTimeMillis() + 30*60*1000));
	}
	
	private DbSession(int id, String uuid, Map<String, String> dataMap, Timestamp expiration) {
		this.id = id;
		this.uuid = uuid;
		this.dataMap = dataMap;
		this.expiration = (Timestamp) expiration.clone();
	}
	
	private DbSession(int id, String uuid, String data, Timestamp expiration) {
		this(id, uuid, toMap(data), expiration);
	}
	
	public DbSession(Timestamp expiration) {
		this(-1, UUID.randomUUID().toString(), new HashMap<String, String>(), expiration);
	}
	
    @Override
	public void clear() {
		dataMap.clear();
	}

	private void create(Connection connection) throws SQLException {
		if(createStatement == null || createStatement.isClosed()) {
			createStatement = connection.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS);
		}
		
		createStatement.clearParameters();
		createStatement.setString(1, uuid);
		createStatement.setString(2, toString(dataMap));
		createStatement.setTimestamp(3, expiration);

		createStatement.executeUpdate();
		ResultSet rs = createStatement.getGeneratedKeys();
		if(rs.next()) {
			id = rs.getInt(1);
		}
		rs.close();
	}

	@Override
	public boolean destroy() {
    	String sql = "DELETE FROM sessions where id=" + id;
    	try {
    		getConnection().createStatement().executeUpdate(sql);
    		id = -1;
    		uuid = null;
    		dataMap.clear();
    		dataMap = null;
    		expiration = null;
    		return true;
    	} catch(SQLException e) {
			logger.error(e);
    	}
    	return false;
	}

	@Override
	public String get(String key) {
		return dataMap.get(key);
	}

	@Override
	public Timestamp getExpiration() {
		return (Timestamp) expiration.clone();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getUuid() {
		return uuid;
	}
	
	@Override
	public boolean isDestroyed() {
		return expiration == null || expiration.before(new Date());
	}
	
	@Override
	public void put(String key, boolean value) {
		dataMap.put(key, Boolean.toString(value));
	}
	
	@Override
	public void put(String key, double value) {
		dataMap.put(key, Double.toString(value));
	}

	@Override
	public void put(String key, long value) {
		dataMap.put(key, Long.toString(value));
	}
	
	@Override
	public void put(String key, String value) {
		dataMap.put(key, value);
	}
	
	@Override
	public void remove(String key) {
		dataMap.remove(key);
	}

	@Override
	public boolean save() {
    	try {
    		Connection connection = getConnection();
    		if(id < 1) {
    			create(connection);
    			return id > 0;
    		} else {
    			return update(connection) == 1;
    		}
    	} catch(SQLException e) {
			logger.warn(e);
    	}
		return false;
	}
	
	@Override
	public void setExpiration(Timestamp expiration) {
		this.expiration = (Timestamp) expiration.clone();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{id=").append(id).append(", expires=").append(expiration).append(", data=").append(dataMap).append('}');
		return sb.toString();
	}
	
	private int update(Connection connection) throws SQLException {
		if(updateStatement == null || updateStatement.isClosed()) {
			updateStatement = connection.prepareStatement(updateSql);
		}
		
		updateStatement.clearParameters();
		updateStatement.setString(1, toString(dataMap));
		updateStatement.setTimestamp(2, expiration);
		updateStatement.setInt(3, id);

		return updateStatement.executeUpdate();
	}
	
}
