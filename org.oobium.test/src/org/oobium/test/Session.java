package org.oobium.test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.oobium.http.HttpSession;

public class Session implements HttpSession {

	private int id;
	private String uuid;
	private Map<String, String> dataMap;
	private Date expiration;

	public Session() {
		this(-1, "*", new LinkedHashMap<String, String>(), new Date(System.currentTimeMillis() + 30*60*1000));
	}
	
	public Session(int id, String uuid) {
		this(id, uuid, new LinkedHashMap<String, String>(), new Date(System.currentTimeMillis() + 30*60*1000));
	}
	
	public Session(int id, String uuid, Map<String, String> dataMap, Date expiration) {
		this.id = id;
		this.uuid = uuid;
		this.dataMap = dataMap;
		this.expiration = (Date) expiration.clone();
	}
	
	@Override
	public void clearData() {
		dataMap.clear();
	}

	@Override
	public boolean destroy() {
		id = -1;
		uuid = null;
		dataMap.clear();
		dataMap = null;
		expiration = null;
		return true;
	}

	@Override
	public String getData(String key) {
		return dataMap.get(key);
	}

	@Override
	public Date getExpiration() {
		return (Date) expiration.clone();
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
	public void putData(String key, boolean value) {
		dataMap.put(key, Boolean.toString(value));
	}
	
	@Override
	public void putData(String key, double value) {
		dataMap.put(key, Double.toString(value));
	}

	@Override
	public void putData(String key, long value) {
		dataMap.put(key, Long.toString(value));
	}
	
	@Override
	public void putData(String key, String value) {
		dataMap.put(key, value);
	}
	
	@Override
	public String removeData(String key) {
		return dataMap.remove(key);
	}

	@Override
	public boolean save() {
		return true;
	}

	@Override
	public void setExpiration(Date expiration) {
		this.expiration = (Date) expiration.clone();
	}

}
