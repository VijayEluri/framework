package org.oobium.persist.mongo;

import static org.oobium.utils.StringUtils.blank;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class Database {

	private final Map<String, Object> properties;
	
	private final Mongo mongo;

	public Database(Map<String, Object> properties) throws Exception {
		this.properties = initProperties(properties);
		mongo = new Mongo(getHost(), getPort());
	}
	
	public DB getDB() {
		return mongo.getDB(getDatabaseName());
	}
	
	public void close() {
		mongo.close();
	}
	
	public void dropDatabase() {
		mongo.dropDatabase(getDatabaseName());
	}
	
	public String getDatabaseName() {
		return (String) properties.get("database");
	}
	
	public String getHost() {
		return (String) properties.get("host");
	}
	
	public int getPort() {
		return (Integer) properties.get("port");
	}
	
	private Map<String, Object> initProperties(Map<String, Object> properties) {
		Map<String, Object> props = new HashMap<String, Object>(properties);
		if(blank(props.get("database"))) {
			throw new IllegalArgumentException("\"database\" field cannot be blank in persist configuration");
		}
		if(props.get("host") == null) {
			props.put("host", "localhost");
		}
		if(props.get("port") == null) {
			props.put("port", 27017);
		}
		if(props.get("username") == null) {
			props.put("username", "");
		}
		if(props.get("password") == null) {
			props.put("password", "");
		}
		return props;
	}

}
