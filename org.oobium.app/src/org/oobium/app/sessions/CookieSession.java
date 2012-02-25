package org.oobium.app.sessions;

import static org.oobium.utils.json.JsonUtils.toJson;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CookieSession implements ISession {

	private Map<String, String> cookieData;
	private boolean destroyed;

	public CookieSession() {
		// default constructor
	}
	
	public CookieSession(Map<String, String> cookieData) {
		this.cookieData = new HashMap<String, String>(cookieData);
	}
	
	@Override
	public void clearCookieData() {
    	if(cookieData != null) {
    		cookieData.clear();
    	}
	}
	
	@Override
	public void clearData() {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public boolean destroy() {
		return destroyed = true;
	}

	@Override
	public String getCookieData() {
		if(cookieData != null) {
			return toJson(cookieData);
		}
		return "";
	}

	@Override
	public String getCookieData(String key) {
		if(cookieData != null) {
			return cookieData.get(key);
		}
		return null;
	}

	@Override
	public Object getData(String key) {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public Date getExpiration() {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public Object getId() {
		return getCookieData(SESSION_ID_KEY);
	}

	@Override
	public String getUuid() {
		return getCookieData(SESSION_UUID_KEY);
	}

	@Override
	public boolean isCookieOnly() {
		return true;
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public ISession putCookieData(Map<String, String> data) {
		if(cookieData == null) {
			cookieData = new HashMap<String, String>(data);
		} else {
			cookieData.putAll(data);
		}
		return this;
	}

	@Override
	public ISession putCookieData(String key, String value) {
		if(cookieData == null) {
			cookieData = new HashMap<String, String>();
		}
		cookieData.put(key, value);
		return this;
	}

	@Override
	public ISession putData(Map<String, Object> data) {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public ISession putData(String key, Object value) {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public Object removeData(String key) {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public boolean save() {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public ISession setExpiration(Date expiration) {
		throw new UnsupportedOperationException("session only has cookie data");
	}

	@Override
	public ISession setExpiration(int secondsFromNow) {
		throw new UnsupportedOperationException("session only has cookie data");
	}

}
